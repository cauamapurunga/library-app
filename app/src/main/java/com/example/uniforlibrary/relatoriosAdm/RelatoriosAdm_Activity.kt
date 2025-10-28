package com.example.uniforlibrary.relatoriosAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervoAdm.AcervoAdm_Activity
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.exposicoesAdm.ExposicoesAdm_Activity
import com.example.uniforlibrary.homeAdm.HomeAdm_Activity
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservasAdm.ReservasADM_activity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class RelatoriosAdm_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                RelatoriosAdmScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosAdmScreen() {
    val context = LocalContext.current
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
                        Text("Relatórios", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, NotificacoesActivity::class.java)) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            AdminBottomNav(context = context, selectedItemIndex = 4)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DateFilters()
            MonthlyMetrics()
            PopularBooks()
        }
    }
}

@Composable
fun DateFilters() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Data inicial") },
            leadingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.weight(1f),
            readOnly = true
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Data final") },
            leadingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.weight(1f),
            readOnly = true
        )
    }
}

@Composable
fun MonthlyMetrics() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Métricas do Mês", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(title = "Total de Reservas", value = "3", change = "+300% vs mês anterior", modifier = Modifier.weight(1f))
            MetricCard(title = "Usuários Ativos", value = "1", change = "+100% vs mês anterior", modifier = Modifier.weight(1f))
            MetricCard(title = "Taxa de Devolução", value = "100%", change = "+0% vs mês anterior", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, change: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(change, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PopularBooks() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Livros Mais Populares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PopularBookItem(rank = 1, title = "PathExileLORE")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                PopularBookItem(rank = 2, title = "WOW")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                PopularBookItem(rank = 3, title = "How to build you upper/lower exercises")
            }
        }
    }
}

@Composable
fun PopularBookItem(rank: Int, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}


@Composable
fun AdminBottomNav(context: Context, selectedItemIndex: Int) {
    val navigationItems = listOf(
        AdminBottomNavItem("Home", Icons.Default.Home, 0) { context.startActivity(Intent(context, HomeAdm_Activity::class.java)) },
        AdminBottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1) { context.startActivity(Intent(context, AcervoAdm_Activity::class.java)) },
        AdminBottomNavItem("Exposições", Icons.Default.PhotoLibrary, 2) { context.startActivity(Intent(context, ExposicoesAdm_Activity::class.java)) },
        AdminBottomNavItem("Reservas", Icons.Default.Bookmark, 3) { context.startActivity(Intent(context, ReservasADM_activity::class.java)) },
        AdminBottomNavItem("Relatórios", Icons.Default.Assessment, 4) { /* Already here */ }
    )

    Surface(tonalElevation = 0.dp, shadowElevation = 16.dp, color = Color.White) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp).padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            navigationItems.forEach { item ->
                NavigationBarItem(
                    selected = selectedItemIndex == item.index,
                    onClick = item.onClick,
                    label = { Text(item.label, fontSize = 9.sp, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 11.sp, fontWeight = if (selectedItemIndex == item.index) FontWeight.Bold else FontWeight.Medium) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp)) },
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
}

data class AdminBottomNavItem(val label: String, val icon: ImageVector, val index: Int, val onClick: () -> Unit)

@Preview(showBackground = true)
@Composable
fun RelatoriosAdmScreenPreview() {
    UniforLibraryTheme {
        RelatoriosAdmScreen()
    }
}
