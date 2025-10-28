package com.example.uniforlibrary.homeAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervoAdm.AcervoAdm_Activity
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.exposicoesAdm.ExposicoesAdm_Activity
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.relatoriosAdm.RelatoriosAdm_Activity
import com.example.uniforlibrary.reservasAdm.ReservasADM_activity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class HomeAdm_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                HomeAdmScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdmScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Image(painter = painterResource(id = R.drawable.logo_branca), contentDescription = "Logo Unifor", modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Biblioteca Central", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { navigateToNotificacoes(context) }) {
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
            AdminBottomNav(context = context, selectedItemIndex = 0)
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
                .padding(16.dp)
        ) {
            SearchBarAdmin()
            Spacer(modifier = Modifier.height(24.dp))
            QuickAccessAdmin(context)
            Spacer(modifier = Modifier.height(24.dp))
            MetricsSection()
        }
    }
}

@Composable
fun SearchBarAdmin() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Biblioteca Universitária", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Pesquise títulos, gerencie empréstimos e acompanhe os relatórios.", fontSize = 12.sp, lineHeight = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Search, contentDescription = "Pesquisar", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pesquisar", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuickAccessAdmin(context: Context) {
    Column {
        Text("Acesso rápido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickAccessButton(text = "Gerenciar acervo", modifier = Modifier.weight(1f), onClick = { navigateToAcervoAdm(context) })
            QuickAccessButton(text = "Gerenciar Produções", modifier = Modifier.weight(1f), onClick = { navigateToExposicoesAdm(context) })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickAccessButton(text = "Reservas", modifier = Modifier.weight(1f), onClick = { navigateToReservasAdm(context) })
            QuickAccessButton(text = "Relatórios", modifier = Modifier.weight(1f), onClick = { navigateToRelatoriosAdm(context) })
        }
    }
}

@Composable
fun QuickAccessButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}

@Composable
fun MetricsSection() {
    Column {
        Text("Visão rápida de métricas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricCircle(value = "342", label = "Empréstimos (30 dias)")
            MetricCircle(value = "89", label = "Reservas ativas")
        }
    }
}

@Composable
fun MetricCircle(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

// --- Modelos e Navegação ---
private fun navigateToAcervoAdm(context: Context) {
    context.startActivity(Intent(context, AcervoAdm_Activity::class.java))
}

private fun navigateToExposicoesAdm(context: Context) {
    context.startActivity(Intent(context, ExposicoesAdm_Activity::class.java))
}

private fun navigateToReservasAdm(context: Context) {
    context.startActivity(Intent(context, ReservasADM_activity::class.java))
}

private fun navigateToRelatoriosAdm(context: Context) {
    context.startActivity(Intent(context, RelatoriosAdm_Activity::class.java))
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

private fun navigateToNotificacoes(context: Context) {
    context.startActivity(Intent(context, NotificacoesActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun HomeAdmScreenPreview() {
    UniforLibraryTheme {
        HomeAdmScreen()
    }
}
