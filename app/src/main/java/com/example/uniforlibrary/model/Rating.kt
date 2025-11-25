package com.example.uniforlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Rating(
    var id: String = "",

    @get:PropertyName("book_id")
    @set:PropertyName("book_id")
    var bookId: String = "",

    @get:PropertyName("producao_id")
    @set:PropertyName("producao_id")
    var producaoId: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("user_name")
    @set:PropertyName("user_name")
    var userName: String = "",

    val stars: Int = 0,
    val comment: String = "",

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp? = null
)

