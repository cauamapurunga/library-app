package com.example.uniforlibrary.acervo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class BookDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                BookDetailScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedItemIndex by remember { mutableIntStateOf(1) }
    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultar Acervo", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notificações */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(80.dp)
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == item.index,
                        onClick = {
                            selectedItemIndex = item.index
                            when (item.index) {
                                0 -> navigateToHome(context)
                                1 -> navigateToAcervo(context)
                                3 -> navigateToReservations(context)
                                4 -> navigateToProduzir(context)
                            }
                        },
                        label = { Text(item.label, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 2) },
                        icon = { Icon(item.icon, contentDescription = item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder
                contentDescription = "Book Cover",
                modifier = Modifier
                    .size(180.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("PathExileLORE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Narak ★5", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "Categoria", value = "História", modifier = Modifier.weight(1f))
                InfoChip(label = "Ano", value = "2020", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "Autor", value = "Narak", modifier = Modifier.weight(1f))
                InfoChip(label = "Exemplares disp.", value = "3 de 10", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showBottomSheet = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reservar Livro", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailSection()
        }

        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
                ReservationBottomSheetContent(
                    onConfirm = {
                        showBottomSheet = false
                        Toast.makeText(context, "Reserva Confirmada!", Toast.LENGTH_SHORT).show()
                    },
                    onCancel = { showBottomSheet = false }
                )
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DetailSection() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Descrição", "Avaliações")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTabIndex) {
            0 -> Text(
                "A história de Path of Exile é um RPG de ação sombrio onde o jogador, um exilado criminoso, é enviado para a perigosa terra de Wraeclast, evitando assim o reinado divino de Oriath.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
            1 -> Text("Nenhuma avaliação disponível ainda.", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReservationBottomSheetContent(onConfirm: () -> Unit, onCancel: () -> Unit) {
    var date by remember { mutableStateOf("30/10/2025") }
    var plazo by remember { mutableStateOf("7") }
    var observations by remember { mutableStateOf("") }
    var agreedToPolicies by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PathExileLORE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Narak ★5", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Data da retirada") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = plazo,
                onValueChange = { plazo = it },
                label = { Text("Prazo (dias)") },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = observations,
            onValueChange = { observations = it },
            label = { Text("Observações") },
            placeholder = { Text("Adicionar nota para a biblioteca") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "A reserva será mantida por 24h após a confirmação. Traga um documento com foto para a retirada.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Checkbox(checked = agreedToPolicies, onCheckedChange = { agreedToPolicies = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Concordo com as políticas de empréstimo e uso da biblioteca.", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = agreedToPolicies,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirmar Reserva")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancelar")
        }
    }
}

// Navigation helpers
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun BookDetailScreenPreview() {
    UniforLibraryTheme {
        BookDetailScreen(onBack = {})
    }
}
