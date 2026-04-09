package com.example.gifthub.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object PushTokenManager {
    fun syncCurrentToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isNotBlank()) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
            }
        }
    }

    fun saveTokenForCurrentUser(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (token.isBlank()) return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    fun clearTokenForCurrentUser(onComplete: () -> Unit = {}) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete()
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", FieldValue.delete())
            .addOnCompleteListener { onComplete() }
    }
}