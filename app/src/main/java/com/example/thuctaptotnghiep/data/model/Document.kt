package com.example.thuctaptotnghiep.data.model

data class Document(
    val id: String,
    val title: String,
    val description: String,
    val price: Double = 0.0,
    val category: String,
    val fileUrl: String?,
    val thumbnail: String?,
    val sellerId: String,
    val status: String
)