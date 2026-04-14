package com.example.gifthub.models

data class CustomizationOption(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val required: Boolean = false,
    val minSelection: Int = 0,
    val maxSelection: Int = 1,
    val values: List<CustomizationValue> = emptyList()
)