package com.example.uniforlibrary.exposicoesAdm

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.model.Producao
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.produzir.PdfReaderActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.viewmodel.ExposicaoDetailAdmViewModel
import com.example.uniforlibrary.viewmodel.ExposicaoDetailUiState
import java.text.SimpleDateFormat
import java.util.*

class ExposicaoDetailAdm_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val producaoId = intent.getStringExtra("PRODUCAO_ID") ?: ""

        setContent {
            UniforLibraryTheme {
                ExposicaoDetailAdmScreen(
                    producaoId = producaoId,
                    onBack = {
                        // Sinalizar que houve mudanças e a lista precisa ser atualizada
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1001
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposicaoDetailAdmScreen(producaoId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ExposicaoDetailAdmViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    // Carregar produção ao iniciar
    LaunchedEffect(producaoId) {
        if (producaoId.isNotEmpty()) {
            viewModel.loadProducao(producaoId)
        }
    }

    // Exibir resultado de ações
    LaunchedEffect(actionResult) {
        actionResult?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearActionResult()
        }
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
                        Text("Detalhes da Produção", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { Intent(context, NotificacoesActivity::class.java) }) {
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
            AdminBottomNav(context = context, selectedItemIndex = 2)
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ExposicaoDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ExposicaoDetailUiState.Success -> {
                ProducaoDetailContent(
                    producao = state.producao,
                    onApprove = { showApproveDialog = true },
                    onReject = { showRejectDialog = true },
                    onOpenFile = { url, title ->
                        PdfReaderActivity.start(
                            context,
                            url,
                            title,
                            showRating = false // Admin não precisa avaliar
                        )
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                // Dialog de Aprovação
                if (showApproveDialog) {
                    AlertDialog(
                        onDismissRequest = { showApproveDialog = false },
                        title = {
                            Text("Aprovar Produção", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Text(
                                "Tem certeza que deseja aprovar \"${state.producao.titulo}\"?\n\nEsta produção será incluída no acervo da biblioteca.",
                                fontSize = 16.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showApproveDialog = false
                                    viewModel.aprovarProducao(state.producao.id)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF388E3C)
                                )
                            ) {
                                Text("Aprovar")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showApproveDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Dialog de Reprovação
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
                                    placeholder = { Text("Ex: Formato inadequado, conteúdo incompleto...") },
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
                                        viewModel.reprovarProducao(state.producao.id, rejectReason)
                                        rejectReason = ""
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Digite o motivo da reprovação",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
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
            }
            is ExposicaoDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
fun ProducaoDetailContent(
    producao: Producao,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onOpenFile: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagem da Produção
        if (producao.fotoUrl.isNotEmpty()) {
            AsyncImage(
                model = producao.fotoUrl,
                contentDescription = "Capa da Produção",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Book,
                contentDescription = "Sem imagem",
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .padding(32.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título e Autor
        Text(
            producao.titulo,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            producao.usuarioNome,
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Badge de Status
        val statusColor = when (producao.status) {
            "aprovado" -> Color(0xFF388E3C)
            "reprovado" -> Color.Red
            else -> Color(0xFFFF9800)
        }
        val statusText = when (producao.status) {
            "aprovado" -> "✓ Aprovado"
            "reprovado" -> "✗ Reprovado"
            else -> "⏳ Pendente"
        }

        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Informações
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            informacoesAutor(
                label = "Categoria",
                value = producao.categoria,
                modifier = Modifier.weight(1f)
            )
            producao.createdAt?.let { timestamp ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                informacoesAutor(
                    label = "Data",
                    value = formatter.format(timestamp.toDate()),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        informacoesAutor(
            label = "Autor",
            value = producao.usuarioNome,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(24.dp))

        // Botão Ver Arquivo
        if (producao.arquivoUrl.isNotEmpty()) {
            Button(
                onClick = { onOpenFile(producao.arquivoUrl, producao.titulo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ABRIR ARQUIVO", fontWeight = FontWeight.Bold)
            }
        }

        // Botões de Aprovação/Reprovação (apenas se status = pendente)
        if (producao.status == "pendente") {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reprovar", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aprovar", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção de Descrição
        DetalheSecao(producao)
    }
}

@Composable
fun informacoesAutor(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        ),
        enabled = false
    )
}

@Composable
fun DetalheSecao(producao: Producao) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Informações")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetalheLinha("Status", producao.status.uppercase())

                producao.createdAt?.let { timestamp ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    DetalheLinha("Data de Submissão", formatter.format(timestamp.toDate()))
                }
            }
        }
    }
}

@Composable
fun DetalheLinha(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}