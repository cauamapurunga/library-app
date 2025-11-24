package com.example.uniforlibrary.notificacoes

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.model.Notification
import com.example.uniforlibrary.model.NotificationType
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.NotificationViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificacoesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                NotificacoesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(viewModel: NotificationViewModel = viewModel()) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Agrupar notificações por data
    val groupedNotifications = remember(notifications) {
        notifications.groupBy { notification ->
            getDateCategory(notification.createdAt)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notificações", fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount não lida${if (unreadCount > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Marcar todas como lidas") },
                            onClick = {
                                viewModel.markAllAsRead()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DoneAll, contentDescription = null)
                            },
                            enabled = unreadCount > 0
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir todas") },
                            onClick = {
                                showDeleteAllDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteForever, contentDescription = null)
                            },
                            enabled = notifications.isNotEmpty()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                notifications.isEmpty() -> {
                    EmptyNotificationsView()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        groupedNotifications.forEach { (dateCategory, notificationsList) ->
                            item {
                                DateHeader(dateCategory)
                            }

                            items(
                                items = notificationsList,
                                key = { it.id }
                            ) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onNotificationClick = {
                                        if (!notification.isRead) {
                                            viewModel.markAsRead(notification.id)
                                        }
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteNotification(notification.id)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Snackbar de erro
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }

    // Dialog de confirmação para excluir todas
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Excluir todas as notificações?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllNotifications()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val (icon, iconColor, iconBackground) = getNotificationStyle(notification.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onNotificationClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícone da notificação
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Conteúdo
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getTimeAgo(notification.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog de confirmação para excluir
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir notificação?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DateHeader(dateCategory: String) {
    Text(
        text = dateCategory,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma notificação",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Você está em dia com tudo! 🎉",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Funções auxiliares
private fun getNotificationStyle(type: String): Triple<ImageVector, Color, Color> {
    return when (type) {
        NotificationType.RESERVA_APROVADA -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF00C853),
            Color(0xFF00C853).copy(alpha = 0.1f)
        )
        NotificationType.RESERVA_REJEITADA -> Triple(
            Icons.Default.Cancel,
            Color(0xFFD32F2F),
            Color(0xFFD32F2F).copy(alpha = 0.1f)
        )
        NotificationType.NOVA_RESERVA -> Triple(
            Icons.Default.LibraryBooks,
            Color(0xFF1976D2),
            Color(0xFF1976D2).copy(alpha = 0.1f)
        )
        NotificationType.PRODUCAO_SUBMETIDA -> Triple(
            Icons.Default.Assignment,
            Color(0xFF7B1FA2),
            Color(0xFF7B1FA2).copy(alpha = 0.1f)
        )
        NotificationType.PRODUCAO_APROVADA -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF00C853),
            Color(0xFF00C853).copy(alpha = 0.1f)
        )
        NotificationType.PRODUCAO_REPROVADA -> Triple(
            Icons.Default.Cancel,
            Color(0xFFD32F2F),
            Color(0xFFD32F2F).copy(alpha = 0.1f)
        )
        NotificationType.LIVRO_ATRASADO -> Triple(
            Icons.Default.Warning,
            Color(0xFFFF6F00),
            Color(0xFFFF6F00).copy(alpha = 0.1f)
        )
        else -> Triple(
            Icons.Default.Notifications,
            Color(0xFF1976D2),
            Color(0xFF1976D2).copy(alpha = 0.1f)
        )
    }
}

private fun getTimeAgo(timestamp: Timestamp): String {
    val now = Date()
    val diff = now.time - timestamp.toDate().time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "Agora"
        minutes < 60 -> "${minutes}min"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timestamp.toDate())
    }
}

private fun getDateCategory(timestamp: Timestamp): String {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)

    calendar.time = timestamp.toDate()
    val notificationDay = calendar.get(Calendar.DAY_OF_YEAR)

    return when {
        today == notificationDay -> "Hoje"
        today - notificationDay == 1 -> "Ontem"
        today - notificationDay < 7 -> "Esta semana"
        else -> "Mais antigas"
    }
}
