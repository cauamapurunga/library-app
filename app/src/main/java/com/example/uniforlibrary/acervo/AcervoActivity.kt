package com.example.uniforlibrary.acervo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.components.UserBottomNav
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.model.Book
import com.example.uniforlibrary.model.BottomNavItem
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.BookUiState
import com.example.uniforlibrary.viewmodel.BookViewModel

class AcervoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                AcervoScreen()
            }
        }
    }
}

@Composable
private fun FilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedAvailability: String,
    onAvailabilityChange: (String) -> Unit
) {
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedAvailability by remember { mutableStateOf(false) }

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
                    readOnly = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                DropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategoryChange(category)
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
                    readOnly = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                DropdownMenu(
                    expanded = expandedAvailability,
                    onDismissRequest = { expandedAvailability = false }
                ) {
                    availabilityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onAvailabilityChange(option)
                                expandedAvailability = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcervoScreen() {
    val context = LocalContext.current
    val viewModel: BookViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedAvailability by remember { mutableStateOf("Todas") }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_branca),
                            contentDescription = "Logo Unifor",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Acervo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, NotificacoesActivity::class.java)) }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificações",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            UserBottomNav(context = context, selectedItemIndex = 1)
        },
        floatingActionButton = {
             Chatbot(context = context)
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Acervo",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            FilterSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it },
                selectedAvailability = selectedAvailability,
                onAvailabilityChange = { selectedAvailability = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar resultados baseado no estado com filtros aplicados
            val booksToDisplay = remember(searchQuery, selectedCategory, selectedAvailability, uiState, searchResults) {
                // Primeiro, determina a lista base
                val baseBooks = if (searchQuery.isNotEmpty()) {
                    searchResults
                } else {
                    when (uiState) {
                        is BookUiState.Success -> (uiState as BookUiState.Success).books
                        else -> emptyList()
                    }
                }

                // Depois aplica os filtros
                baseBooks.filter { book ->
                    val matchesCategory = selectedCategory == "Todas" || book.category == selectedCategory
                    val matchesAvailability = when (selectedAvailability) {
                        "Disponível" -> book.isAvailable()
                        "Indisponível" -> !book.isAvailable()
                        else -> true // "Todas"
                    }
                    matchesCategory && matchesAvailability
                }
            }

            when {
                uiState is BookUiState.Loading && searchQuery.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState is BookUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as BookUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                booksToDisplay.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Nenhum livro encontrado")
                            if (searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tente buscar por outro termo",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(booksToDisplay) { book ->
                            BookCard(
                                book = book,
                                onReserveClick = {
                                    android.util.Log.d("AcervoActivity", "Clicou em reservar livro: ${book.title}, ID: ${book.id}")
                                    navigateToBookDetail(context, book.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(book: Book, onReserveClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Capa do livro
            if (book.coverImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = "Capa de ${book.title}",
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder se não houver capa
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = "Sem capa",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${book.author} - ${book.year}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        book.rating.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (book.isAvailable()) "Disponível" else "Indisponível",
                    color = if (book.isAvailable()) Color(0xFF388E3C) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onReserveClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = book.isAvailable()
            ) {
                Text("Reservar")
            }
        }
    }
}

@Composable
fun AddEditContent(title: String, onBack: () -> Unit, onConfirm: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Ano") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = false, onCheckedChange = {})
            Spacer(modifier = Modifier.width(8.dp))
            Text("Digital")
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(checked = false, onCheckedChange = {})
            Spacer(modifier = Modifier.width(8.dp))
            Text("Físico")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Voltar")
            }
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text(if (title.contains("Editar")) "Confirmar" else "Adicionar")
            }
        }
    }
}

// Navigation helpers
private fun navigateToHome(context: Context) {
    val intent = Intent(context, HomeActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToReservations(context: Context) {
    val intent = Intent(context, MyReservationsActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToProfile(context: Context) {
    val intent = Intent(context, EditProfileActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToProduzir(context: Context) {
    val intent = Intent(context, ProduzirActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToAcervo(context: Context) {
    val intent = Intent(context, AcervoActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToExposicoes(context: Context) {
    val intent = Intent(context, ExposicoesActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToBookDetail(context: Context, bookId: String = "") {
    val intent = Intent(context, BookDetailActivity::class.java)
    intent.putExtra("BOOK_ID", bookId)
    context.startActivity(intent)
}

private fun navigateToEmprestimos(context: Context) {
    val intent = Intent(context, EmprestimosActivity::class.java)
    context.startActivity(intent)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AcervoScreenPreview() {
    UniforLibraryTheme {
        AcervoScreen()
    }
}
