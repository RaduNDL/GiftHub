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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val product = repository.getProduct(productId)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    product = product,
                    totalPrice = product.price
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
        _uiState.value = _uiState.value.copy(
            product = product,
            totalPrice = product.price
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
        _uiState.value = _uiState.value.copy(quantity = quantity.coerceAtLeast(1))
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
        val product = current.product ?: return
        
        val missingRequired = product.customizationOptions.filter { 
            it.required && !current.selectedByOption.containsKey(it.id) 
        }
        
        if (missingRequired.isNotEmpty()) {
            _uiState.value = current.copy(error = "Please select: ${missingRequired.joinToString { it.name }}")
            return
        }

        val selections = buildSelections(product, current.selectedByOption)
        val customText = selections.joinToString("; ") { sel ->
            "${sel.optionName}: ${sel.selectedLabels.joinToString(", ")}"
        }

        val customColor = product.customizationOptions
            .flatMap { it.values }
            .find { v -> selections.any { s -> s.selectedValueIds.contains(v.id) } && !v.colorHex.isNullOrEmpty() }
            ?.colorHex ?: ""

        cartViewModel.addCustomizedToCart(
            product = product,
            quantity = current.quantity,
            customText = customText,
            customColor = customColor
        )
        
        _uiState.value = current.copy(successAdded = true)
    }

    private fun buildSelections(product: ProductDto, selectedByOption: Map<String, Set<String>>): List<SelectedCustomization> {
        return product.customizationOptions.mapNotNull { option ->
            val selectedIds = selectedByOption[option.id] ?: return@mapNotNull null
            val values = option.values.filter { it.id in selectedIds }
            
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
