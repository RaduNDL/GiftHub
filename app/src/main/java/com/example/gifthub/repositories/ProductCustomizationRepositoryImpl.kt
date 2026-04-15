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
        if (productId.isBlank()) return ProductDto()
        val snapshot = firestore.collection("products").document(productId).get().await()
        val dto = snapshot.toObject(ProductDto::class.java) ?: return ProductDto()
        return dto.copy(idProduct = if (dto.idProduct.isBlank()) snapshot.id else dto.idProduct)
    }

    override suspend fun addCustomizedItemToCart(
        userId: String,
        product: ProductDto,
        quantity: Int,
        selections: List<SelectedCustomization>
    ): Result<Unit> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("Missing user"))
        if (product.idProduct.isBlank()) return Result.failure(IllegalArgumentException("Missing product"))

        val validation = validateSelections(product, selections)
        if (validation.isFailure) return validation

        val safeQuantity = quantity.coerceAtLeast(1)
        val extraPerUnit = computeCustomizationExtra(selections)
        val hash = buildCustomizationHash(selections)
        val docId = if (selections.isEmpty()) product.idProduct else "${product.idProduct}_$hash"

        val itemRef = firestore.collection("users")
            .document(userId)
            .collection("shoppingCart")
            .document("current")
            .collection("items")
            .document(docId)

        return try {
            val existing = itemRef.get().await()
            val now = System.currentTimeMillis()

            if (existing.exists()) {
                val oldQty = (existing.getLong("quantity") ?: 0L).toInt()
                val newQty = oldQty + safeQuantity
                val newTotal = (product.price + extraPerUnit) * newQty

                itemRef.update(
                    mapOf(
                        "quantity" to newQty,
                        "lineExtraPrice" to extraPerUnit,
                        "lineTotalPrice" to newTotal,
                        "updatedAt" to now
                    )
                ).await()
            } else {
                val item = CartItem(
                    id = docId,
                    userId = userId,
                    productId = product.idProduct,
                    productName = if (selections.isEmpty()) product.name else "${product.name} (Customized)",
                    productImage = product.imageUrl,
                    basePrice = product.price,
                    quantity = safeQuantity,
                    selectedCustomizations = selections,
                    customizationsHash = hash,
                    lineExtraPrice = extraPerUnit,
                    lineTotalPrice = (product.price + extraPerUnit) * safeQuantity,
                    createdAt = now,
                    updatedAt = now
                )
                itemRef.set(item).await()
            }

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
            .joinToString("|") { s ->
                s.optionId + ":" + s.selectedValueIds.sorted().joinToString(",")
            }
    }

    override fun validateSelections(
        product: ProductDto,
        selections: List<SelectedCustomization>
    ): Result<Unit> {
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
                val selectedIds = selected?.selectedValueIds.orEmpty()
                if (!selectedIds.all { it in allowed }) {
                    return Result.failure(IllegalArgumentException("Invalid selected values for option: ${option.name}"))
                }
            }
        }
        return Result.success(Unit)
    }
}