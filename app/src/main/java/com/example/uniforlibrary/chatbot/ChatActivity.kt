package com.example.uniforlibrary.chatbot

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.BookUiState
import com.example.uniforlibrary.viewmodel.BookViewModel
import com.example.uniforlibrary.viewmodel.ExposicoesUiState
import com.example.uniforlibrary.viewmodel.ExposicoesViewModel
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ChatScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val bookViewModel: BookViewModel = viewModel()
    val producoesViewModel: ExposicoesViewModel = viewModel()
    val bookUiState by bookViewModel.uiState.collectAsState()
    val producaoUiState by producoesViewModel.uiState.collectAsState()

    val messages = remember {
        mutableStateListOf(ChatMessage("Olá! Como posso ajudar você hoje?", isUser = false))
    }
    var inputText by remember { mutableStateOf("") }

    val generativeModel = remember {
        GenerativeModel(
            "gemini-2.0-flash",
            BuildConfig.GEMINI_API_KEY,
        )
    }

    // Manter a mesma instância do chat durante toda a conversa
    val chat = remember { generativeModel.startChat() }

    val scope = rememberCoroutineScope()

    // Carregar livros e produçoes ao iniciar
    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
        producoesViewModel.loadApprovedProducoes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat de Ajuda", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Digite sua dúvida...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (inputText.isNotBlank()) {
                            val userMessage = inputText
                            messages.add(ChatMessage(userMessage, isUser = true))

                            scope.launch {
                                try {
                                    // Preparar contexto dos livros
                                    val booksContext = when (val state = bookUiState) {
                                        is BookUiState.Success -> {
                                            state.books.joinToString("\n") { book ->
                                                "- ${book.title} por ${book.author} (${book.category}) - ${if (book.availableCopies > 0) "Disponível" else "Indisponível"}"
                                            }
                                        }
                                        else -> "Nenhum livro disponível no momento."
                                    }

                                    val producoesContext = when (val state = producaoUiState){
                                        is ExposicoesUiState.Success -> {
                                            state.producoes.joinToString("\n") { producao ->
                                                "- ${producao.titulo} por ${producao.usuarioNome} - ${if (producao.status == "aprovado") "Disponível" else "Indisponível"}"
                                            }
                                        }
                                        else -> "Nenhuma produção disponível no momento."
                                    }

                                    // Enviar mensagem no chat contínuo
                                    val prompt = """
                                        Você é um bibliotecário de uma universidade brasileira. 
                                        Ajude alunos com suas dúvidas sobre livros e a biblioteca.
                                        Seja amigável e prestativo.
                                        Limite suas respostas a no máximo 1200 caracteres.
                                        
                                        LIVROS NA BIBLIOTECA:
                                        $booksContext
                                        
                                        PRODUÇÕES NA BIBLIOTECA:
                                        $producoesContext
                                        
                                        Pergunta do aluno: $userMessage
                                    """.trimIndent()

                                    val aiResponse = chat.sendMessage(prompt)

                                    aiResponse.text?.let {
                                        messages.add(ChatMessage(it, isUser = false))
                                    }
                                    inputText = ""
                                } catch (e: Exception) {
                                    messages.add(ChatMessage("Desculpe, ocorreu um erro: ${e.message}", isUser = false))
                                }
                            }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = Color.DarkGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    UniforLibraryTheme {
        ChatScreen()
    }
}
