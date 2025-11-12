package com.example.uniforlibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Producao(
    var id: String = "",
    val titulo: String = "",
    val categoria: String = "",

    @get:PropertyName("foto_url")
    @set:PropertyName("foto_url")
    var fotoUrl: String = "",

    @get:PropertyName("arquivo_url")
    @set:PropertyName("arquivo_url")
    var arquivoUrl: String = "",

    @get:PropertyName("usuario_id")
    @set:PropertyName("usuario_id")
    var usuarioId: String = "",

    @get:PropertyName("usuario_nome")
    @set:PropertyName("usuario_nome")
    var usuarioNome: String = "",

    val status: String = "pendente", // pendente, aprovado, rejeitado

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp? = null,

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Timestamp? = null
) {
    constructor() : this(
        id = "",
        titulo = "",
        categoria = "",
        fotoUrl = "",
        arquivoUrl = "",
        usuarioId = "",
        usuarioNome = "",
        status = "pendente",
        createdAt = null,
        updatedAt = null
    )

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "titulo" to titulo,
            "categoria" to categoria,
            "foto_url" to fotoUrl,
            "arquivo_url" to arquivoUrl,
            "usuario_id" to usuarioId,
            "usuario_nome" to usuarioNome,
            "status" to status,
            "created_at" to (createdAt ?: Timestamp.now()),
            "updated_at" to (updatedAt ?: Timestamp.now())
        )

        if (id.isNotEmpty()) {
            map["id"] = id
        }

        return map
    }
}

