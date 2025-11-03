package com.example.uniforlibrary.acervo

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val year: Int = 0,
    val category: String = "",
    val description: String = "",
    val totalCopies: Int = 0,
    val availableCopies: Int = 0,
    val rating: Double = 0.0,
    val coverUrl: String = "",
    val isDigital: Boolean = false,
    val isPhysical: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
