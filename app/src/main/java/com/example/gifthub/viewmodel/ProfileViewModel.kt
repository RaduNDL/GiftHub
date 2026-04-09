package com.example.gifthub.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.UserDto
import com.example.gifthub.repositories.UserRepository

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val ordersCount: Int = 0,
    val wishlistCount: Int = 0,
    val cardsCount: Int = 0,
    val addressesCount: Int = 0,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val repository = UserRepository()

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        val currentUserId = repository.getCurrentUserId()

        if (currentUserId.isNullOrBlank()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "User not authenticated"
            )
            return
        }

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        repository.getUser(
            userId = currentUserId,
            onSuccess = { user ->
                val safeUser = user ?: UserDto(
                    userId = currentUserId,
                    email = repository.getCurrentUserEmail()
                )

                uiState = uiState.copy(
                    isLoading = false,
                    user = safeUser,
                    errorMessage = null
                )

                repository.getProfileStats(currentUserId) { orders, wishlist, cards, addresses ->
                    uiState = uiState.copy(
                        ordersCount = orders,
                        wishlistCount = wishlist,
                        cardsCount = cards,
                        addressesCount = addresses
                    )
                }
            },
            onError = { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        )
    }

    fun logout() {
        repository.signOut()
    }
}