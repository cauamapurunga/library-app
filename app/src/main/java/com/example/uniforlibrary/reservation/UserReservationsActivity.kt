package com.example.uniforlibrary.reservation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.R
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.viewmodel.ReservationUiState
import com.example.uniforlibrary.viewmodel.ReservationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity para o usuário visualizar suas próprias reservas
 */
class UserReservationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                UserReservationsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReservationsScreen(viewModel: ReservationViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ativas", "Histórico")

    // Observar estado da UI
    val uiState by viewModel.uiState.collectAsState()
    val allReservations by viewModel.allReservations.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    // Dialog states
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedReservation by remember { mutableStateOf<Reservation?>(null) }

    // Carregar reservas do usuário
    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            // Note: Aqui você precisaria adicionar um método no ViewModel
            // para carregar apenas as reservas do usuário atual
            viewModel.loadAllReservations()
        }
    }

    // Mostrar mensagens de feedback
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // Filtrar apenas reservas do usuário atual
    val userReservations = remember(allReservations, currentUserId) {
        allReservations.filter { it.userId == currentUserId }
    }

    // Filtrar por tab
    val filteredReservations = remember(selectedTabIndex, userReservations) {
        when (selectedTabIndex) {
            0 -> userReservations.filter {
                it.status in listOf("Pendente", "Aprovada")
            }
            1 -> userReservations.filter {
                it.status in listOf("Retirado", "Rejeitada", "Expirada", "Cancelada")
            }
            else -> userReservations
        }
    }

    if (showCancelDialog && selectedReservation != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar Reserva") },
            text = {
                Text("Tem certeza que deseja cancelar a reserva do livro '${selectedReservation?.bookTitle}'?")
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedReservation?.let { reservation ->
                        scope.launch {
                            // Aqui você chamaria o método de cancelamento
                            // viewModel.cancelReservation(reservation.id)
                        }
                    }
                    showCancelDialog = false
                }) { Text("Sim") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Não") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_branca),
                            contentDescription = "Logo",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Minhas Reservas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to notifications */ }) {
                        Icon(Icons.Default.Notifications, "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { /* TODO: Navigate to profile */ }) {
                        Icon(Icons.Default.Person, "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            // Conteúdo
            when (uiState) {
                is ReservationUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ReservationUiState.Empty -> {
                    EmptyStateMessage(selectedTabIndex)
                }
                is ReservationUiState.Error -> {
                    ErrorStateMessage(
                        message = (uiState as ReservationUiState.Error).message,
                        onRetry = { viewModel.loadAllReservations() }
                    )
                }
                is ReservationUiState.Success -> {
                    if (filteredReservations.isEmpty()) {
                        EmptyStateMessage(selectedTabIndex)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(filteredReservations, key = { it.id }) { reservation ->
                                UserReservationCard(
                                    reservation = reservation,
                                    onCancel = {
                                        selectedReservation = reservation
                                        showCancelDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserReservationCard(
    reservation: Reservation,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val requestDate = dateFormat.format(reservation.requestDate.toDate())
    val expirationDate = reservation.expirationDate?.let { dateFormat.format(it.toDate()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Book Icon
            Surface(
                modifier = Modifier
                    .width(70.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Informações
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    reservation.bookTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                Text(
                    reservation.bookAuthor,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Data
                Text(
                    "Solicitado em: $requestDate",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Status
                UserStatusTag(reservation.status, expirationDate)

                // Botão de cancelar (apenas para Pendente e Aprovada)
                if (reservation.status in listOf("Pendente", "Aprovada")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar Reserva", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UserStatusTag(status: String, expirationDate: String?) {
    val (text, color, icon) = when (status) {
        "Pendente" -> Triple(
            "Aguardando aprovação",
            Color(0xFFFFA000),
            Icons.Default.Schedule
        )
        "Aprovada" -> Triple(
            "Aprovada - Retire até $expirationDate",
            Color(0xFF388E3C),
            Icons.Default.CheckCircle
        )
        "Retirado" -> Triple(
            "Livro retirado",
            MaterialTheme.colorScheme.primary,
            Icons.AutoMirrored.Filled.LibraryBooks
        )
        "Rejeitada" -> Triple(
            "Rejeitada",
            Color(0xFFD32F2F),
            Icons.Default.Cancel
        )
        "Expirada" -> Triple(
            "Prazo expirado",
            Color(0xFFD32F2F),
            Icons.Default.AccessTime
        )
        "Cancelada" -> Triple(
            "Cancelada",
            Color.Gray,
            Icons.Default.Cancel
        )
        else -> Triple(status, Color.Gray, Icons.Default.Info)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyStateMessage(tabIndex: Int) {
    val message = when (tabIndex) {
        0 -> "Você não tem reservas ativas"
        1 -> "Você não tem histórico de reservas"
        else -> "Nenhuma reserva encontrada"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Text(
                message,
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorStateMessage(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Red
            )
            Text(
                "Erro ao carregar reservas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Text(
                message,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Button(onClick = onRetry) {
                Text("Tentar novamente")
            }
        }
    }
}
