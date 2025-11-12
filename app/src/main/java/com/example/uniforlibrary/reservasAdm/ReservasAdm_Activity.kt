package com.example.uniforlibrary.reservasAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.ReservationUiState
import com.example.uniforlibrary.viewmodel.ReservationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReservasADM_activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ReservasADMScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasADMScreen(viewModel: ReservationViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Pendentes", "Aprovados", "Retirados")

    // Observar estado da UI
    val uiState by viewModel.uiState.collectAsState()
    val allReservations by viewModel.allReservations.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    // Dialog states
    var showDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf<() -> Unit>({}) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogText by remember { mutableStateOf("") }

    // Verificar reservas expiradas ao iniciar
    LaunchedEffect(Unit) {
        viewModel.checkAndExpireReservations()
    }

    // Mostrar mensagens de feedback
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val openConfirmationDialog = { title: String, text: String, onConfirm: () -> Unit ->
        dialogTitle = title
        dialogText = text
        dialogAction = onConfirm
        showDialog = true
    }

    // Filtrar reservas baseado na tab selecionada
    val filteredReservations = remember(selectedTabIndex, allReservations) {
        viewModel.getReservationsByStatus(tabs[selectedTabIndex])
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            confirmButton = {
                TextButton(onClick = {
                    dialogAction()
                    showDialog = false
                }) { Text("Sim") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Não") }
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
                            "Reservas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, NotificacoesActivity::class.java))
                    }) {
                        Icon(Icons.Default.Notifications, "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(Icons.Default.Person, "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            AdminBottomNav(context = context, selectedItemIndex = 3)
        },
        floatingActionButton = {
            Chatbot(context = context)
        },
        floatingActionButtonPosition = FabPosition.Start,
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
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            // Conteúdo baseado no estado
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma reserva encontrada",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                is ReservationUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Erro ao carregar reservas",
                                fontSize = 16.sp,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (uiState as ReservationUiState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadAllReservations() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }
                is ReservationUiState.Success -> {
                    if (filteredReservations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhuma reserva nesta categoria",
                                fontSize = 16.sp,
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
                            items(filteredReservations, key = { it.id }) { reservation ->
                                AdminReservationCard(
                                    reservation = reservation,
                                    viewModel = viewModel,
                                    openConfirmationDialog = openConfirmationDialog
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
fun AdminReservationCard(
    reservation: Reservation,
    viewModel: ReservationViewModel,
    openConfirmationDialog: (String, String, () -> Unit) -> Unit
) {
    // Formatar datas
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
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Informações da Reserva
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    reservation.bookTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
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

                // Info do Aluno
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PersonOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${reservation.userName} - Mat: ${reservation.userMatricula}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Data da Reserva
                Text(
                    "Solicitado em: $requestDate",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                // Status da Reserva
                StatusTag(reservation.status)
                Spacer(modifier = Modifier.height(4.dp))

                // Botões de Ação do ADM
                AdminActionButtons(
                    reservation = reservation,
                    viewModel = viewModel,
                    expirationDate = expirationDate,
                    openConfirmationDialog = openConfirmationDialog
                )
            }
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val (text, color) = when (status) {
        "Pendente" -> "Aprovação pendente" to Color(0xFFFFA000) // Laranja
        "Aprovada" -> "Aprovada - Aguardando retirada" to Color(0xFF388E3C) // Verde
        "Expirada" -> "Prazo Expirado" to Color(0xFFD32F2F) // Vermelho
        "Retirado" -> "Livro Retirado" to MaterialTheme.colorScheme.primary // Azul
        "Rejeitada" -> "Rejeitada" to Color(0xFFD32F2F) // Vermelho
        else -> status to Color.Gray
    }
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
}

@Composable
fun AdminActionButtons(
    reservation: Reservation,
    viewModel: ReservationViewModel,
    expirationDate: String?,
    openConfirmationDialog: (String, String, () -> Unit) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (reservation.status) {
            "Pendente" -> {
                OutlinedButton(
                    onClick = {
                        openConfirmationDialog(
                            "Rejeitar Reserva",
                            "Tem certeza que deseja REJEITAR esta reserva do livro '${reservation.bookTitle}' para ${reservation.userName}?"
                        ) {
                            viewModel.rejectReservation(
                                reservationId = reservation.id,
                                reason = "Rejeitada pelo administrador"
                            )
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Rejeitar", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        openConfirmationDialog(
                            "Aprovar Reserva",
                            "Tem certeza que deseja APROVAR esta reserva? O aluno terá 7 dias para retirar o livro."
                        ) {
                            viewModel.approveReservation(reservationId = reservation.id)
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Aprovar", fontSize = 11.sp)
                }
            }
            "Aprovada" -> {
                Column(horizontalAlignment = Alignment.End) {
                    if (expirationDate != null) {
                        Text(
                            "Expira em: $expirationDate",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = {
                            openConfirmationDialog(
                                "Marcar como Retirada",
                                "Confirmar a RETIRADA do livro '${reservation.bookTitle}' pelo aluno ${reservation.userName}?"
                            ) {
                                viewModel.markAsWithdrawn(reservationId = reservation.id)
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Marcar como retirada", fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            "Expirada" -> {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Prazo expirado em ${expirationDate ?: "N/A"}",
                        fontSize = 10.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            openConfirmationDialog(
                                "Contactar Aluno",
                                "Deseja notificar ${reservation.userName} sobre o prazo expirado?"
                            ) {
                                // TODO: Implementar lógica de notificação
                            }
                        }
                    ) {
                        Text("Contactar aluno", fontSize = 11.sp)
                    }
                }
            }
            "Retirado" -> {
                Text(
                    "Retirado em: ${reservation.withdrawalDate?.let { 
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.toDate()) 
                    } ?: "N/A"}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            "Rejeitada" -> {
                Text(
                    "Rejeitada",
                    fontSize = 10.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun ReservasADMScreenPreview() {
    UniforLibraryTheme {
        ReservasADMScreen()
    }
}
