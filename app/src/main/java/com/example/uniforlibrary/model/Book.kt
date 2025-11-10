package com.example.uniforlibrary.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp

data class Book(
    var id: String = "",
    val title: String = "",
    val author: String = "",

    @get:PropertyName("publication_year")
    @set:PropertyName("publication_year")
    var publicationYear: Int = 0,

    @get:PropertyName("category_id")
    @set:PropertyName("category_id")
    var categoryId: Int = 0,

    val description: String = "",
    val rating: Int = 0,

    @get:PropertyName("is_digital")
    @set:PropertyName("is_digital")
    var isDigital: Boolean = false,

    @get:PropertyName("total_copies")
    @set:PropertyName("total_copies")
    var totalCopies: Int = 0,

    @get:PropertyName("available_copies")
    @set:PropertyName("available_copies")
    var availableCopies: Int = 0,

    @get:PropertyName("cover_image_url")
    @set:PropertyName("cover_image_url")
    var coverImageUrl: String = "",

    @get:PropertyName("digital_content_url")
    @set:PropertyName("digital_content_url")
    var digitalContentUrl: String = "",

    val isbn: String = "",

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp? = null,

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Timestamp? = null
) {
    // Construtor vazio necessário para Firebase
    constructor() : this(
        id = "",
        title = "",
        author = "",
        publicationYear = 0,
        categoryId = 0,
        description = "",
        rating = 0,
        isDigital = false,
        totalCopies = 0,
        availableCopies = 0,
        coverImageUrl = "",
        digitalContentUrl = "",
        isbn = "",
        createdAt = null,
        updatedAt = null
    )

    // Propriedades de compatibilidade
    val year: String
        get() = if (publicationYear > 0) publicationYear.toString() else ""

    val category: String
        get() = getCategoryName(categoryId)

    val isPhysical: Boolean
        get() = !isDigital

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "title" to title,
            "author" to author,
            "publication_year" to publicationYear,
            "category_id" to categoryId,
            "description" to description,
            "rating" to rating,
            "is_digital" to isDigital,
            "total_copies" to totalCopies,
            "available_copies" to availableCopies,
            "cover_image_url" to coverImageUrl,
            "digital_content_url" to digitalContentUrl,
            "isbn" to isbn,
            "created_at" to (createdAt ?: Timestamp.now()),
            "updated_at" to (updatedAt ?: Timestamp.now())
        )

        // Adiciona id apenas se não estiver vazio
        if (id.isNotEmpty()) {
            map["id"] = id
        }

        return map
    }

    fun isAvailable(): Boolean = availableCopies > 0

    fun getAvailabilityText(): String {
        return if (availableCopies > 0) {
            "Disponível ($availableCopies de $totalCopies)"
        } else {
            "Indisponível"
        }
    }

    // Mapeamento de categoria ID para nome
    private fun getCategoryName(id: Int): String {
        return when (id) {
            1 -> "Romance"
            2 -> "Ficção"
            3 -> "Não-ficção"
            4 -> "História"
            5 -> "Ciência"
            6 -> "Tecnologia"
            7 -> "Arte"
            8 -> "Biografia"
            else -> "Outros"
        }
    }
}
