package com.example.gifthub.repositories

import com.example.gifthub.models.PaymentMethodDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentMethodRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    private fun paymentMethodsCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("paymentMethods")

    fun getPaymentMethods(
        onSuccess: (List<PaymentMethodDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        paymentMethodsCollection(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val methods = snapshot.documents.map { doc ->
                    PaymentMethodDto(
                        transactionId = doc.id,
                        orderID = doc.getString("orderID") ?: "",
                        method = doc.getString("method") ?: "",
                        paymentStatus = doc.getString("paymentStatus") ?: ""
                    )
                }.sortedBy { it.method.lowercase() }

                onSuccess(methods)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load payment methods")
            }
    }

    fun addPaymentMethod(
        method: String,
        paymentStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        val docRef = paymentMethodsCollection(uid).document()
        val data = PaymentMethodDto(
            transactionId = docRef.id,
            orderID = "",
            method = method.trim(),
            paymentStatus = paymentStatus.trim()
        )

        docRef.set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to add payment method")
            }
    }

    fun updatePaymentMethod(
        paymentMethod: PaymentMethodDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        if (paymentMethod.transactionId.isBlank()) {
            onError("Invalid payment method ID")
            return
        }

        paymentMethodsCollection(uid)
            .document(paymentMethod.transactionId)
            .set(paymentMethod)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to update payment method")
            }
    }

    fun deletePaymentMethod(
        transactionId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        paymentMethodsCollection(uid)
            .document(transactionId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to delete payment method")
            }
    }
}