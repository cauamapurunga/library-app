package com.example.uniforlibrary.acervoAdm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.model.Book
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.BookUiState
import com.example.uniforlibrary.viewmodel.BookViewModel

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
    val viewModel: BookViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var currentScreen by remember { mutableStateOf("list") }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var bookToModify by remember { mutableStateOf<Book?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Mostrar toast quando houver resultado de operação
    LaunchedEffect(operationResult) {
        operationResult?.let { result ->
            result.onSuccess { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                currentScreen = "list"
            }.onFailure { error ->
                Toast.makeText(context, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearOperationResult()
        }
    }

    // Realizar busca quando o searchQuery mudar
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.searchBooks(searchQuery)
        }
    }

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
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
                        onClick = {
                            bookToModify = null
                            currentScreen = "add"
                        },
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
        when (currentScreen) {
            "list" -> {
                val booksToDisplay = if (searchQuery.isNotEmpty()) searchResults else {
                    when (uiState) {
                        is BookUiState.Success -> (uiState as BookUiState.Success).books
                        else -> emptyList()
                    }
                }

                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FilterSectionAdmin(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it }
                        )
                    }

                    when {
                        uiState is BookUiState.Loading && searchQuery.isEmpty() -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        uiState is BookUiState.Error -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (uiState as BookUiState.Error).message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        booksToDisplay.isEmpty() -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("Nenhum livro encontrado")
                                }
                            }
                        }
                        else -> {
                            items(booksToDisplay) { book ->
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
                }
            }
            "add" -> {
                AddEditBookScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentScreen = "list" },
                    onConfirm = { book ->
                        viewModel.addBook(book)
                    },
                    book = null
                )
            }
            "edit" -> {
                AddEditBookScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentScreen = "list" },
                    onConfirm = { book ->
                        viewModel.updateBook(book)
                    },
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
                        bookToModify?.let { book ->
                            viewModel.deleteBook(book.id)
                        }
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
fun AddEditBookScreen(
    modifier: Modifier = Modifier,
    book: Book?,
    onBack: () -> Unit,
    onConfirm: (Book) -> Unit
) {
    val isEditMode = book != null
    val viewModel: BookViewModel = viewModel()
    var title by remember { mutableStateOf(book?.title ?: "") }
    var author by remember { mutableStateOf(book?.author ?: "") }
    var year by remember { mutableStateOf(book?.year ?: "") }
    var category by remember { mutableStateOf(book?.category ?: "") }
    var isbn by remember { mutableStateOf(book?.isbn ?: "") }
    var isDigital by remember { mutableStateOf(book?.isDigital ?: false) }
    var isPhysical by remember { mutableStateOf(book?.isPhysical ?: true) }
    var physicalCopies by remember { mutableStateOf(book?.totalCopies?.toString() ?: "10") }
    var description by remember { mutableStateOf(book?.description ?: "") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageUrl by remember { mutableStateOf(book?.coverImageUrl ?: "") }
    val context = LocalContext.current

            // Launcher para seleção de imagem
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Se for modo de edição e já tiver um ID de livro, fazer upload imediatamente
            if (isEditMode && book.id.isNotEmpty()) {
                viewModel.uploadBookCover(context, book.id, it)
                Toast.makeText(context, "Fazendo upload da capa...", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                .clickable { imagePickerLauncher.launch("image/*") }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Mostrar imagem selecionada
                selectedImageUri != null -> {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Capa selecionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Mostrar imagem existente
                coverImageUrl.isNotEmpty() -> {
                    AsyncImage(
                        model = coverImageUrl,
                        contentDescription = "Capa do livro",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Mostrar placeholder
                else -> {
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
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o título do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o(a) autor(a) do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Autor(a)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o ano de publicação do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Ano") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite a categoria do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoria (ex: Romance, Ficção, História)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Digite o ISBN do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = isbn,
            onValueChange = { isbn = it },
            label = { Text("ISBN") },
            modifier = Modifier.fillMaxWidth()
        )
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
            OutlinedTextField(
                value = physicalCopies,
                onValueChange = { physicalCopies = it },
                label = { Text("Exemplares") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Digite a descrição do livro:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = title.isNotBlank() && author.isNotBlank()
            ) {
                Text(if (isEditMode) "+ Editar" else "+ Adicionar")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    if (isEditMode) "Tem certeza que deseja editar a obra do acervo?"
                    else "Tem certeza que deseja adicionar a obra ao acervo?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false

                    val copies = physicalCopies.toIntOrNull() ?: 0
                    // Tenta converter category para Int, se falhar usa 0
                    val categoryIdValue = category.toIntOrNull() ?: 0

                    val newBook = Book(
                        id = book?.id ?: "",
                        title = title,
                        author = author,
                        publicationYear = year.toIntOrNull() ?: 2024,
                        categoryId = categoryIdValue,
                        isbn = isbn,
                        description = description,
                        isDigital = isDigital,
                        totalCopies = copies,
                        availableCopies = book?.availableCopies ?: copies,
                        rating = book?.rating ?: 5,
                        coverImageUrl = book?.coverImageUrl ?: "",
                        digitalContentUrl = book?.digitalContentUrl ?: "",
                        createdAt = book?.createdAt,
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    onConfirm(newBook)

                    // Nota: Para adicionar upload de imagem em modo de adição,
                    // você precisa aguardar o callback de sucesso com o ID do livro criado
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
fun FilterSectionAdmin(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedAvailability by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedAvailability by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas", "Romance", "Ficção", "Não-ficção", "História", "Ciência", "Tecnologia", "Arte", "Biografia")
    val availabilityOptions = listOf("Todas", "Disponível", "Indisponível")

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Pesquisar por título, autor ou ISBN") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dropdown de Categoria
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Categoria") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expandedCategory = !expandedCategory }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCategory = !expandedCategory },
                    readOnly = true
                )
                DropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Dropdown de Disponibilidade
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedAvailability,
                    onValueChange = {},
                    label = { Text("Disponibilidade") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expandedAvailability = !expandedAvailability }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedAvailability = !expandedAvailability },
                    readOnly = true
                )
                DropdownMenu(
                    expanded = expandedAvailability,
                    onDismissRequest = { expandedAvailability = false }
                ) {
                    availabilityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedAvailability = option
                                expandedAvailability = false
                            }
                        )
                    }
                }
            }
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
            Icon(
                Icons.Default.Book,
                contentDescription = "Book Icon",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${book.author} - ${book.year}", color = Color.Gray, fontSize = 14.sp)
                Text(book.rating.toString(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(
                    book.getAvailabilityText(),
                    color = if (book.isAvailable()) Color(0xFF388E3C) else Color.Red,
                    fontSize = 12.sp
                )
            }
            Column {
                TextButton(onClick = onEditClick) { Text("Editar") }
                TextButton(onClick = onRemoveClick) { Text("Remover", color = Color.Red) }
            }
        }
    }
}

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
