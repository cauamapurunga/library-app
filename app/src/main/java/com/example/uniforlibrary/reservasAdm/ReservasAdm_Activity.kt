package com.example.uniforlibrary.reservasAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.BadgeBox
import com.example.uniforlibrary.model.Reservation
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.NotificationViewModel
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
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
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
                            contentDescription = "Logo Unifor",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gerenciar Reservas", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, NotificacoesActivity::class.java)) }) {
                        BadgeBox(count = unreadCount) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            AdminBottomNav(context = context, selectedItemIndex = 3)
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Book Cover Image
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                ) {
                    if (reservation.bookCoverUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(reservation.bookCoverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Capa do livro ${reservation.bookTitle}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Informações da Reserva
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Título do Livro
                    Text(
                        reservation.bookTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        lineHeight = 18.sp
                    )

                    // Autor
                    Text(
                        reservation.bookAuthor,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Informações do Aluno
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PersonOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            reservation.userName,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    // Matrícula
                    Text(
                        "Matrícula: ${reservation.userMatricula}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Data de Solicitação
                    Text(
                        "Solicitado em: $requestDate",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    // Status
                    StatusTag(reservation.status)
                }
            }

            // Área de Ações (separada para melhor organização)
            if (reservation.status != "Retirado" && reservation.status != "Rejeitada") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    AdminActionButtons(
                        reservation = reservation,
                        viewModel = viewModel,
                        expirationDate = expirationDate,
                        openConfirmationDialog = openConfirmationDialog
                    )
                }
            } else {
                // Para status finalizados, mostrar informações extras
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (reservation.status == "Retirado") {
                            Text(
                                "✓ Retirado em: ${reservation.withdrawalDate?.let { 
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.toDate()) 
                                } ?: "N/A"}",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (reservation.status == "Rejeitada") {
                            Column {
                                Text(
                                    "✗ Rejeitada",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold
                                )
                                if (reservation.rejectionReason != null) {
                                    Text(
                                        "Motivo: ${reservation.rejectionReason}",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val (text, color) = when (status) {
        "Pendente" -> "Aprovação pendente" to Color(0xFFFFA000) // Laranja
        "Aprovada" -> "Aprovada - Aguardando usuário" to Color(0xFF388E3C) // Verde
        "Aguardando Retirada" -> "Usuário confirmou - Pronto para retirar" to Color(0xFF1976D2) // Azul
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
    when (reservation.status) {
        "Pendente" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Rejeitar", fontSize = 12.sp)
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
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Aprovar", fontSize = 12.sp)
                }
            }
        }
        "Aprovada" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Aguardando aluno confirmar retirada",
                            fontSize = 11.sp,
                            color = Color(0xFFFFA000),
                            fontWeight = FontWeight.Medium
                        )
                        if (expirationDate != null) {
                            Text(
                                "Expira em: $expirationDate",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        "Aguardando Retirada" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Aluno confirmou! Pronto para entregar",
                            fontSize = 11.sp,
                            color = Color(0xFF388E3C),
                            fontWeight = FontWeight.Bold
                        )
                        if (expirationDate != null) {
                            Text(
                                "Expira em: $expirationDate",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            openConfirmationDialog(
                                "Marcar como Retirada",
                                "Confirmar a RETIRADA do livro '${reservation.bookTitle}' pelo aluno ${reservation.userName}?\n\nApós confirmar, o empréstimo será criado automaticamente."
                            ) {
                                viewModel.markAsWithdrawn(reservationId = reservation.id)
                            }
                        },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Confirmar Retirada", fontSize = 11.sp)
                    }
                }
            }
        }
        "Expirada" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Prazo expirado em ${expirationDate ?: "N/A"}",
                    fontSize = 11.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = {
                        openConfirmationDialog(
                            "Contactar Aluno",
                            "Deseja notificar ${reservation.userName} sobre o prazo expirado?"
                        ) {
                            // TODO: Implementar lógica de notificação
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Contactar", fontSize = 11.sp)
                }
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
