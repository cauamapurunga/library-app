package com.example.uniforlibrary.exposicoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.uniforlibrary.acervo.AcervoActivity
import com.example.uniforlibrary.components.BadgeBox
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.model.BottomNavItem
import com.example.uniforlibrary.model.Rating
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.PdfReaderActivity
import com.example.uniforlibrary.produzir.ProduzirActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.repository.RatingRepository
import com.example.uniforlibrary.repository.ReadingProgressRepository
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.ExposicaoDetailUserViewModel
import com.example.uniforlibrary.viewmodel.NotificationViewModel
import com.example.uniforlibrary.viewmodel.ProducaoDetailUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ExhibitionDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val producaoId = intent.getStringExtra(EXTRA_PRODUCAO_ID)

        setContent {
            UniforLibraryTheme {
                ProducaoDetailScreen(
                    producaoId = producaoId,
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PRODUCAO_ID = "producao_id"

        fun newIntent(context: Context, producaoId: String): Intent {
            return Intent(context, ExhibitionDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCAO_ID, producaoId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducaoDetailScreen(
    producaoId: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val viewModel: ExposicaoDetailUserViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var selectedItemIndex by remember { mutableIntStateOf(5) }

    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )

    LaunchedEffect(producaoId) {
        if (producaoId != null) {
            viewModel.loadProducao(producaoId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalhes da Produção",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, NotificacoesActivity::class.java))
                    }) {
                        BadgeBox(count = unreadCount) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = Color.White
                            )
                        }
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
                    containerColor = MaterialTheme.colorScheme.primary
                )
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
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 7.sp,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 9.sp,
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
    ) { innerPadding ->
        when (val state = uiState) {
            is ProducaoDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProducaoDetailUiState.Success -> {
                val producao = state.producao
                val auth = FirebaseAuth.getInstance()
                val ratingRepository = remember { RatingRepository() }
                val readingProgressRepository = remember { ReadingProgressRepository() }
                val scope = rememberCoroutineScope()

                var ratings by remember { mutableStateOf<List<Rating>>(emptyList()) }
                var averageRating by remember { mutableStateOf(0f) }
                var userRating by remember { mutableStateOf<Rating?>(null) }
                var isLoadingRatings by remember { mutableStateOf(false) }
                var showRatingDialog by remember { mutableStateOf(false) }
                var hasCompletedReading by remember { mutableStateOf(false) }

                // Carregar avaliações ao abrir a tela
                LaunchedEffect(producao.id) {
                    producao.id?.let { prodId ->
                        isLoadingRatings = true

                        // Verificar se o usuário completou a leitura
                        auth.currentUser?.uid?.let { userId ->
                            readingProgressRepository.hasCompletedReading(prodId, userId).onSuccess { completed ->
                                hasCompletedReading = completed
                            }
                        }

                        // Carregar avaliações
                        ratingRepository.getRatingsForProducao(prodId).onSuccess { loadedRatings ->
                            ratings = loadedRatings
                        }

                        // Carregar média
                        ratingRepository.getProducaoAverageRating(prodId).onSuccess { avg ->
                            averageRating = avg

                            // Atualizar rating no Firestore se necessário
                            if (avg > 0f) {
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("producoes")
                                    .document(prodId)
                                    .update("rating", avg)
                            }
                        }

                        // Verificar se usuário já avaliou
                        auth.currentUser?.uid?.let { userId ->
                            ratingRepository.getUserProducaoRating(prodId, userId).onSuccess { rating ->
                                userRating = rating
                            }
                        }

                        isLoadingRatings = false
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Imagem da capa
                    if (producao.fotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = producao.fotoUrl,
                            contentDescription = "Capa de ${producao.titulo}",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Título
                    Text(
                        producao.titulo,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Autor
                    Text(
                        producao.usuarioNome,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Informações em chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoChip(
                            label = "Categoria",
                            value = producao.categoria,
                            modifier = Modifier.weight(1f)
                        )
                        producao.createdAt?.let { timestamp ->
                            val date = timestamp.toDate()
                            val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
                            InfoChip(
                                label = "Ano",
                                value = formatter.format(date),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoChip(
                            label = "Autor",
                            value = producao.usuarioNome,
                            modifier = Modifier.weight(1f)
                        )
                        producao.createdAt?.let { timestamp ->
                            val date = timestamp.toDate()
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            InfoChip(
                                label = "Data",
                                value = formatter.format(date),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão de leitura (se houver PDF)
                    if (producao.arquivoUrl.isNotEmpty()) {
                        Button(
                            onClick = {
                                PdfReaderActivity.start(
                                    context,
                                    producao.arquivoUrl,
                                    producao.titulo,
                                    showRating = true,
                                    producaoId = producao.id
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ler Produção", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Seção de Avaliações
                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        "Avaliações",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resumo de avaliações
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", averageRating),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            ProducaoStarRatingDisplay(rating = averageRating)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Button(
                                onClick = {
                                    if (hasCompletedReading || userRating != null) {
                                        showRatingDialog = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Você precisa ler a produção completa antes de avaliar!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier.height(40.dp),
                                enabled = hasCompletedReading || userRating != null
                            ) {
                                Icon(
                                    if (userRating != null) Icons.Default.Edit else Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (userRating != null) "Editar" else "Avaliar")
                            }

                            // Mensagem informativa
                            if (!hasCompletedReading && userRating == null) {
                                Text(
                                    "Leia até o fim para avaliar",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de avaliações
                    if (isLoadingRatings) {
                        android.util.Log.d("ProducaoRatings", "⏳ Carregando avaliações...")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (ratings.isEmpty()) {
                        android.util.Log.d("ProducaoRatings", "📭 Nenhuma avaliação encontrada")
                        android.util.Log.d("ProducaoRatings", "  - producaoId: ${producao.id}")
                        Text(
                            "Boa leitura! 📚",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    } else {
                        android.util.Log.d("ProducaoRatings", "✅ Mostrando ${ratings.size} avaliações")
                        ratings.forEachIndexed { index, rating ->
                            android.util.Log.d("ProducaoRatings", "  [$index] ${rating.userName} - ${rating.stars} estrelas")
                        }

                        Text(
                            "Avaliações dos leitores (${ratings.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ratings.forEach { rating ->
                            ProducaoRatingItem(rating = rating)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Diálogo de avaliação
                if (showRatingDialog) {
                    ProducaoRatingDialog(
                        existingRating = userRating,
                        onDismiss = { showRatingDialog = false },
                        onSubmit = { stars, comment ->
                            scope.launch {
                                val currentUser = auth.currentUser
                                if (currentUser != null && producao.id != null) {
                                    val result = ratingRepository.createProducaoRating(
                                        producaoId = producao.id!!,
                                        userId = currentUser.uid,
                                        userName = currentUser.displayName ?: currentUser.email ?: "Usuário",
                                        stars = stars,
                                        comment = comment
                                    )

                                    result.onSuccess {
                                        showRatingDialog = false

                                        // Recarregar avaliações
                                        ratingRepository.getRatingsForProducao(producao.id!!).onSuccess { loadedRatings ->
                                            ratings = loadedRatings
                                        }
                                        ratingRepository.getProducaoAverageRating(producao.id!!).onSuccess { avg ->
                                            averageRating = avg
                                        }
                                        ratingRepository.getUserProducaoRating(producao.id!!, currentUser.uid).onSuccess { rating ->
                                            userRating = rating
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
            is ProducaoDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
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
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("Voltar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label, fontSize = 12.sp) },
        readOnly = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        enabled = false
    )
}

@Composable
fun ProducaoStarRatingDisplay(rating: Float, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating.toInt()) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProducaoRatingItem(rating: Rating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícone de usuário
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rating.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Nota numérica em destaque
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFC107).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${rating.stars}.0",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8F00),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    ProducaoStarRatingDisplay(rating = rating.stars.toFloat())
                }
            }

            // Data da avaliação
            rating.createdAt?.let { timestamp ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Text(
                    text = dateFormat.format(timestamp.toDate()),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProducaoRatingDialog(
    existingRating: Rating?,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(existingRating?.stars ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingRating != null) "Editar Avaliação" else "Avaliar esta produção",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Toque nas estrelas para avaliar")

                // Estrelas interativas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { index ->
                        IconButton(onClick = { selectedRating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Estrela ${index + 1}",
                                tint = if (index < selectedRating) Color(0xFFFFC107) else Color.LightGray,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedRating, "") },
                enabled = selectedRating > 0
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


// Navigation helpers
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

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

private fun navigateToReading(context: Context, pdfUrl: String) {
    try {
        // Cria um intent genérico para visualizar o conteúdo
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(android.net.Uri.parse(pdfUrl), "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Verifica se há aplicativos disponíveis
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)

        if (activities.isNotEmpty()) {
            // Cria o chooser para forçar a seleção de aplicativo
            val chooserIntent = Intent.createChooser(intent, "Escolha um aplicativo para abrir o PDF")
            context.startActivity(chooserIntent)
        } else {
            // Se não houver apps, tenta abrir no navegador
            val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(pdfUrl))
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Erro ao abrir PDF. Instale um leitor de PDF.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

@Preview(showBackground = true)
@Composable
fun ProducaoDetailScreenPreview() {
    UniforLibraryTheme {
        ProducaoDetailScreen(
            producaoId = "sample_id",
            onBack = {}
        )
    }
}