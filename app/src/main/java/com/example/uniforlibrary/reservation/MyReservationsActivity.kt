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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervo.AcervoActivity
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

data class ReservationItem(
    val id: Int,
    val title: String,
    val author: String,
    val date: String,
    val status: String,
    val positionInQueue: Int? = null
)

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
fun MyReservationsScreen() {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedNavItemIndex by remember { mutableIntStateOf(3) } // "Reservas" is selected
    val tabs = listOf("Todos", "Disponíveis", "Aguardando", "Devolvidos")

    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )

    val allReservations = remember {
        listOf(
            ReservationItem(1, "How to build your upper/lower exercises", "Autor #1", "02/09/2025", "Aguardando", 2),
            ReservationItem(2, "WOW", "Autor X", "02/09/2025", "Devolvido"),
            ReservationItem(3, "O Poder do Hábito", "Charles Duhigg", "15/08/2025", "Disponível"),
            ReservationItem(4, "O Guia do Mochileiro das Galáxias", "Douglas Adams", "10/07/2025", "Aguardando", 5),
            ReservationItem(5, "1984", "George Orwell", "20/06/2025", "Disponível"),
            ReservationItem(6, "A Revolução dos Bichos", "George Orwell", "18/06/2025", "Devolvido")
        )
    }

    val filteredReservations = remember(selectedTabIndex) {
        when (tabs[selectedTabIndex]) {
            "Disponíveis" -> allReservations.filter { it.status == "Disponível" }
            "Aguardando" -> allReservations.filter { it.status == "Aguardando" }
            "Devolvidos" -> allReservations.filter { it.status == "Devolvido" }
            else -> allReservations
        }
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
            Surface(tonalElevation = 0.dp, shadowElevation = 16.dp, color = Color.White) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(80.dp).padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedNavItemIndex == item.index,
                            onClick = {
                                selectedNavItemIndex = item.index
                                when (item.index) {
                                    0 -> navigateToHome(context)
                                    1 -> navigateToAcervo(context)
                                    2 -> navigateToEmprestimos(context)
                                    3 -> { /* Já está em Reservas */ }
                                    4 -> navigateToProduzir(context)
                                    5 -> navigateToExposicoes(context)
                                }
                            },
                            label = { Text(item.label, fontSize = 9.sp, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 11.sp, fontWeight = if (selectedNavItemIndex == item.index) FontWeight.Bold else FontWeight.Medium) },
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredReservations) { reservation ->
                    ReservationCard(reservation)
                }
            }
        }
    }
}

@Composable
fun ReservationCard(reservation: ReservationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = reservation.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reservation.author,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (reservation.status == "Aguardando" && reservation.positionInQueue != null) {
                            "Posição na fila: ${reservation.positionInQueue}"
                        } else {
                            reservation.date
                        },
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    when (reservation.status) {
                        "Aguardando" -> {
                            OutlinedButton(
                                onClick = { /* TODO: Cancelar */ },
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancelar", fontSize = 11.sp)
                            }
                        }
                        "Devolvido" -> {
                            Button(
                                onClick = { /* TODO: Renovar */ },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Renovar", fontSize = 11.sp)
                            }
                        }
                        "Disponível" -> {
                            Button(
                                onClick = { /* TODO: Reservar */ },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reservar", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val index: Int)

private fun navigateToHome(context: Context) {
    context.startActivity(Intent(context, HomeActivity::class.java))
}

private fun navigateToAcervo(context: Context) {
    context.startActivity(Intent(context, AcervoActivity::class.java))
}

private fun navigateToEmprestimos(context: Context) {
    context.startActivity(Intent(context, EmprestimosActivity::class.java))
}

private fun navigateToReservations(context: Context) {
    context.startActivity(Intent(context, MyReservationsActivity::class.java))
}

private fun navigateToProduzir(context: Context) {
    context.startActivity(Intent(context, ProduzirActivity::class.java))
}

private fun navigateToExposicoes(context: Context) {
    context.startActivity(Intent(context, ExposicoesActivity::class.java))
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun MyReservationsScreenPreview() {
    UniforLibraryTheme {
        MyReservationsScreen()
    }
}
