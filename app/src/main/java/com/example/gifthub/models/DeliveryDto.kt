package com.example.gifthub.models

data class DeliveryDto(
    val awb: String = "",
    val orderID: String = "",
    val courierCompany: String = "",
    val estimatedDate: Long = 0L
)