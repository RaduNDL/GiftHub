package com.example.gifthub.models

data class SelectedCustomization(
    val optionId: String = "",
    val optionName: String = "",
    val selectedValueIds: List<String> = emptyList(),
    val selectedLabels: List<String> = emptyList(),
    val extraPriceTotal: Double = 0.0
)
