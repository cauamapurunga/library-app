package com.example.uniforlibrary.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervo.AcervoActivity
import com.example.uniforlibrary.acervo.BookDetailActivity
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.components.UserBottomNav
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.model.Book
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.BookUiState
import com.example.uniforlibrary.viewmodel.BookViewModel

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel: BookViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

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
                            text = "Biblioteca Central",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
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
                    IconButton(onClick = { navigateToProfile(context) }) {
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
            UserBottomNav(context = context, selectedItemIndex = 0)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar Section
            SearchBarSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Access Section
            Text(
                text = "Acesso rápido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickAccessGrid(
                onAcervoClick = { context.startActivity(Intent(context, AcervoActivity::class.java)) },
                onEmprestimosClick = { context.startActivity(Intent(context, EmprestimosActivity::class.java)) },
                onReservationsClick = { context.startActivity(Intent(context, MyReservationsActivity::class.java)) },
                onProduzirClick = { context.startActivity(Intent(context, ProduzirActivity::class.java)) },
                onExposicoesClick = { context.startActivity(Intent(context, ExposicoesActivity::class.java)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Highlights Section
            Text(
                text = "Destaques",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState) {
                is BookUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BookUiState.Success -> {
                    val books = (uiState as BookUiState.Success).books
                    HighlightsSection(books = books, context = context)
                }
                is BookUiState.Error -> {
                    Text(
                        text = "Erro ao carregar livros em destaque",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SearchBarSection() {
    var searchQuery by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Biblioteca Universitária",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pesquise títulos, gêneros, empréstimos e acompanhe suas produções.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Pesquisar...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun QuickAccessGrid(
    onAcervoClick: () -> Unit,
    onEmprestimosClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onProduzirClick: () -> Unit,
    onExposicoesClick: () -> Unit
) {
    val quickAccessItems = listOf(
        QuickAccessItem("Consultar acervo", Icons.AutoMirrored.Filled.MenuBook, onAcervoClick),
        QuickAccessItem("Meus Empréstimos", Icons.Default.Book, onEmprestimosClick),
        QuickAccessItem("Reservas", Icons.Default.Bookmark, onReservationsClick),
        QuickAccessItem("Submeter\nProdução", Icons.Default.FileUpload, onProduzirClick),
        QuickAccessItem("Exposições\ndos alunos", Icons.Default.PhotoLibrary, onExposicoesClick)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(280.dp),
        userScrollEnabled = false
    ) {
        items(quickAccessItems) { item ->
            QuickAccessCard(item)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessCard(item: QuickAccessItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun HighlightsSection(books: List<Book>, context: Context) {
    Column {
        // Primeira linha - Livros mais populares (com melhor avaliação)
        Text(
            text = "📚 Livros mais populares",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        val popularBooks = try {
            books.sortedByDescending { it.rating }.take(5)
        } catch (e: Exception) {
            books.take(5)
        }

        if (popularBooks.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(popularBooks) { book ->
                    BookHighlightChip(book = book, context = context)
                }
            }
        } else {
            Text("Nenhum livro disponível", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Segunda linha - Novidades (livros mais recentes)
        Text(
            text = "🎄 Novidades",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        val newBooks = try {
            books.sortedByDescending { it.createdAt?.seconds ?: 0 }.take(5)
        } catch (e: Exception) {
            books.take(5)
        }

        if (newBooks.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(newBooks) { book ->
                    BookHighlightChip(book = book, context = context)
                }
            }
        } else {
            Text("Nenhum livro disponível", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terceira linha - Livros disponíveis
        Text(
            text = "📖 Disponíveis para empréstimo",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        val availableBooks = try {
            books.filter { it.isAvailable() }.take(5)
        } catch (e: Exception) {
            emptyList()
        }

        if (availableBooks.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableBooks) { book ->
                    BookHighlightChip(book = book, context = context)
                }
            }
        } else {
            Text("Nenhum livro disponível no momento", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookHighlightChip(book: Book, context: Context) {
    AssistChip(
        onClick = {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("BOOK_ID", book.id)
            context.startActivity(intent)
        },
        label = {
            Text(
                text = "${book.title} - ${book.author}",
                fontSize = 12.sp,
                maxLines = 1
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun HighlightRow(items: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            HighlightChip(text = item)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightChip(text: String) {
    AssistChip(
        onClick = { /* TODO: Abrir detalhes */ },
        label = {
            Text(
                text = text,
                fontSize = 12.sp,
                maxLines = 1
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, Color.LightGray)
    )
}


// Função para navegar para a tela de Perfil
private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

// --- Modelos de Dados ---

data class QuickAccessItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    UniforLibraryTheme {
        HomeScreen()
    }
}
