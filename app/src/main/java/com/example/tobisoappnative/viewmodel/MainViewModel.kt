package com.example.tobisoappnative.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobisoappnative.model.ApiClient
import com.example.tobisoappnative.model.Category
import com.example.tobisoappnative.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError

    fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val categories = ApiClient.apiService.getCategories()
                _categories.value = categories
                _categoryError.value = null
            } catch (e: Exception) {
                _categories.value = emptyList()
                _categoryError.value = e.message ?: e.toString()
            }
        }
    }

    fun loadPosts(categoryId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val posts = ApiClient.apiService.getPosts(categoryId)
                _posts.value = posts
            } catch (e: Exception) {
                _posts.value = emptyList()
            }
        }
    }
}
