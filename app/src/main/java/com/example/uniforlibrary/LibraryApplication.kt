package com.example.uniforlibrary

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class LibraryApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Configurar Firestore para desabilitar persistência offline
        // Isso evita erros de índice composto
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false) // Desabilitar cache offline
            .build()
        firestore.firestoreSettings = settings
    }
}

