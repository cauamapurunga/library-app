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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervo.BookDetailActivity
import com.example.uniforlibrary.model.Book
import com.example.uniforlibrary.viewmodel.BookViewModel
import com.example.uniforlibrary.acervoAdm.AcervoAdm_Activity
import com.example.uniforlibrary.components.AdminBottomNav
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
    val viewModel: com.example.uniforlibrary.viewmodel.HomeAdmViewModel = viewModel()
    val bookViewModel: BookViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by bookViewModel.searchResults.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

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
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SearchBarAdmin(
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    isSearching = query.isNotBlank()
                    bookViewModel.searchBooks(query)
                },
                onClearSearch = {
                    searchQuery = ""
                    isSearching = false
                    bookViewModel.searchBooks("")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results Section (shown when searching)
            if (isSearching) {
                SearchResultsSectionAdmin(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    context = context
                )
            } else {
                // Normal Home Content (shown when not searching)
                Spacer(modifier = Modifier.height(8.dp))
                QuickAccessAdmin(context)
                Spacer(modifier = Modifier.height(24.dp))

                when (uiState) {
                    is com.example.uniforlibrary.viewmodel.HomeAdmUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is com.example.uniforlibrary.viewmodel.HomeAdmUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = (uiState as com.example.uniforlibrary.viewmodel.HomeAdmUiState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadMetrics() }) {
                                    Text("Tentar novamente")
                                }
                            }
                        }
                    }
                    is com.example.uniforlibrary.viewmodel.HomeAdmUiState.Success -> {
                        val metrics = (uiState as com.example.uniforlibrary.viewmodel.HomeAdmUiState.Success).metrics
                        MetricsSection(metrics)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarAdmin(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Biblioteca Universitária",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pesquise títulos, gerencie empréstimos e acompanhe os relatórios.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Pesquisar livros por título, autor ou gênero...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Limpar pesquisa",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun SearchResultsSectionAdmin(
    searchQuery: String,
    searchResults: List<Book>,
    context: Context
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resultados da pesquisa",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${searchResults.size} livro(s)",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pesquisando por: \"$searchQuery\"",
            fontSize = 12.sp,
            color = Color.Gray,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum livro encontrado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tente pesquisar com outras palavras-chave",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Results list - Simple book names only
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    searchResults.forEach { book ->
                        SearchResultItemAdmin(book = book, context = context)
                        if (book != searchResults.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color.LightGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultItemAdmin(book: Book, context: Context) {
    Surface(
        onClick = {
            val intent = Intent(context, AcervoAdm_Activity::class.java)
            intent.putExtra("BOOK_ID", book.id)
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = book.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalhes",
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
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
fun MetricsSection(metrics: com.example.uniforlibrary.repository.HomeMetrics) {
    Column {
        Text("Visão rápida de métricas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricCircle(value = metrics.loansLast30Days.toString(), label = "Empréstimos (30 dias)")
            MetricCircle(value = metrics.activeReservations.toString(), label = "Reservas ativas")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricCircle(value = metrics.totalBooks.toString(), label = "Total de livros")
            MetricCircle(value = metrics.activeUsers.toString(), label = "Usuários ativos")
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

// --- Navegação ---
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
