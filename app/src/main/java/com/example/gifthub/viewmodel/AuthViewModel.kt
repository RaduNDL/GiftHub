package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.gifthub.models.UserDto
import com.example.gifthub.repositories.AuthRepository
import com.example.gifthub.repositories.NotificationRepository
import com.example.gifthub.repositories.UserRepository
import com.example.gifthub.screens.notifications.NotificationHelper
import com.example.gifthub.screens.notifications.NotificationRealtimeListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val notificationRepository = NotificationRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var infoMessage by mutableStateOf<String?>(null)
        private set

    var isAuthenticated by mutableStateOf(authRepository.getCurrentUser() != null)
        private set

    var currentUserRole by mutableStateOf<String?>(null)
        private set

    init {
        checkUserRoleStatus()
        if (isAuthenticated) {
            NotificationRealtimeListener.start(getApplication())
        }
    }

    private fun checkUserRoleStatus() {
        val user = authRepository.getCurrentUser() ?: return
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

    fun register(firstName: String, lastName: String, email: String, password: String) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "All fields are required."
            return
        }

        if (!isValidEmail(email)) {
            errorMessage = "Invalid email address."
            return
        }

        if (!isValidPassword(password)) {
            errorMessage = "Password must have at least 8 characters with an uppercase letter, lowercase letter, number, and special character."
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.register(
            email = email.trim(),
            password = password,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser == null) {
                    isLoading = false
                    errorMessage = "Registration successful, but session is missing."
                    return@register
                }

                val newUser = UserDto(
                    userId = firebaseUser.uid,
                    email = email.trim(),
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    role = "customer"
                )

                userRepository.createUser(
                    user = newUser,
                    onSuccess = {
                        syncFcmToken(firebaseUser.uid)
                        currentUserRole = "customer"
                        isLoading = false
                        isAuthenticated = true
                        NotificationRealtimeListener.start(getApplication())
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
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

        if (!isValidEmail(email)) {
            errorMessage = "Invalid email address."
            return
        }

        isLoading = true
        errorMessage = null

        authRepository.login(
            email = email.trim(),
            password = password,
            onSuccess = {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser == null) {
                    isLoading = false
                    errorMessage = "Login successful, but session is missing."
                    return@login
                }

                userRepository.getUser(
                    userId = firebaseUser.uid,
                    onSuccess = { userDto ->
                        currentUserRole = userDto?.role ?: "customer"
                        syncFcmToken(firebaseUser.uid)
                        isLoading = false
                        isAuthenticated = true
                        NotificationRealtimeListener.start(getApplication())
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun sendPasswordReset(email: String, onComplete: () -> Unit = {}) {
        if (email.isBlank()) {
            errorMessage = "Email is required."
            return
        }

        if (!isValidEmail(email)) {
            errorMessage = "Invalid email address."
            return
        }

        isLoading = true
        errorMessage = null
        infoMessage = null

        authRepository.sendPasswordReset(
            email = email.trim(),
            onSuccess = {
                isLoading = false
                infoMessage = "Reset email sent! Check your inbox."
                NotificationHelper.notifyPasswordResetSent(getApplication())
                notificationRepository.createNotification(
                    title = "Password reset",
                    message = "Reset link sent to ${email.trim()}",
                    type = "auth_update"
                )
                onComplete()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
                onComplete()
            }
        )
    }

    fun logout() {
        NotificationRealtimeListener.stop()
        authRepository.logout()
        isAuthenticated = false
        currentUserRole = null
        errorMessage = null
        infoMessage = null
    }

    private fun syncFcmToken(userId: String) {
        if (userId.isBlank()) return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { it in "!@#$%^&*" }
    }
}