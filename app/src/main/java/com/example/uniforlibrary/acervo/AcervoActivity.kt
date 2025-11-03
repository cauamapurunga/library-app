package com.example.uniforlibrary.acervo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.lifecycleScope
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.*
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import kotlinx.coroutines.launch
import java.util.Date

class AcervoActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()
    private val bookRepository = BookRepository()
    private val reservationRepository = ReservationRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadBooks()

        // Teste: Criar um livro e uma reserva
        lifecycleScope.launch {
            testCreateBookAndReservation()
        }

        setContent {
            UniforLibraryTheme {
                AcervoScreen(viewModel = viewModel)
            }
        }
    }

    private suspend fun testCreateBookAndReservation() {
        try {
            // 1. Criar um livro de teste
            val book = Book(
                title = "Clean Code",
                author = "Robert C. Martin",
                year = 2008,
                category = "Programação",
                description = "Um guia prático sobre como escrever código limpo",
                totalCopies = 5,
                availableCopies = 3,
                rating = 4.8,
                isDigital = true,
                isPhysical = true
            )

            val addBookResult = bookRepository.addBook(book)
            if (addBookResult.isSuccess) {
                val bookId = addBookResult.getOrNull()!!
                Toast.makeText(this, "Livro criado com ID: $bookId", Toast.LENGTH_LONG).show()

                // 2. Criar uma reserva para este livro
                val reservationResult = reservationRepository.createReservation(
                    bookId = bookId,
                    pickupDate = Date(), // Data atual
                    durationDays = 7,
                    observations = "Reserva de teste"
                )

                if (reservationResult.isSuccess) {
                    Toast.makeText(this, "Reserva criada com sucesso!", Toast.LENGTH_LONG).show()

                    // 3. Buscar todas as reservas do usuário
                    val userReservations = reservationRepository.getUserReservations()
                    if (userReservations.isSuccess) {
                        val reservations = userReservations.getOrNull()!!
                        Toast.makeText(this, "Você tem ${reservations.size} reservas", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Erro ao criar reserva: ${reservationResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Erro ao criar livro: ${addBookResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcervoScreen(viewModel: BookViewModel) {
    val context = LocalContext.current
    var selectedItemIndex by remember { mutableIntStateOf(1) }
    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )
    var searchQuery by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val books by viewModel.books.collectAsState()

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
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .height(80.dp)
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedItemIndex == item.index,
                            onClick = {
                                selectedItemIndex = item.index
                                when (item.index) {
                                    0 -> navigateToHome(context)
                                    1 -> { /* já está em Acervo */ }
                                    2 -> navigateToEmprestimos(context)
                                    3 -> navigateToReservations(context)
                                    4 -> navigateToProduzir(context)
                                    5 -> navigateToExposicoes(context)
                                }
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 9.sp,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 11.sp,
                                    fontWeight = if (selectedItemIndex == item.index)
                                        FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color(0xFF666666),
                                unselectedTextColor = Color(0xFF666666),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
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

            // Campo de busca com ação
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchBooks(it)
                },
                placeholder = { Text("Pesquisar por título, autor ou ISBN") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is BookUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is BookUiState.Error -> {
                    Text(
                        text = (uiState as BookUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    books.forEach { book ->
                        BookCard(
                            title = book.title,
                            subtitle = "${book.author} - ${book.year}",
                            rating = "★${book.rating}",
                            onReserveClick = {
                                val intent = Intent(context, BookDetailActivity::class.java)
                                intent.putExtra("bookId", book.id)
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(title: String, subtitle: String, rating: String, onReserveClick: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(rating, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Disponível",
                    color = Color(0xFF388E3C), // Green color
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onReserveClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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

private fun navigateToBookDetail(context: Context) {
    val intent = Intent(context, BookDetailActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToEmprestimos(context: Context) {
    val intent = Intent(context, EmprestimosActivity::class.java)
    context.startActivity(intent)
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AcervoScreenPreview() {
    UniforLibraryTheme {
        AcervoScreen(viewModel = BookViewModel())
    }
}
