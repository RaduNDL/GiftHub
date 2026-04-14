package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.CategoryDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    var categoriesList by mutableStateOf<List<CategoryDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadCategories() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val snapshot = categoriesCollection.get().await()
                categoriesList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CategoryDto::class.java)?.copy(categoryId = doc.id)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load categories"
            } finally {
                isLoading = false
            }
        }
    }

    fun addCategory(name: String, description: String, imageUrl: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val newCategoryRef = categoriesCollection.document()
                val category = CategoryDto(
                    categoryId = newCategoryRef.id,
                    name = name,
                    description = description,
                    imageUrl = imageUrl
                )
                newCategoryRef.set(category).await()
                loadCategories()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to add category"
                isLoading = false
            }
        }
    }

    fun updateCategory(categoryId: String, name: String, description: String, imageUrl: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val updates = mapOf(
                    "name" to name,
                    "description" to description,
                    "imageUrl" to imageUrl
                )
                categoriesCollection.document(categoryId).update(updates).await()
                loadCategories()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to update category"
                isLoading = false
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                categoriesCollection.document(categoryId).delete().await()
                loadCategories()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to delete category"
                isLoading = false
            }
        }
    }
}