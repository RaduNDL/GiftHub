package com.example.gifthub.repositories

import com.example.gifthub.models.UserDto
import com.google.firebase.auth.FirebaseAuth
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
        usersCollection.document(user.userId)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to create user")
            }
    }

    fun getUser(
        userId: String,
        onSuccess: (UserDto?) -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.toObject(UserDto::class.java))
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch user")
            }
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
        val userDoc = usersCollection.document(userId)

        safeCount(userDoc.collection("orders")) { ordersCount ->
            safeCount(userDoc.collection("favorites")) { wishlistCount ->
                safeCount(userDoc.collection("paymentMethods")) { cardsCount ->
                    safeCount(userDoc.collection("addresses")) { addressesCount ->
                        onSuccess(
                            ordersCount,
                            wishlistCount,
                            cardsCount,
                            addressesCount
                        )
                    }
                }
            }
        }
    }

    private fun safeCount(
        collectionRef: com.google.firebase.firestore.CollectionReference,
        onResult: (Int) -> Unit
    ) {
        collectionRef.get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.size())
            }
            .addOnFailureListener {
                onResult(0)
            }
    }
}