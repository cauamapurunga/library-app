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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.components.UserBottomNav
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.UserReservationViewModel
import com.example.uniforlibrary.viewmodel.UserReservationUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MyReservationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                MyReservationsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(viewModel: UserReservationViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Disponíveis", "Aguardando", "Devolvidos")

    // Observar estado da UI
    val uiState by viewModel.uiState.collectAsState()
    val userReservations by viewModel.userReservations.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    // Mostrar mensagens de feedback
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // Filtrar reservas baseado na tab selecionada
    val filteredReservations = remember(selectedTabIndex, userReservations) {
        viewModel.getReservationsByDisplayStatus(tabs[selectedTabIndex])
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Image(painter = painterResource(id = R.drawable.logo_branca), contentDescription = "Logo Unifor", modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Minhas Reservas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            UserBottomNav(
                context = context,
                selectedItemIndex = 3
            )
        },
        floatingActionButton = {
            Chatbot(context = context)
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            // Exibir conteúdo baseado no estado
            when (uiState) {
                is UserReservationUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UserReservationUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (uiState as UserReservationUiState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is UserReservationUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Nenhuma reserva encontrada",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                is UserReservationUiState.Success -> {
                    if (filteredReservations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma reserva com este status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(filteredReservations) { reservation ->
                                ReservationCard(
                                    reservation = reservation,
                                    dateFormatter = dateFormatter,
                                    viewModel = viewModel
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
fun ReservationCard(
    reservation: Reservation,
    dateFormatter: SimpleDateFormat,
    viewModel: UserReservationViewModel
) {
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    val displayStatus = viewModel.getDisplayStatus(reservation)
    val canCancel = viewModel.canCancelReservation(reservation)
    val canWithdraw = viewModel.canConfirmWithdrawal(reservation)
    val isWaitingForAdmin = viewModel.isWaitingForAdminPickup(reservation)
    val requestDate = dateFormatter.format(reservation.requestDate.toDate())

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    "Confirmação",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Tem certeza que quer cancelar a sua reserva?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        scope.launch {
                            viewModel.cancelReservation(reservation.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sim, Cancelar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text("Não")
                }
            }
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Text(
                    "Confirmar Retirada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Você confirma que vai retirar este livro na biblioteca? Após a confirmação, o livro estará disponível para você retirar no balcão.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWithdrawDialog = false
                        scope.launch {
                            viewModel.confirmWithdrawal(reservation.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sim, Vou Retirar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showWithdrawDialog = false }
                ) {
                    Text("Não")
                }
            }
        )
    }

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
            // Book Cover Image
            if (reservation.bookCoverUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(reservation.bookCoverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Capa do livro ${reservation.bookTitle}",
                    modifier = Modifier
                        .width(70.dp)
                        .height(100.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback para quando não há imagem
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
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Título e autor
                Text(
                    text = reservation.bookTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )

                Text(
                    text = reservation.bookAuthor,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Data e Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = requestDate,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (reservation.status) {
                                "Pendente" -> Color(0xFFFFF3CD)
                                "Aprovada" -> Color(0xFFD1E7DD)
                                "Aguardando Retirada" -> Color(0xFFCFE2FF)
                                "Rejeitada" -> Color(0xFFF8D7DA)
                                "Retirado" -> Color(0xFFD1E7DD)
                                "Expirada" -> Color(0xFFF8D7DA)
                                "Cancelada" -> Color(0xFFCFD8DC)
                                else -> Color.LightGray
                            }
                        ) {
                            Text(
                                text = displayStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (reservation.status) {
                                    "Pendente" -> Color(0xFF856404)
                                    "Aprovada" -> Color(0xFF0F5132)
                                    "Aguardando Retirada" -> Color(0xFF084298)
                                    "Rejeitada" -> Color(0xFF842029)
                                    "Retirado" -> Color(0xFF0F5132)
                                    "Expirada" -> Color(0xFF842029)
                                    "Cancelada" -> Color(0xFF455A64)
                                    else -> Color.DarkGray
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))


                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    when {
                        // Botão "Retirar" - Quando aprovado pelo admin
                        canWithdraw -> {
                            Button(
                                onClick = { showWithdrawDialog = true },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retirar", fontSize = 11.sp)
                            }
                        }

                        // Aguardando admin marcar como retirado
                        isWaitingForAdmin -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFCFE2FF),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF084298)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "No balcão",
                                        fontSize = 11.sp,
                                        color = Color(0xFF084298),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Botão "Cancelar" - Quando ainda pode cancelar
                        canCancel -> {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFD32F2F)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancelar", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

