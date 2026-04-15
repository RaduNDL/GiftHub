package com.example.gifthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomization
import com.example.gifthub.repositories.ProductCustomizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class ProductCustomizationViewModel(
    private val repository: ProductCustomizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductCustomizationUiState())
    val uiState: StateFlow<ProductCustomizationUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: String) {
        _uiState.value = ProductCustomizationUiState(loading = true)

        viewModelScope.launch {
            try {
                if (productId.isBlank()) {
                    _uiState.value = _uiState.value.copy(loading = false, error = "Invalid product ID")
                    return@launch
                }

                val product = repository.getProduct(productId)
                if (product.idProduct.isBlank()) {
                    _uiState.value = _uiState.value.copy(loading = false, error = "Product not found")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    product = product,
                    totalPrice = product.price,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load product"
                )
            }
        }
    }

    fun setProduct(product: ProductDto) {
        if (product.idProduct.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Invalid product")
            return
        }
        _uiState.value = _uiState.value.copy(
            product = product,
            totalPrice = product.price,
            error = null
        )
    }

    fun onToggleValue(optionId: String, valueId: String, multiSelect: Boolean) {
        val current = _uiState.value
        val currentSet = current.selectedByOption[optionId] ?: emptySet()

        val newSet = if (multiSelect) {
            if (currentSet.contains(valueId)) currentSet - valueId else currentSet + valueId
        } else {
            if (currentSet.contains(valueId)) emptySet() else setOf(valueId)
        }

        val newMap = current.selectedByOption.toMutableMap()
        if (newSet.isEmpty()) newMap.remove(optionId) else newMap[optionId] = newSet

        _uiState.value = current.copy(selectedByOption = newMap, error = null)
        recalculateTotal()
    }

    fun setQuantity(quantity: Int) {
        val safeQuantity = quantity.coerceAtLeast(1).coerceAtMost(999)
        _uiState.value = _uiState.value.copy(quantity = safeQuantity)
        recalculateTotal()
    }

    private fun recalculateTotal() {
        val current = _uiState.value
        val product = current.product ?: return
        val selections = buildSelections(product, current.selectedByOption)
        val extra = selections.sumOf { it.extraPriceTotal }
        _uiState.value = current.copy(
            totalPrice = (product.price + extra) * current.quantity
        )
    }

    fun addToCartWithCartViewModel(cartViewModel: CartViewModel) {
        val current = _uiState.value
        val product = current.product

        if (product == null || product.idProduct.isBlank()) {
            _uiState.value = current.copy(error = "Product data is invalid")
            return
        }
        if (current.quantity <= 0) {
            _uiState.value = current.copy(error = "Invalid quantity")
            return
        }

        val missingRequired = product.customizationOptions.filter {
            it.required && current.selectedByOption[it.id].isNullOrEmpty()
        }
        if (missingRequired.isNotEmpty()) {
            val names = missingRequired.joinToString(", ") { it.name }
            _uiState.value = current.copy(error = "Please select: $names")
            return
        }

        try {
            val selections = buildSelections(product, current.selectedByOption)

            cartViewModel.addCustomizedToCart(
                product = product,
                quantity = current.quantity,
                selections = selections
            )

            _uiState.value = current.copy(successAdded = true, error = null)
        } catch (e: Exception) {
            _uiState.value = current.copy(error = "Error preparing customization: ${e.message}")
        }
    }

    private fun buildSelections(
        product: ProductDto,
        selectedByOption: Map<String, Set<String>>
    ): List<SelectedCustomization> {
        return product.customizationOptions.mapNotNull { option ->
            val selectedIds = selectedByOption[option.id] ?: return@mapNotNull null
            val values = option.values.filter { it.id in selectedIds }
            if (values.isEmpty()) return@mapNotNull null

            SelectedCustomization(
                optionId = option.id,
                optionName = option.name,
                selectedValueIds = values.map { it.id },
                selectedLabels = values.map { it.label },
                extraPriceTotal = values.sumOf { it.extraPrice }
            )
        }
    }
}