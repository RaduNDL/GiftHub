package com.example.gifthub.models

data class UserDto(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "customer"
)