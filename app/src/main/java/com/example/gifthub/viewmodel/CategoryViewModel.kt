package com.example.gifthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gifthub.models.CategoryDto
import com.example.gifthub.repositories.CategoryRepository

class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var categoriesList by mutableStateOf<List<CategoryDto>>(emptyList())
        private set

    init {
        loadCategories()
    }

    fun loadCategories() {
        isLoading = true
        errorMessage = null

        repository.getAllCategories(
            onSuccess = { categories ->
                categoriesList = categories
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    fun addCategory(name: String, description: String) {
        if (name.isBlank() || description.isBlank()) {
            errorMessage = "Name and description are required."
            return
        }

        isLoading = true
        errorMessage = null

        val category = CategoryDto(
            name = name.trim(),
            description = description.trim()
        )

        repository.addCategory(
            category = category,
            onSuccess = {
                isLoading = false
                loadCategories()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun updateCategory(categoryId: String, name: String, description: String) {
        if (categoryId.isBlank()) {
            errorMessage = "Invalid category ID."
            return
        }

        if (name.isBlank() || description.isBlank()) {
            errorMessage = "Name and description are required."
            return
        }

        isLoading = true
        errorMessage = null

        val category = CategoryDto(
            categoryId = categoryId,
            name = name.trim(),
            description = description.trim()
        )

        repository.updateCategory(
            category = category,
            onSuccess = {
                isLoading = false
                loadCategories()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    fun deleteCategory(categoryId: String) {
        if (categoryId.isBlank()) {
            errorMessage = "Invalid category ID."
            return
        }

        isLoading = true
        errorMessage = null

        repository.deleteCategory(
            categoryId = categoryId,
            onSuccess = {
                isLoading = false
                loadCategories()
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }
}