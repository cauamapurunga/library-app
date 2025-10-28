package com.example.uniforlibrary.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.acervo.AcervoActivity
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity

@Composable
fun UserBottomNav(context: Context, selectedItemIndex: Int) {
    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0) { navigateToHome(context) },
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1) { navigateToAcervo(context) },
        BottomNavItem("Empréstimos", Icons.Default.Book, 2) { navigateToEmprestimos(context) },
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3) { navigateToReservations(context) },
        BottomNavItem("Produzir", Icons.Default.Add, 4) { navigateToProduzir(context) },
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5) { navigateToExposicoes(context) }
    )

    Surface(tonalElevation = 0.dp, shadowElevation = 16.dp, color = Color.White) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(80.dp)
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            navigationItems.forEach { item ->
                NavigationBarItem(
                    selected = selectedItemIndex == item.index,
                    onClick = {
                        if (selectedItemIndex != item.index) {
                            item.onClick()
                        }
                    },
                    label = {
                        Text(
                            item.label,
                            fontSize = 9.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                            fontWeight = if (selectedItemIndex == item.index)
                                FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
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

data class BottomNavItem(val label: String, val icon: ImageVector, val index: Int, val onClick: () -> Unit)

// --- Funções de Navegação ---
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
