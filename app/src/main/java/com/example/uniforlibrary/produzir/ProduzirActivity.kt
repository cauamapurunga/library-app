package com.example.uniforlibrary.produzir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class ProduzirActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ProduzirScreen(
                    onBack = { finish() } 
                )
            }
        }
    }
}
