package com.example.gifthub.repositories

import com.example.gifthub.models.AddressDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddressRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    private fun addressesCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("addresses")

    fun getAddresses(
        onSuccess: (List<AddressDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        addressesCollection(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val addresses = snapshot.documents.map { doc ->
                    AddressDto(
                        idAddress = doc.id,
                        userId = uid,
                        street = doc.getString("street") ?: "",
                        city = doc.getString("city") ?: "",
                        zipcode = doc.getString("zipcode") ?: ""
                    )
                }.sortedByDescending { it.idAddress }

                onSuccess(addresses)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load addresses")
            }
    }

    fun addAddress(
        street: String,
        city: String,
        zipcode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        val docRef = addressesCollection(uid).document()
        val address = AddressDto(
            idAddress = docRef.id,
            userId = uid,
            street = street.trim(),
            city = city.trim(),
            zipcode = zipcode.trim()
        )

        docRef.set(address)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to add address")
            }
    }

    fun updateAddress(
        address: AddressDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        if (address.idAddress.isBlank()) {
            onError("Invalid address ID")
            return
        }

        val safeAddress = address.copy(userId = uid)

        addressesCollection(uid)
            .document(address.idAddress)
            .set(safeAddress)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to update address")
            }
    }

    fun deleteAddress(
        addressId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId() ?: return onError("User not authenticated")

        addressesCollection(uid)
            .document(addressId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to delete address")
            }
    }
}