package com.example.gifthub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifthub.models.CategoryDto
import com.example.gifthub.screens.notifications.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
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
                errorMessage = e.message ?: "Error loading categories"
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
                NotificationHelper.notifyCategoryAdded(getApplication(), name)
                loadCategories()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error adding category"
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
                NotificationHelper.notifyCategoryUpdated(getApplication(), name)
                loadCategories()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error updating category"
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
                NotificationHelper.notifyCategoryDeleted(getApplication())
                loadCategories()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error deleting category"
                isLoading = false
            }
        }
    }
}