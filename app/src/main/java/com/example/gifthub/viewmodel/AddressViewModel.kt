package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.AddressDto
import com.example.gifthub.repositories.AddressRepository
import com.example.gifthub.repositories.NotificationRepository
import com.example.gifthub.screens.notifications.NotificationHelper

class AddressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AddressRepository()
    private val notificationRepository = NotificationRepository()

    var addresses by mutableStateOf<List<AddressDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    fun loadAddresses() {
        isLoading = true
        errorMessage = null
        repository.getAddresses(
            onSuccess = {
                addresses = it
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun saveAddress(
        idAddress: String,
        street: String,
        city: String,
        zipcode: String
    ) {
        if (street.isBlank() || city.isBlank() || zipcode.isBlank()) {
            errorMessage = "All fields are required."
            return
        }

        isLoading = true
        errorMessage = null

        val label = "${street.trim()}, ${city.trim()}"

        if (idAddress.isBlank()) {
            repository.addAddress(
                street = street,
                city = city,
                zipcode = zipcode,
                onSuccess = {
                    userMessage = "Address added successfully."
                    NotificationHelper.notifyAddressAdded(getApplication(), label)
                    notificationRepository.createAddressNotification(
                        title = "Address added",
                        message = "$label was added",
                        type = "address_added"
                    )
                    loadAddresses()
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } else {
            repository.updateAddress(
                address = AddressDto(
                    idAddress = idAddress,
                    street = street.trim(),
                    city = city.trim(),
                    zipcode = zipcode.trim()
                ),
                onSuccess = {
                    userMessage = "Address updated successfully."
                    NotificationHelper.notifyAddressUpdated(getApplication(), label)
                    notificationRepository.createAddressNotification(
                        title = "Address updated",
                        message = "$label was updated",
                        type = "address_update"
                    )
                    loadAddresses()
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        }
    }

    fun deleteAddress(addressId: String) {
        if (addressId.isBlank()) {
            errorMessage = "Invalid ID."
            return
        }

        val label = addresses.firstOrNull { it.idAddress == addressId }?.let { "${it.street}, ${it.city}" } ?: "Address"

        isLoading = true
        errorMessage = null

        repository.deleteAddress(
            addressId = addressId,
            onSuccess = {
                userMessage = "Address deleted."
                NotificationHelper.notifyAddressDeleted(getApplication(), label)
                notificationRepository.createAddressNotification(
                    title = "Address deleted",
                    message = "$label was removed",
                    type = "address_deleted"
                )
                loadAddresses()
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun clearUserMessage() {
        userMessage = null
    }
}