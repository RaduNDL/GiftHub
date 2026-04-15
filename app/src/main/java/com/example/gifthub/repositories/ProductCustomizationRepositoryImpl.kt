package com.example.gifthub.repositories

import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomization
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductCustomizationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ProductCustomizationRepository {

    private val productsCollection = firestore.collection("products")

    override suspend fun getProduct(productId: String): ProductDto {
        if (productId.isBlank()) {
            return ProductDto()
        }

        return try {
            val doc = productsCollection.document(productId).get().await()
            val product = doc.toObject(ProductDto::class.java) ?: return ProductDto()
            product.copy(idProduct = if (product.idProduct.isBlank()) doc.id else product.idProduct)
        } catch (e: Exception) {
            ProductDto()
        }
    }

    override suspend fun addCustomizedItemToCart(
        userId: String,
        product: ProductDto,
        quantity: Int,
        selections: List<SelectedCustomization>
    ): Result<Unit> {
        return try {
            if (userId.isBlank() || product.idProduct.isBlank() || quantity <= 0) {
                return Result.failure(IllegalArgumentException("Invalid parameters"))
            }

            val hash = buildCustomizationHash(selections)
            val cartItemId = if (selections.isEmpty()) product.idProduct else "${product.idProduct}_$hash"

            val extra = computeCustomizationExtra(selections)
            val lineTotal = (product.price + extra) * quantity

            val itemData = hashMapOf(
                "id" to cartItemId,
                "userId" to userId,
                "productId" to product.idProduct,
                "productName" to product.name,
                "productImage" to product.imageUrl,
                "basePrice" to product.price,
                "quantity" to quantity,
                "selectedCustomizations" to selections.map { it.toMap() },
                "customizationsHash" to hash,
                "lineExtraPrice" to extra,
                "lineTotalPrice" to lineTotal,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("cart")
                .document(cartItemId)
                .set(itemData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun computeCustomizationExtra(selections: List<SelectedCustomization>): Double {
        return selections.sumOf { it.extraPriceTotal }
    }

    override fun buildCustomizationHash(selections: List<SelectedCustomization>): String {
        if (selections.isEmpty()) return ""
        return selections
            .sortedBy { it.optionId }
            .joinToString("_") { sel ->
                sel.selectedValueIds.sorted().joinToString("-")
            }
            .hashCode()
            .toString()
            .take(8)
    }

    override fun validateSelections(
        product: ProductDto,
        selections: List<SelectedCustomization>
    ): Result<Unit> {
        return try {
            val missingRequired = product.customizationOptions.filter { option ->
                option.required && selections.none { it.optionId == option.id }
            }

            if (missingRequired.isNotEmpty()) {
                val names = missingRequired.joinToString(", ") { it.name }
                return Result.failure(IllegalArgumentException("Missing required options: $names"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun SelectedCustomization.toMap(): Map<String, Any> {
    return mapOf(
        "optionId" to optionId,
        "optionName" to optionName,
        "selectedValueIds" to selectedValueIds,
        "selectedLabels" to selectedLabels,
        "extraPriceTotal" to extraPriceTotal
    )
}