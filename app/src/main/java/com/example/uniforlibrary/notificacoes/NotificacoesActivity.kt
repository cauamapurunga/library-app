package com.example.uniforlibrary.notificacoes

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class NotificacoesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificacoesScreen()
        }
    }
}

data class Notificacao(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isUnread: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen() {
    val context = LocalContext.current
    val notifications = listOf(
        Notificacao(
            id = 1,
            title = "Devolução atrasada",
            description = "O livro \"PathExileLORE\" deve ser devolvido imediatamente",
            time = "2h",
            icon = Icons.Default.Schedule,
            iconColor = MaterialTheme.colorScheme.error,
            isUnread = true
        ),
        Notificacao(
            id = 2,
            title = "Reserva confirmada",
            description = "Sua reserva do livro \"How to build you upper/lower exercises\" foi confirmada",
            time = "2h",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF00C853), // A nice green
            isUnread = false
        )
    )

    UniforLibraryTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notificações") },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificações", style = MaterialTheme.typography.titleLarge)
                    Text("1 nova", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification = notification)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notificacao) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = notification.icon,
            contentDescription = null,
            tint = notification.iconColor,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = notification.time, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.description, fontSize = 14.sp, color = Color.Gray, lineHeight = 18.sp)
        }
        if (notification.isUnread) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificacoesScreenPreview() {
    UniforLibraryTheme {
        NotificacoesScreen()
    }
}
