package com.example.uniforlibrary.exposicoesAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.notificacoes.NotificacoesActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

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
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedAvailability by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAvailabilityDropdown by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Cordel", "Artigo", "TCC", "Conto", "Produção")
    val availabilityOptions = listOf("Todos", "Pendente", "Aprovado", "Reprovado")

    val submissions = remember {
        mutableStateListOf(
            Submission("PathExileLORE", "Narak - 2020", "★5", "Aprovado"),
            Submission("WOW", "Aliens - 1977", "★4.8", "Reprovado"),
            Submission("How to build your upper/lower exercises", "JohnDoe - 2025", "★4.1", "Pendente")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.logo_branca), contentDescription = "Logo Unifor", modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Exposições", color = Color.White, fontWeight = FontWeight.Bold)
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
            AdminBottomNav(context = context, selectedItemIndex = 2)
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

                // Search and Filter Section
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Pesquisar por título ou autor") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedCategory.ifEmpty { "Todos" },
                            onValueChange = {},
                            label = { Text("Categoria") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true },
                            readOnly = true
                        )
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.surface)
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

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedAvailability.ifEmpty { "Todos" },
                            onValueChange = {},
                            label = { Text("Disponibilidade") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAvailabilityDropdown = true },
                            readOnly = true
                        )
                        DropdownMenu(
                            expanded = showAvailabilityDropdown,
                            onDismissRequest = { showAvailabilityDropdown = false },
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.surface)
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(submissions) { submission ->
                    SubmissionCard(
                        submission = submission,
                        onStatusChange = { newStatus ->
                            val index = submissions.indexOf(submission)
                            if (index != -1) {
                                submissions[index] = submission.copy(status = newStatus)
                            }
                        },
                        onViewClick = { navigateToExposicaoDetailAdm(context) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(submission: Submission, onStatusChange: (String) -> Unit, onViewClick: () -> Unit) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = {
                Text(
                    "Confirmação",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Tem certeza que deseja aprovar a obra ao acervo?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApproveDialog = false
                        onStatusChange("Aprovado")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sim")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showApproveDialog = false }
                ) {
                    Text("Não")
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text(
                    "Confirmação",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Tem certeza que deseja reprovar a obra?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog = false
                        onStatusChange("Reprovado")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sim")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRejectDialog = false }
                ) {
                    Text("Não")
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
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = "Book Icon", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    submission.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(submission.author, color = Color.Gray, fontSize = 14.sp)
                Text(submission.rating, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (submission.status) {
                    "Aprovado" -> Color(0xFF388E3C)
                    "Reprovado" -> Color.Red
                    else -> Color.Gray
                }
                Text(submission.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextButton(onClick = onViewClick) { Text("Ver") }
                    TextButton(
                        onClick = { showApproveDialog = true }
                    ) {
                        Text("Aprovar", color = Color(0xFF388E3C))
                    }
                    TextButton(
                        onClick = { showRejectDialog = true }
                    ) {
                        Text("Reprovar", color = Color.Red)
                    }
                }
            }
        }
    }
}


// --- Modelos e Navegação ---
data class Submission(val title: String, val author: String, val rating: String, val status: String)

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

private fun navigateToExposicaoDetailAdm(context: Context) {
    context.startActivity(Intent(context, ExposicaoDetailAdm_Activity::class.java))
}


@Preview(showBackground = true)
@Composable
fun ExposicoesAdmScreenPreview() {
    UniforLibraryTheme {
        ExposicoesAdmScreen()
    }
}
