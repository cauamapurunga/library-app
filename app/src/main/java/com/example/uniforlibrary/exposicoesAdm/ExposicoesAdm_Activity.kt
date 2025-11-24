package com.example.uniforlibrary.exposicoesAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.BadgeBox
import com.example.uniforlibrary.model.Producao
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.NotificationViewModel
import com.example.uniforlibrary.viewmodel.ProducaoAdminUiState
import com.example.uniforlibrary.viewmodel.ProducaoAdminViewModel
import java.text.SimpleDateFormat
import java.util.*

class ExposicoesAdm_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ExposicoesAdmScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposicoesAdmScreen() {
    val context = LocalContext.current
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val viewModel: ProducaoAdminViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedAvailability by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAvailabilityDropdown by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Cordel", "Artigo", "Produção", "Conto")
    val availabilityOptions = listOf("Todos", "Pendente", "Aprovado", "Reprovado")

    // Exibir resultado de ações
    LaunchedEffect(actionResult) {
        actionResult?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearActionResult()
        }
    }

    // Aplicar filtros quando mudarem
    LaunchedEffect(selectedCategory, selectedAvailability, searchText) {
        val categoriaFiltro = if (selectedCategory == "Todos" || selectedCategory.isEmpty()) "" else selectedCategory
        val statusFiltro = if (selectedAvailability == "Todos" || selectedAvailability.isEmpty()) "" else selectedAvailability

        viewModel.loadProducoes(
            categoria = categoriaFiltro,
            status = statusFiltro,
            searchQuery = searchText
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
                        Text("Exposições", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, NotificacoesActivity::class.java))
                    }) {
                        BadgeBox(count = unreadCount) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                        }
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
            AdminBottomNav(context = context, selectedItemIndex = 2)
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Gerencie as submissões dos alunos que desejam expor seus trabalhos para avaliação e validação no nosso acervo.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search Field
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Pesquisar por título ou autor") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedCategory.ifEmpty { "Todos" },
                            onValueChange = {},
                            label = { Text("Categoria") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { showCategoryDropdown = !showCategoryDropdown }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Status Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedAvailability.ifEmpty { "Todos" },
                            onValueChange = {},
                            label = { Text("Status") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { showAvailabilityDropdown = !showAvailabilityDropdown }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        DropdownMenu(
                            expanded = showAvailabilityDropdown,
                            onDismissRequest = { showAvailabilityDropdown = false }
                        ) {
                            availabilityOptions.forEach { availability ->
                                DropdownMenuItem(
                                    text = { Text(availability) },
                                    onClick = {
                                        selectedAvailability = availability
                                        showAvailabilityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Content based on state
            when (val state = uiState) {
                is ProducaoAdminUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProducaoAdminUiState.Success -> {
                    if (state.producoes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Inbox,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Nenhuma produção encontrada",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.producoes) { producao ->
                                ProducaoCard(
                                    producao = producao,
                                    onAprovar = { viewModel.aprovarProducao(producao.id) },
                                    onReprovar = { motivo -> viewModel.reprovarProducao(producao.id, motivo) },
                                    onViewClick = {
                                        navigateToExposicaoDetailAdm(context, producao.id)
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                is ProducaoAdminUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                state.message,
                                color = Color.Red,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProducaoCard(
    producao: Producao,
    onAprovar: () -> Unit,
    onReprovar: (String) -> Unit,
    onViewClick: () -> Unit
) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = {
                Text("Confirmação", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Tem certeza que deseja aprovar a obra \"${producao.titulo}\" ao acervo?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApproveDialog = false
                        onAprovar()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sim")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApproveDialog = false }) {
                    Text("Não")
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text("Reprovar Produção", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Informe o motivo da reprovação:",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Motivo da reprovação") },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            showRejectDialog = false
                            onReprovar(rejectReason)
                            rejectReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Reprovar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showRejectDialog = false
                    rejectReason = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Capa da produção
            if (producao.fotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = producao.fotoUrl,
                    contentDescription = "Capa de ${producao.titulo}",
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producao.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    producao.usuarioNome,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    producao.categoria,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                producao.createdAt?.let { timestamp ->
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text(
                        formatter.format(date),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (producao.status) {
                    "aprovado" -> Color(0xFF388E3C)
                    "reprovado" -> Color.Red
                    else -> Color(0xFFFF9800)
                }
                val statusText = when (producao.status) {
                    "aprovado" -> "Aprovado"
                    "reprovado" -> "Reprovado"
                    else -> "Pendente"
                }
                Text(
                    statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextButton(onClick = onViewClick) {
                        Text("Ver")
                    }
                    if (producao.status == "pendente") {
                        TextButton(onClick = { showApproveDialog = true }) {
                            Text("Aprovar", color = Color(0xFF388E3C))
                        }
                        TextButton(onClick = { showRejectDialog = true }) {
                            Text("Reprovar", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

private fun navigateToExposicaoDetailAdm(context: Context, producaoId: String) {
    val intent = Intent(context, ExposicaoDetailAdm_Activity::class.java)
    intent.putExtra("PRODUCAO_ID", producaoId)
    context.startActivity(intent)
}