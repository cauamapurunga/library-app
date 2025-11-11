package com.example.uniforlibrary.acervo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.uniforlibrary.R
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.model.Book
import com.example.uniforlibrary.model.BottomNavItem
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.repository.BookRepository
import com.example.uniforlibrary.repository.ReservationRepository
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class BookDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bookId = intent.getStringExtra("BOOK_ID") ?: ""
        setContent {
            UniforLibraryTheme {
                BookDetailScreen(
                    bookId = bookId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { BookRepository() }
    val reservationRepository = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var book by remember { mutableStateOf<Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedItemIndex by remember { mutableIntStateOf(1) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isCreatingReservation by remember { mutableStateOf(false) }

    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )

    // Carregar dados do livro
    LaunchedEffect(bookId) {
        android.util.Log.d("BookDetailActivity", "LaunchedEffect iniciado com bookId: '$bookId'")

        if (bookId.isNotEmpty()) {
            scope.launch {
                android.util.Log.d("BookDetailActivity", "Buscando livro com ID: $bookId")
                val result = repository.getBookById(bookId)

                result.onSuccess { loadedBook ->
                    android.util.Log.d("BookDetailActivity", "Livro carregado com sucesso: ${loadedBook?.title}")
                    if (loadedBook == null) {
                        android.util.Log.e("BookDetailActivity", "Livro é null mesmo com sucesso")
                        error = "Livro não encontrado no banco de dados"
                    } else {
                        book = loadedBook
                    }
                    isLoading = false
                }.onFailure { e ->
                    android.util.Log.e("BookDetailActivity", "Erro ao carregar livro", e)
                    error = "Erro ao carregar detalhes: ${e.message}"
                    isLoading = false
                }
            }
        } else {
            android.util.Log.e("BookDetailActivity", "bookId está vazio!")
            error = "ID do livro não foi fornecido"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultar Acervo", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notificações */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(80.dp)
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == item.index,
                        onClick = {
                            selectedItemIndex = item.index
                            when (item.index) {
                                0 -> navigateToHome(context)
                                1 -> navigateToAcervo(context)
                                3 -> navigateToReservations(context)
                                4 -> navigateToProduzir(context)
                            }
                        },
                        label = { Text(item.label, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 2) },
                        icon = { Icon(item.icon, contentDescription = item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null || book == null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Livro não encontrado",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Capa do livro
                    if (book!!.coverImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = book!!.coverImageUrl,
                            contentDescription = "Capa de ${book!!.title}",
                            modifier = Modifier
                                .width(180.dp)
                                .height(270.dp)
                                .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder se não houver capa
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Book Cover",
                            modifier = Modifier
                                .size(180.dp)
                                .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(book!!.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${book!!.author} ${book!!.rating}", fontSize = 16.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip(label = "Categoria", value = book!!.category.ifEmpty { "N/A" }, modifier = Modifier.weight(1f))
                        InfoChip(label = "Ano", value = book!!.year, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip(label = "Autor", value = book!!.author, modifier = Modifier.weight(1f))
                        InfoChip(
                            label = "Exemplares disp.",
                            value = "${book!!.availableCopies} de ${book!!.totalCopies}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = book!!.isAvailable()
                    ) {
                        Text("Reservar Livro", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSection(book = book!!)
                }

                if (showBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
                        ReservationBottomSheetContent(
                            book = book!!,
                            isCreatingReservation = isCreatingReservation,
                            onConfirm = {
                                // Criar reserva no Firebase
                                val currentUserId = auth.currentUser?.uid

                                if (currentUserId == null) {
                                    Toast.makeText(
                                        context,
                                        "Erro: Usuário não autenticado",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showBottomSheet = false
                                    return@ReservationBottomSheetContent
                                }

                                isCreatingReservation = true

                                scope.launch {
                                    android.util.Log.d(
                                        "BookDetailActivity",
                                        "Criando reserva para livro: ${book!!.id}, usuário: $currentUserId"
                                    )

                                    val result = reservationRepository.createReservation(
                                        bookId = book!!.id,
                                        userId = currentUserId
                                    )

                                    isCreatingReservation = false

                                    result.onSuccess { reservationId ->
                                        android.util.Log.d(
                                            "BookDetailActivity",
                                            "Reserva criada com sucesso: $reservationId"
                                        )
                                        showBottomSheet = false
                                        Toast.makeText(
                                            context,
                                            "Reserva criada com sucesso! Aguarde aprovação do administrador.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    result.onFailure { e ->
                                        android.util.Log.e(
                                            "BookDetailActivity",
                                            "Erro ao criar reserva",
                                            e
                                        )
                                        Toast.makeText(
                                            context,
                                            "Erro ao criar reserva: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            onCancel = { showBottomSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DetailSection(book: Book) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Descrição", "Avaliações")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTabIndex) {
            0 -> Text(
                book.description.ifEmpty { "Sem descrição disponível." },
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
            1 -> Text("Nenhuma avaliação disponível ainda.", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReservationBottomSheetContent(
    book: Book,
    isCreatingReservation: Boolean = false,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var date by remember { mutableStateOf("30/10/2025") }
    var plazo by remember { mutableStateOf("7") }
    var observations by remember { mutableStateOf("") }
    var agreedToPolicies by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(book.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("${book.author} ${book.rating}", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Data da retirada") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = plazo,
                onValueChange = { plazo = it },
                label = { Text("Prazo (dias)") },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = observations,
            onValueChange = { observations = it },
            label = { Text("Observações") },
            placeholder = { Text("Adicionar nota para a biblioteca") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "A reserva será mantida por 24h após a confirmação. Traga um documento com foto para a retirada.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Checkbox(checked = agreedToPolicies, onCheckedChange = { agreedToPolicies = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Concordo com as políticas de empréstimo e uso da biblioteca.", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = agreedToPolicies && !isCreatingReservation,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isCreatingReservation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criando...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar Reserva")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isCreatingReservation
        ) {
            Text("Cancelar")
        }
    }
}

// Navigation helpers
private fun navigateToHome(context: Context) {
    context.startActivity(Intent(context, HomeActivity::class.java))
}

private fun navigateToReservations(context: Context) {
    context.startActivity(Intent(context, MyReservationsActivity::class.java))
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

private fun navigateToProduzir(context: Context) {
    context.startActivity(Intent(context, ProduzirActivity::class.java))
}

private fun navigateToAcervo(context: Context) {
    context.startActivity(Intent(context, AcervoActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun BookDetailScreenPreview() {
    UniforLibraryTheme {
        BookDetailScreen(bookId = "", onBack = {})
    }
}
