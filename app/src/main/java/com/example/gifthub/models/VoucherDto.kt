package com.example.gifthub.models

data class VoucherDto(
    val code: String = "",
    val discountValue: Double = 0.0,
    val expirationDate: Long = 0L
)