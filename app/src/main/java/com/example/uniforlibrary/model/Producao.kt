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

    val rating: Float = 0f, // Média de avaliações dos leitores

    @get:PropertyName("data_avaliacao")
    @set:PropertyName("data_avaliacao")
    var dataAvaliacao: Timestamp? = null,

    @get:PropertyName("motivo_avaliacao")
    @set:PropertyName("motivo_avaliacao")
    var motivoAvaliacao: String? = null,

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
        dataAvaliacao = null,
        motivoAvaliacao = null,
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

        dataAvaliacao?.let {
            map["data_avaliacao"] = it
        }

        motivoAvaliacao?.let {
            map["motivo_avaliacao"] = it
        }

        return map
    }
}

