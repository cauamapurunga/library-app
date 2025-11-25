package com.example.uniforlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ReadingProgress(
    var id: String = "",

    @get:PropertyName("producao_id")
    @set:PropertyName("producao_id")
    var producaoId: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var completed: Boolean = false,

    @get:PropertyName("completed_at")
    @set:PropertyName("completed_at")
    var completedAt: Timestamp? = null
)

