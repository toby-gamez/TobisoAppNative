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

    private val _postError = MutableStateFlow<String?>(null)
    val postError: StateFlow<String?> = _postError

    private val _postDetail = MutableStateFlow<Post?>(null)
    val postDetail: StateFlow<Post?> = _postDetail
    private val _postDetailError = MutableStateFlow<String?>(null)
    val postDetailError: StateFlow<String?> = _postDetailError

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
                _postError.value = null
            } catch (e: Throwable) { // zachytí i fatální chyby
                _posts.value = emptyList()
                _postError.value = e.message ?: e.toString()
            }
        }
    }

    fun loadPostDetail(postId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val post = ApiClient.apiService.getPost(postId)
                _postDetail.value = post
                _postDetailError.value = null
            } catch (e: Throwable) {
                _postDetail.value = null
                _postDetailError.value = e.message ?: e.toString()
            }
        }
    }
}
