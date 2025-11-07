package com.example.uniforlibrary.acervo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uniforlibrary.acervo.model.Acervo
import androidx.compose.foundation.text.KeyboardOptions
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervo.viewmodel.AcervoViewModel
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class AcervoActivity : ComponentActivity() {
    private val viewModel: AcervoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                AcervoScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcervoScreen(viewModel: AcervoViewModel) {
    val context = LocalContext.current
    var selectedItemIndex by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    val acervos by viewModel.acervos.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )
    var currentScreen by remember { mutableStateOf("list") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_branca),
                            contentDescription = "Logo Unifor",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Acervo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, NotificacoesActivity::class.java)) }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificações",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 16.dp,
                color = Color.White
            ) {
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
                                selectedItemIndex = item.index
                                when (item.index) {
                                    0 -> navigateToHome(context)
                                    1 -> { /* já está em Acervo */ }
                                    2 -> navigateToEmprestimos(context)
                                    3 -> navigateToReservations(context)
                                    4 -> navigateToProduzir(context)
                                    5 -> navigateToExposicoes(context)
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
                .padding(16.dp)
        ) {
            when (currentScreen) {
                "list" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acervo",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { currentScreen = "add" }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Adicionar acervo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de busca
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isBlank()) {
                                viewModel.loadAcervos()
                            } else {
                                viewModel.searchAcervos(it)
                            }
                        },
                        placeholder = { Text("Pesquisar por título, autor ou ISBN") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtros
                    var showFilters by remember { mutableStateOf(false) }
                    var selectedCategoria by remember { mutableStateOf<String?>(null) }
                    var showSomenteDisponiveis by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showFilters = !showFilters }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtros"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filtros")
                        }

                        if (selectedCategoria != null || showSomenteDisponiveis) {
                            TextButton(
                                onClick = {
                                    selectedCategoria = null
                                    showSomenteDisponiveis = false
                                    viewModel.loadAcervos()
                                }
                            ) {
                                Text("Limpar filtros")
                            }
                        }
                    }

                    AnimatedVisibility(visible = showFilters) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Filtro de categoria
                            var showCategoriaMenu by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedCategoria ?: "Selecione uma categoria",
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showCategoriaMenu = !showCategoriaMenu }) {
                                            Icon(Icons.Default.ArrowDropDown, "Selecionar categoria")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                DropdownMenu(
                                    expanded = showCategoriaMenu,
                                    onDismissRequest = { showCategoriaMenu = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    listOf("Literatura", "Ciências", "Tecnologia", "História", "Geografia",
                                          "Artes", "Filosofia", "Sociologia", "Engenharia", "Matemática", "Outros"
                                    ).forEach { categoria ->
                                        DropdownMenuItem(
                                            text = { Text(categoria) },
                                            onClick = {
                                                selectedCategoria = categoria
                                                showCategoriaMenu = false
                                                viewModel.filterAcervos(
                                                    categoria = categoria,
                                                    disponivel = if (showSomenteDisponiveis) true else null
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Filtro de disponibilidade
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = showSomenteDisponiveis,
                                    onCheckedChange = { checked ->
                                        showSomenteDisponiveis = checked
                                        viewModel.filterAcervos(
                                            categoria = selectedCategoria,
                                            disponivel = if (checked) true else null
                                        )
                                    }
                                )
                                Text("Mostrar apenas disponíveis")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (error != null) {
                        Text(
                            text = error ?: "Erro ao carregar acervo",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn {
                            items(acervos) { acervo ->
                                BookCard(
                                    title = acervo.titulo,
                                    subtitle = "${acervo.autor} - ${acervo.ano}",
                                    rating = "★${acervo.classificacao}",
                                    disponivel = acervo.disponivel,
                                    onReserveClick = {
                                        val intent = Intent(context, BookDetailActivity::class.java).apply {
                                            putExtra("acervoId", acervo.id)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                "add" -> {
                    AddEditContent(
                        viewModel = viewModel,
                        title = "Adicionar Acervo",
                        onBack = { currentScreen = "list" }
                    )
                }
            }
        }
    }
}

@Composable
fun BookCard(
    title: String,
    subtitle: String,
    rating: String,
    disponivel: Boolean,
    onReserveClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(rating, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (disponivel) "Disponível" else "Indisponível",
                    color = if (disponivel) Color(0xFF388E3C) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onReserveClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = disponivel
            ) {
                Text("Reservar")
            }
        }
    }
}

@Composable
fun AddEditContent(
    viewModel: AcervoViewModel,
    title: String,
    onBack: () -> Unit
) {
    var acervoTitulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var exemplaresTotais by remember { mutableStateOf("") }
    var isDigital by remember { mutableStateOf(false) }
    var isFisico by remember { mutableStateOf(true) }
    var showCategoriaMenu by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = acervoTitulo,
            onValueChange = { acervoTitulo = it },
            label = { Text("Título*") },
            modifier = Modifier.fillMaxWidth(),
            isError = hasError && acervoTitulo.isBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = autor,
            onValueChange = { autor = it },
            label = { Text("Autor*") },
            modifier = Modifier.fillMaxWidth(),
            isError = hasError && autor.isBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = isbn,
            onValueChange = { isbn = it },
            label = { Text("ISBN") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ano,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("\\d*"))) ano = newValue
            },
            label = { Text("Ano*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = hasError && ano.isBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Categoria
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = categoria,
                onValueChange = { },
                label = { Text("Categoria*") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoriaMenu = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Selecionar categoria")
                    }
                }
            )

            DropdownMenu(
                expanded = showCategoriaMenu,
                onDismissRequest = { showCategoriaMenu = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                listOf("Literatura", "Ciências", "Tecnologia", "História", "Geografia",
                      "Artes", "Filosofia", "Sociologia", "Engenharia", "Matemática", "Outros"
                ).forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            categoria = cat
                            showCategoriaMenu = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = exemplaresTotais,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("\\d*"))) exemplaresTotais = newValue
            },
            label = { Text("Quantidade de exemplares*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = hasError && exemplaresTotais.isBlank()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isDigital,
                onCheckedChange = { isDigital = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Digital")
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = isFisico,
                onCheckedChange = { isFisico = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Físico")
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição*") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            isError = hasError && descricao.isBlank()
        )

        if (hasError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Voltar")
            }

            Button(
                onClick = {
                    if (acervoTitulo.isNotBlank() &&
                        autor.isNotBlank() &&
                        ano.isNotBlank() &&
                        exemplaresTotais.isNotBlank() &&
                        descricao.isNotBlank() &&
                        (isDigital || isFisico)) {
                        val novoAcervo = Acervo(
                            titulo = acervoTitulo,
                            autor = autor,
                            ano = ano.toInt(),
                            categoria = categoria,
                            descricao = descricao,
                            exemplaresTotais = exemplaresTotais.toInt(),
                            exemplaresDisponiveis = exemplaresTotais.toInt(),
                            digital = isDigital,
                            fisico = isFisico
                        )
                        viewModel.addAcervo(novoAcervo)
                        onBack()
                    } else {
                        hasError = true
                        errorMessage = "Por favor, preencha todos os campos obrigatórios"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (title.contains("Editar")) "Confirmar" else "Adicionar")
            }
        }
    }
}



// Navigation helpers
private fun navigateToHome(context: Context) {
    val intent = Intent(context, HomeActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToReservations(context: Context) {
    val intent = Intent(context, MyReservationsActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToProfile(context: Context) {
    val intent = Intent(context, EditProfileActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToProduzir(context: Context) {
    val intent = Intent(context, ProduzirActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToExposicoes(context: Context) {
    val intent = Intent(context, ExposicoesActivity::class.java)
    context.startActivity(intent)
}

private fun navigateToEmprestimos(context: Context) {
    val intent = Intent(context, EmprestimosActivity::class.java)
    context.startActivity(intent)
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

