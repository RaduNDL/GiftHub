package com.example.gifthub.models

data class CustomizationOptionDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val required: Boolean = false,
    val minSelection: Int = 0,
    val maxSelection: Int = 1,
    val values: List<CustomizationValueDto> = emptyList()
)

data class CustomizationValueDto(
    val id: String = "",
    val label: String = "",
    val extraPrice: Double = 0.0,
    val colorHex: String? = null,
    val imageUrl: String? = null
)

data class SelectedCustomizationDto(
    val optionId: String = "",
    val optionName: String = "",
    val selectedValueIds: List<String> = emptyList(),
    val selectedLabels: List<String> = emptyList(),
    val uploadedImageUrl: String = "",
    val textInput: String = "",
    val extraPriceTotal: Double = 0.0
)