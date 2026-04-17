package com.example.gifthub.ui.redeem

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RedeemGiftViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    suspend fun redeemByCode(codeInput: String): Boolean {
        val code = normalizeCode(codeInput)
        if (code.isBlank()) {
            message = "Please enter a voucher code."
            return false
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            message = "You must be logged in."
            return false
        }

        isLoading = true
        message = null

        return try {
            val db = FirebaseFirestore.getInstance()
            val now = System.currentTimeMillis()

            Log.d("RedeemDebug", "Input=[$codeInput] | Normalized=[$code] | userId=[$userId]")

            val query = db.collection("vouchers")
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()

            Log.d("RedeemDebug", "Query result size=${query.documents.size}")

            if (query.documents.isEmpty()) {
                message = "Invalid voucher code."
                isLoading = false
                return false
            }

            val voucherDoc = query.documents.first()
            val voucherId = voucherDoc.id
            val status = voucherDoc.getString("status").orEmpty()
            val assignedUserId = voucherDoc.getString("assignedUserId").orEmpty()
            val expiresAt = voucherDoc.getLong("expiresAt") ?: 0L

            Log.d(
                "RedeemDebug",
                "Voucher found id=[$voucherId] status=[$status] assignedUserId=[$assignedUserId] expiresAt=[$expiresAt]"
            )

            if (status != "active") {
                message = if (status == "redeemed") "Voucher already used." else "Voucher unavailable."
                isLoading = false
                return false
            }

            if (expiresAt in 1..now) {
                message = "Voucher expired."
                isLoading = false
                return false
            }

            if (assignedUserId.isNotBlank() && assignedUserId != userId) {
                message = "This voucher is assigned to another user."
                isLoading = false
                return false
            }

            db.runTransaction { tr ->
                val fresh = tr.get(voucherDoc.reference)
                val freshStatus = fresh.getString("status").orEmpty()
                val freshExpiresAt = fresh.getLong("expiresAt") ?: 0L
                val freshAssignedUserId = fresh.getString("assignedUserId").orEmpty()

                if (freshStatus != "active") {
                    throw IllegalStateException("Voucher already used.")
                }
                if (freshExpiresAt in 1..now) {
                    throw IllegalStateException("Voucher expired.")
                }
                if (freshAssignedUserId.isNotBlank() && freshAssignedUserId != userId) {
                    throw IllegalStateException("This voucher is assigned to another user.")
                }

                tr.update(
                    voucherDoc.reference,
                    mapOf(
                        "status" to "redeemed",
                        "updatedAt" to now,
                        "redeemedBy" to userId,
                        "redeemedAt" to now
                    )
                )

                val redeemLogRef = db.collection("voucherRedeems").document()
                tr.set(
                    redeemLogRef,
                    mapOf(
                        "redeemId" to redeemLogRef.id,
                        "voucherId" to voucherId,
                        "voucherCode" to code,
                        "userId" to userId,
                        "redeemedAt" to now,
                        "status" to "success",
                        "reason" to "ok"
                    )
                )
            }.await()

            message = "Voucher redeemed successfully!"
            true
        } catch (e: Exception) {
            Log.e("RedeemDebug", "Redeem failed", e)
            message = e.message ?: "Redeem failed."
            false
        } finally {
            isLoading = false
        }
    }

    fun clearMessage() {
        message = null
    }

    private fun normalizeCode(raw: String): String {
        val input = raw.trim()

        val urlRegex = Regex("""[?&]code=([^&]+)""")
        urlRegex.find(input)?.let {
            return it.groupValues[1].trim().uppercase()
        }

        val voucherRegex = Regex("""VOUCHER:([A-Za-z0-9\-_]+)""", RegexOption.IGNORE_CASE)
        voucherRegex.find(input)?.let {
            return it.groupValues[1].trim().uppercase()
        }

        return input.uppercase()
    }
}