package com.example.uniforlibrary.exposicoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import com.example.uniforlibrary.acervo.AcervoActivity
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class ExposicoesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ExposicoesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposicoesScreen() {
    val context = LocalContext.current
    var selectedItemIndex by remember { mutableIntStateOf(5) } // Exposições selecionado
    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )

    val exhibitions = listOf(
        Exhibition("PathExileLORE", "Narak - 2020", "★5", true),
        Exhibition("WOW", "Aliens - 1977", "★4.8", false),
        Exhibition("How to build you upper/lower exercises", "JohnDoe - 2025", "★4.1", false)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Image(painter = painterResource(id = R.drawable.logo_branca), contentDescription = "Logo Unifor", modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Exposições", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            Surface(tonalElevation = 0.dp, shadowElevation = 16.dp, color = Color.White) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(80.dp).padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedItemIndex == item.index,
                            onClick = {
                                selectedItemIndex = item.index
                                when (item.index) {
                                    0 -> navigateToHome(context)
                                    1 -> navigateToAcervo(context)
                                    2 -> navigateToEmprestimos(context)
                                    3 -> navigateToReservations(context)
                                    4 -> navigateToProduzir(context)
                                    5 -> { /* Já está em Exposições */ }
                                }
                            },
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
        },
        floatingActionButton = {
            Chatbot(context = context)
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FilterSection()
            }
            items(exhibitions) { exhibition ->
                ExhibitionCard(exhibition, onReserveClick = { navigateToExhibitionDetail(context) })
            }
        }
    }
}

@Composable
fun FilterSection() {
    Column {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Pesquisar por título, autor ou categoria") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Categoria") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Disponibilidade") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }
    }
}

@Composable
fun ExhibitionCard(exhibition: Exhibition, onReserveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = "Book Icon", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exhibition.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(exhibition.author, color = Color.Gray, fontSize = 14.sp)
                Text(exhibition.rating, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (exhibition.isAvailable) "Disponível" else "Emprestado",
                    color = if (exhibition.isAvailable) Color(0xFF388E3C) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onReserveClick,
                    enabled = exhibition.isAvailable,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Reservar", fontSize = 12.sp)
                }
            }
        }
    }
}

// --- Modelos e Navegação ---
data class Exhibition(val title: String, val author: String, val rating: String, val isAvailable: Boolean)
data class BottomNavItem(val label: String, val icon: ImageVector, val index: Int)

private fun navigateToHome(context: Context) {
    context.startActivity(Intent(context, HomeActivity::class.java))
}
private fun navigateToAcervo(context: Context) {
    context.startActivity(Intent(context, AcervoActivity::class.java))
}
private fun navigateToReservations(context: Context) {
    context.startActivity(Intent(context, MyReservationsActivity::class.java))
}
private fun navigateToProduzir(context: Context) {
    context.startActivity(Intent(context, ProduzirActivity::class.java))
}
private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}
private fun navigateToExhibitionDetail(context: Context) {
    context.startActivity(Intent(context, ExhibitionDetailActivity::class.java))
}
private fun navigateToEmprestimos(context: Context) {
    context.startActivity(Intent(context, EmprestimosActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun ExposicoesScreenPreview() {
    UniforLibraryTheme {
        ExposicoesScreen()
    }
}
