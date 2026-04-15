package com.example.gifthub.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun register(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isValidEmail(email)) {
            onError("Invalid email format")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(getErrorMessage(task.exception?.message ?: "Registration failed"))
                }
            }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Email and password are required")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(getErrorMessage(task.exception?.message ?: "Login failed"))
                }
            }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isValidEmail(email)) {
            onError("Please enter a valid email address")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(getErrorMessage(task.exception?.message ?: "Password reset failed"))
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
    }

    private fun getErrorMessage(error: String): String {
        return when {
            error.contains("EMAIL_EXISTS", ignoreCase = true) -> "This email is already registered"
            error.contains("INVALID_EMAIL", ignoreCase = true) -> "Invalid email address"
            error.contains("WEAK_PASSWORD", ignoreCase = true) -> "Password is too weak. Use 8+ characters with uppercase, lowercase, number and special character"
            error.contains("USER_DISABLED", ignoreCase = true) -> "This account has been disabled"
            error.contains("USER_NOT_FOUND", ignoreCase = true) -> "No account found with this email"
            error.contains("INVALID_PASSWORD", ignoreCase = true) -> "Incorrect password"
            error.contains("too many attempts", ignoreCase = true) -> "Too many failed login attempts. Please try again later"
            else -> error
        }
    }
}