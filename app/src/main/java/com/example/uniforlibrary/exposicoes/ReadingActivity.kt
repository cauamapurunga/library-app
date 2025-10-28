package com.example.uniforlibrary.exposicoes

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import kotlinx.coroutines.delay

class ReadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ReadingScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0.2f) }
    var showRatingPopup by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (progress >= 1.0f) {
            delay(3000)
            showRatingPopup = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exposições", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                ReadingProgressHeader(progress = progress, onAdvance = { progress = (progress + 0.2f).coerceAtMost(1f) })
                Spacer(modifier = Modifier.height(24.dp))

                if (progress < 1.0f) {
                    ReadingContent()
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                        Text("- FIM -", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            AnimatedVisibility(visible = showRatingPopup, modifier = Modifier.align(Alignment.Center)) {
                RatingPopup(
                    onConfirm = { rating ->
                        showRatingPopup = false
                        Toast.makeText(context, "Avaliação ($rating) enviada!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    onDismiss = { showRatingPopup = false }
                )
            }
        }
    }
}

@Composable
fun ReadingProgressHeader(progress: Float, onAdvance: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { /* TODO: Voltar página */ }) {
            Text("< Voltar")
        }
        Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        TextButton(onClick = onAdvance) {
            Text("Avançar >")
        }
    }
}

@Composable
fun ReadingContent() {
    Column {
        Text("- Contexto Histórico -", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "A história de Path of Exile se passa no continente de Wraeclast. Esse continente foi devastado por um evento apocalíptico chamado de Cataclisma que aconteceu 250 anos antes de começarmos o jogo. A sudeste de Wraeclast há uma ilha chamada Oriath que não foi atingida por esse Cataclisma. No início do jogo você...",
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun RatingPopup(onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var rating by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.padding(32.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Agradeceríamos caso possa dar uma nota para a obra.", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { rating = star } // Esta linha agora funcionará
                            .size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDismiss) { Text("< Voltar") }
                TextButton(onClick = { onConfirm(rating) }, enabled = rating > 0) { Text("Confirmar >") }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ReadingScreenPreview() {
    UniforLibraryTheme {
        ReadingScreen(onBack = {})
    }
}
