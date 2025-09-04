package com.example.tobisoappnative.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobisoappnative.model.ApiClient
import com.example.tobisoappnative.model.Category
import com.example.tobisoappnative.model.Post
import com.example.tobisoappnative.model.Snippet
import com.google.gson.Gson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "saved_posts")

class MainViewModel(application: Application) : AndroidViewModel(application) {
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

    private val dataStore = application.dataStore
    private val FAVORITE_POSTS_KEY = stringSetPreferencesKey("favorite_posts_json")
    private val gson = Gson()

    private val _favoritePosts = MutableStateFlow<List<Post>>(emptyList())
    val favoritePosts: StateFlow<List<Post>> = _favoritePosts

    private val _snippets = MutableStateFlow<List<Snippet>>(emptyList())
    val snippets: StateFlow<List<Snippet>> = _snippets
    private val SNIPPETS_FILE_NAME = "snippets.json"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.data
                .map { prefs ->
                    val jsonSet = prefs[FAVORITE_POSTS_KEY] ?: emptySet()
                    jsonSet.mapNotNull { json ->
                        try {
                            gson.fromJson(json, Post::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .collect { posts ->
                    _favoritePosts.value = posts
                }
        }
    }

    fun savePost(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { prefs ->
                val current = prefs[FAVORITE_POSTS_KEY] ?: emptySet()
                // Pokud už je post uložen, nepřidávej znovu
                val alreadySaved = current.any { json ->
                    try {
                        gson.fromJson(json, Post::class.java).id == post.id
                    } catch (e: Exception) {
                        false
                    }
                }
                if (!alreadySaved) {
                    prefs[FAVORITE_POSTS_KEY] = current + gson.toJson(post)
                }
            }
        }
    }

    fun unsavePost(postId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { prefs ->
                val current = prefs[FAVORITE_POSTS_KEY] ?: emptySet()
                val newSet = current.filterNot { json ->
                    try {
                        gson.fromJson(json, Post::class.java).id == postId
                    } catch (e: Exception) {
                        false
                    }
                }.toSet()
                prefs[FAVORITE_POSTS_KEY] = newSet
            }
        }
    }

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
            } catch (e: Throwable) {
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

    fun loadSnippets() {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, SNIPPETS_FILE_NAME)
            if (!file.exists()) {
                _snippets.value = emptyList()
                return@launch
            }
            try {
                val json = file.readText()
                val loaded = gson.fromJson(json, Array<Snippet>::class.java)?.toList() ?: emptyList()
                _snippets.value = loaded
            } catch (e: Exception) {
                _snippets.value = emptyList()
            }
        }
    }

    fun addSnippet(snippet: Snippet) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, SNIPPETS_FILE_NAME)
            val current = try {
                val json = file.takeIf { it.exists() }?.readText() ?: "[]"
                gson.fromJson(json, Array<Snippet>::class.java)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf<Snippet>()
            }
            current.add(snippet)
            val json = gson.toJson(current)
            file.writeText(json)
            _snippets.value = current
        }
    }

    fun removeSnippet(snippet: Snippet) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, SNIPPETS_FILE_NAME)
            val current = try {
                val json = file.takeIf { it.exists() }?.readText() ?: "[]"
                gson.fromJson(json, Array<Snippet>::class.java)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf<Snippet>()
            }
            val newList = current.filterNot {
                it.postId == snippet.postId && it.content == snippet.content && it.createdAt == snippet.createdAt
            }
            val json = gson.toJson(newList)
            file.writeText(json)
            _snippets.value = newList
        }
    }

    fun clearSnippets() {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, SNIPPETS_FILE_NAME)
            file.writeText("[]")
            _snippets.value = emptyList()
        }
    }

    fun clearFavoritePosts() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { prefs ->
                prefs[FAVORITE_POSTS_KEY] = emptySet()
            }
            _favoritePosts.value = emptyList()
        }
    }
}