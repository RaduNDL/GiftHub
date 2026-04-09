package com.example.gifthub.models

data class AddressDto(
    val idAddress: String = "",
    val userId: String = "",
    val street: String = "",
    val city: String = "",
    val zipcode: String = ""
)