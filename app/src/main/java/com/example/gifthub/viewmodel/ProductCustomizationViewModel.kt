package com.example.gifthub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomizationDto
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
                    previewImageUrl = product.imageUrl,
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

    fun setTextInput(optionId: String, text: String) {
        val current = _uiState.value
        val newTextInputs = current.textInputs.toMutableMap()
        newTextInputs[optionId] = text
        _uiState.value = current.copy(textInputs = newTextInputs, error = null)
        recalculateTotal()
    }

    fun setUploadedImage(optionId: String, imageUrl: String) {
        val current = _uiState.value
        val newImages = current.uploadedImages.toMutableMap()
        newImages[optionId] = imageUrl

        _uiState.value = current.copy(
            uploadedImages = newImages,
            previewImageUrl = imageUrl,
            error = null
        )
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
            it.required && current.selectedByOption[it.id].isNullOrEmpty() &&
                    current.textInputs[it.id].isNullOrBlank() &&
                    current.uploadedImages[it.id].isNullOrBlank()
        }
        if (missingRequired.isNotEmpty()) {
            val names = missingRequired.joinToString(", ") { it.name }
            _uiState.value = current.copy(error = "Please select: $names")
            return
        }

        try {
            val selections = buildSelectionsWithUploads(
                product,
                current.selectedByOption,
                current.uploadedImages,
                current.textInputs
            )

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
    ): List<SelectedCustomizationDto> {
        return product.customizationOptions.mapNotNull { option ->
            val selectedIds = selectedByOption[option.id] ?: return@mapNotNull null
            val values = option.values.filter { it.id in selectedIds }
            if (values.isEmpty()) return@mapNotNull null

            SelectedCustomizationDto(
                optionId = option.id,
                optionName = option.name,
                selectedValueIds = values.map { it.id },
                selectedLabels = values.map { it.label },
                extraPriceTotal = values.sumOf { it.extraPrice }
            )
        }
    }

    private fun buildSelectionsWithUploads(
        product: ProductDto,
        selectedByOption: Map<String, Set<String>>,
        uploadedImages: Map<String, String>,
        textInputs: Map<String, String>
    ): List<SelectedCustomizationDto> {
        return product.customizationOptions.mapNotNull { option ->
            val hasSelection = selectedByOption[option.id]?.isNotEmpty() == true
            val hasUpload = !uploadedImages[option.id].isNullOrBlank()
            val hasText = !textInputs[option.id].isNullOrBlank()

            when {
                hasSelection -> {
                    val selectedIds = selectedByOption[option.id] ?: return@mapNotNull null
                    val values = option.values.filter { it.id in selectedIds }
                    if (values.isEmpty()) return@mapNotNull null

                    SelectedCustomizationDto(
                        optionId = option.id,
                        optionName = option.name,
                        selectedValueIds = values.map { it.id },
                        selectedLabels = values.map { it.label },
                        extraPriceTotal = values.sumOf { it.extraPrice }
                    )
                }
                hasUpload -> {
                    SelectedCustomizationDto(
                        optionId = option.id,
                        optionName = option.name,
                        uploadedImageUrl = uploadedImages[option.id] ?: ""
                    )
                }
                hasText -> {
                    SelectedCustomizationDto(
                        optionId = option.id,
                        optionName = option.name,
                        textInput = textInputs[option.id] ?: ""
                    )
                }
                else -> null
            }
        }
    }
}