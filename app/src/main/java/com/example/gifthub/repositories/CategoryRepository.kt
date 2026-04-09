package com.example.gifthub.repositories

import com.example.gifthub.models.CategoryDto
import com.google.firebase.firestore.FirebaseFirestore

class CategoryRepository {

    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    fun addCategory(
        category: CategoryDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val documentRef = categoriesCollection.document()
        val categoryWithId = category.copy(categoryId = documentRef.id)

        documentRef.set(categoryWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add category")
            }
    }

    fun getAllCategories(
        onSuccess: (List<CategoryDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        categoriesCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val categories = snapshot.toObjects(CategoryDto::class.java)
                    .sortedBy { it.name.lowercase() }
                onSuccess(categories)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch categories")
            }
    }

    fun updateCategory(
        category: CategoryDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (category.categoryId.isBlank()) {
            onError("Invalid category ID")
            return
        }

        categoriesCollection.document(category.categoryId)
            .set(category)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to update category")
            }
    }

    fun deleteCategory(
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        categoriesCollection.document(categoryId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to delete category")
            }
    }
}