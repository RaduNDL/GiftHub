package com.example.gifthub.repositories

import com.example.gifthub.models.CartItem
import com.example.gifthub.models.ProductDto
import com.example.gifthub.models.SelectedCustomization
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductCustomizationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ProductCustomizationRepository {

    override suspend fun getProduct(productId: String): ProductDto {
        val snapshot = firestore.collection("products").document(productId).get().await()
        val dto = snapshot.toObject(ProductDto::class.java) ?: ProductDto()
        return dto.copy(idProduct = if (dto.idProduct.isBlank()) snapshot.id else dto.idProduct)
    }

    override suspend fun addCustomizedItemToCart(
        userId: String,
        product: ProductDto,
        quantity: Int,
        selections: List<SelectedCustomization>
    ): Result<Unit> {
        val validation = validateSelections(product, selections)
        if (validation.isFailure) return validation

        val safeQuantity = quantity.coerceAtLeast(1)
        val extra = computeCustomizationExtra(selections)
        val hash = buildCustomizationHash(selections)
        val lineTotal = (product.price + extra) * safeQuantity

        val cartItemsRef = firestore.collection("users")
            .document(userId)
            .collection("shoppingCart")
            .document("active")
            .collection("items")

        val existing = cartItemsRef
            .whereEqualTo("productId", product.idProduct)
            .whereEqualTo("customizationsHash", hash)
            .limit(1)
            .get()
            .await()

        if (!existing.isEmpty) {
            val doc = existing.documents.first()
            val oldQty = (doc.getLong("quantity") ?: 1L).toInt()
            val newQty = oldQty + safeQuantity
            val newTotal = (product.price + extra) * newQty

            doc.reference.update(
                mapOf(
                    "quantity" to newQty,
                    "lineExtraPrice" to extra,
                    "lineTotalPrice" to newTotal,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } else {
            val doc = cartItemsRef.document()
            val item = CartItem(
                id = doc.id,
                userId = userId,
                productId = product.idProduct,
                productName = product.name,
                productImage = product.imageUrl,
                basePrice = product.price,
                quantity = safeQuantity,
                selectedCustomizations = selections,
                customizationsHash = hash,
                lineExtraPrice = extra,
                lineTotalPrice = lineTotal,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            doc.set(item).await()
        }

        return Result.success(Unit)
    }

    override fun computeCustomizationExtra(selections: List<SelectedCustomization>): Double {
        return selections.sumOf { it.extraPriceTotal }
    }

    override fun buildCustomizationHash(selections: List<SelectedCustomization>): String {
        return selections
            .sortedBy { it.optionId }
            .joinToString("|") { s ->
                s.optionId + ":" + s.selectedValueIds.sorted().joinToString(",")
            }
    }

    override fun validateSelections(product: ProductDto, selections: List<SelectedCustomization>): Result<Unit> {
        val byOption = selections.associateBy { it.optionId }

        for (option in product.customizationOptions) {
            val selected = byOption[option.id]
            val count = selected?.selectedValueIds?.size ?: 0

            if (option.required && count == 0) {
                return Result.failure(IllegalArgumentException("Missing required option: ${option.name}"))
            }

            if (count < option.minSelection || count > option.maxSelection) {
                return Result.failure(IllegalArgumentException("Invalid selection count for option: ${option.name}"))
            }

            if (count > 0) {
                val allowed = option.values.map { it.id }.toSet()
                val selectedIds = selected?.selectedValueIds ?: emptyList()
                if (!selectedIds.all { it in allowed }) {
                    return Result.failure(IllegalArgumentException("Invalid selected values for option: ${option.name}"))
                }
            }
        }

        return Result.success(Unit)
    }
}
