package com.example.gifthub.screens.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object PushTokenManager {
    fun syncCurrentToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
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
    }

    fun subscribeDefaultTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
    }
}