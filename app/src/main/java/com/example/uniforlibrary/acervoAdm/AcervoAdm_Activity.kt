package com.example.uniforlibrary.acervoAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class AcervoAdm_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                AcervoAdmScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcervoAdmScreen() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("list") }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var bookToModify by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_branca),
                            contentDescription = "Logo Unifor",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gerenciar Acervo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                     if (currentScreen != "list") {
                        IconButton(onClick = { currentScreen = "list" }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, NotificacoesActivity::class.java)) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            AdminBottomNav(context = context, selectedItemIndex = 1)
        },
        floatingActionButton = {
            Row {
                if (currentScreen == "list") {
                    SmallFloatingActionButton(
                        onClick = { currentScreen = "add" },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Chatbot(context = context)
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        val books = remember {
            mutableStateListOf(
                Book("PathExileLORE", "Narak - 2020", "★5"),
                Book("WOW", "Aliens - 1977", "★4.8")
            )
        }

        when (currentScreen) {
            "list" -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { FilterSectionAdmin() }
                    items(books) { book ->
                        AdminBookCard(
                            book = book,
                            onEditClick = {
                                bookToModify = book
                                currentScreen = "edit"
                            },
                            onRemoveClick = {
                                bookToModify = book
                                showRemoveDialog = true
                            }
                        )
                    }
                }
            }
            "add" -> {
                AddEditBookScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentScreen = "list" },
                    onConfirm = { currentScreen = "list" },
                    book = null
                )
            }
            "edit" -> {
                AddEditBookScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentScreen = "list" },
                    onConfirm = { currentScreen = "list" },
                    book = bookToModify
                )
            }
        }

        if (showRemoveDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveDialog = false },
                title = { Text("Tem certeza que deseja remover a obra do acervo?", textAlign = TextAlign.Center) },
                confirmButton = {
                    TextButton(onClick = {
                        showRemoveDialog = false
                        books.remove(bookToModify)
                        Toast.makeText(context, "Obra removida!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Sim", tint = MaterialTheme.colorScheme.primary)
                        Text("Sim", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Não", tint = Color.Red)
                        Text("Não", color = Color.Red)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun AddEditBookScreen(modifier: Modifier = Modifier, book: Book?, onBack: () -> Unit, onConfirm: () -> Unit) {
    val isEditMode = book != null
    var title by remember { mutableStateOf(book?.title ?: "") }
    var author by remember { mutableStateOf(book?.author?.substringBefore(" - ") ?: "") }
    var year by remember { mutableStateOf(book?.author?.substringAfter(" - ") ?: "") }
    var isDigital by remember { mutableStateOf(false) }
    var isPhysical by remember { mutableStateOf(true) }
    var physicalCopies by remember { mutableStateOf("10") }
    var description by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                .clickable { /* TODO: handle image selection */ }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = "Adicionar Capa",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Adicionar capa do livro", color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o título do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o(a) autor(a) do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autor(a)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o ano de publicação do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Ano") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Text("O exemplar é digital e/ou físico?", style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isDigital, onCheckedChange = { isDigital = it })
            Text("Digital")
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(checked = isPhysical, onCheckedChange = { isPhysical = it })
            Text("Físico")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isPhysical) {
            Text("Se é físico, quantos exemplares existem?", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = physicalCopies, onValueChange = { physicalCopies = it }, label = { Text("10") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Digite a descrição do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Text("< Voltar")
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isEditMode) "+ Editar" else "+ Adicionar")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isEditMode) "Tem certeza que deseja editar a obra do acervo?" else "Tem certeza que deseja adicionar a obra ao acervo?", textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val message = if (isEditMode) "Obra editada!" else "Obra adicionada!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    onConfirm()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Sim", tint = MaterialTheme.colorScheme.primary)
                    Text("Sim", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Não", tint = Color.Red)
                    Text("Não", color = Color.Red)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun FilterSectionAdmin() {
    Column {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Pesquisar por título, autor ou ISBN") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Categoria") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Disponibilidade") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }
    }
}

@Composable
fun AdminBookCard(book: Book, onEditClick: () -> Unit, onRemoveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = "Book Icon", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(book.author, color = Color.Gray, fontSize = 14.sp)
                Text(book.rating, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Row {
                TextButton(onClick = onEditClick) { Text("Editar") }
                TextButton(onClick = onRemoveClick) { Text("Remover") }
            }
        }
    }
}

// --- Modelos e Navegação ---
data class Book(val title: String, val author: String, val rating: String)

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun AcervoAdmScreenPreview() {
    UniforLibraryTheme {
        AcervoAdmScreen()
    }
}
