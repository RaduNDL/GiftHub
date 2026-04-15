package com.example.gifthub.repositories

import com.example.gifthub.models.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    fun createUser(
        user: UserDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (user.userId.isBlank()) {
            onError("Invalid user ID"); return
        }
        val safeUser = user.copy(role = "customer")

        usersCollection.document(safeUser.userId)
            .set(safeUser)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to create user") }
    }

    fun getUser(
        userId: String,
        onSuccess: (UserDto?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId.isBlank()) {
            onError("Invalid user ID"); return
        }
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.toObject(UserDto::class.java))
            }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to fetch user") }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""

    fun signOut() {
        auth.signOut()
    }

    fun getProfileStats(
        userId: String,
        onSuccess: (ordersCount: Int, wishlistCount: Int, cardsCount: Int, addressesCount: Int) -> Unit
    ) {
        if (userId.isBlank()) {
            onSuccess(0, 0, 0, 0); return
        }
        val userDoc = usersCollection.document(userId)

        safeCount(userDoc.collection("orders")) { orders ->
            safeCount(userDoc.collection("favorites")) { wishlist ->
                safeCount(userDoc.collection("paymentMethods")) { cards ->
                    safeCount(userDoc.collection("addresses")) { addresses ->
                        onSuccess(orders, wishlist, cards, addresses)
                    }
                }
            }
        }
    }

    private fun safeCount(
        collectionRef: CollectionReference,
        onResult: (Int) -> Unit
    ) {
        collectionRef.get()
            .addOnSuccessListener { snapshot -> onResult(snapshot.size()) }
            .addOnFailureListener { onResult(0) }
    }
}