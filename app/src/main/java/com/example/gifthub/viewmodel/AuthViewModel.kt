package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.UserDto
import com.example.gifthub.repositories.AuthRepository
import com.example.gifthub.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isAuthenticated by mutableStateOf(authRepository.getCurrentUser() != null)
        private set

    var currentUserRole by mutableStateOf<String?>(null)
        private set

    init {
        checkUserRoleStatus()
    }

    private fun checkUserRoleStatus() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            userRepository.getUser(
                userId = user.uid,
                onSuccess = { userDto ->
                    currentUserRole = userDto?.role ?: "customer"
                    syncFcmToken(user.uid)
                },
                onError = {
                    currentUserRole = "customer"
                    syncFcmToken(user.uid)
                }
            )
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            errorMessage = "All fields are required."
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.register(
            email = email,
            password = password,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    val newUser = UserDto(
                        userId = firebaseUser.uid,
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        role = "customer"
                    )

                    userRepository.createUser(
                        user = newUser,
                        onSuccess = {
                            syncFcmToken(firebaseUser.uid)
                            currentUserRole = "customer"
                            isLoading = false
                            isAuthenticated = true
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                } else {
                    isLoading = false
                    errorMessage = "Registration succeeded, but user session is missing."
                }
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password are required."
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.login(
            email = email,
            password = password,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    userRepository.getUser(
                        userId = firebaseUser.uid,
                        onSuccess = { userDto ->
                            currentUserRole = userDto?.role ?: "customer"
                            syncFcmToken(firebaseUser.uid)
                            isLoading = false
                            isAuthenticated = true
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                } else {
                    isLoading = false
                    errorMessage = "Login succeeded, but user session is missing."
                }
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            errorMessage = "Email is required."
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.sendPasswordReset(
            email = email,
            onSuccess = {
                isLoading = false
                errorMessage = "Reset email sent."
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun logout() {
        authRepository.logout()
        isAuthenticated = false
        currentUserRole = null
    }

    fun clearMessage() {
        errorMessage = null
    }

    private fun syncFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token: String ->
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
            }
    }
}