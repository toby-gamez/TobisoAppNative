package com.example.tobisoappnative.model

import java.util.Date

data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val filePath: String,
    val createdAt: Date,
    val updatedAt: Date?,
    val categoryId: Int?,
    val category: Category?
)
