package com.example.gifthub.screens.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object PushTokenManager {
    private const val TOPIC_ALL_USERS = "all_users"

    fun syncCurrentToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isNullOrBlank()) return@addOnSuccessListener
            writeTokenForUser(uid, token)
        }
    }

    fun updateToken(uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return
        writeTokenForUser(uid, token)
    }

    fun clearCurrentTokenForUser(uid: String) {
        if (uid.isBlank()) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isNullOrBlank()) return@addOnSuccessListener
            val updates = mapOf(
                "fcmTokens.$token" to FieldValue.delete()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(updates, SetOptions.merge())
        }
    }

    private fun writeTokenForUser(uid: String, token: String) {
        val updates = mapOf(
            "fcmToken" to token,
            "fcmTokens.$token" to true,
            "lastTokenRefreshAt" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(updates, SetOptions.merge())
    }

    fun subscribeDefaultTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ALL_USERS)
    }

    fun unsubscribeDefaultTopics() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_ALL_USERS)
    }
}