package com.example.uniforlibrary.produzir

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.R
import com.example.uniforlibrary.acervo.AcervoActivity
import com.example.uniforlibrary.components.Chatbot
import com.example.uniforlibrary.emprestimos.EmprestimosActivity
import com.example.uniforlibrary.exposicoes.ExposicoesActivity
import com.example.uniforlibrary.home.HomeActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.reservation.MyReservationsActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme
import com.example.uniforlibrary.model.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduzirScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Cordel") }
    var fotoSelecionada by remember { mutableStateOf<String?>(null) }
    var arquivoSelecionado by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val navigationItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, 0),
        BottomNavItem("Acervo", Icons.AutoMirrored.Filled.MenuBook, 1),
        BottomNavItem("Empréstimos", Icons.Default.Book, 2),
        BottomNavItem("Reservas", Icons.Default.Bookmark, 3),
        BottomNavItem("Produzir", Icons.Default.Add, 4),
        BottomNavItem("Exposições", Icons.Default.PhotoLibrary, 5)
    )
    var selectedItemIndex by remember { mutableIntStateOf(4) } // Produzir selecionado

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_branca),
                            contentDescription = "Logo",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Submeter produção",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: notificações */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = Color.White)
                    }
                    IconButton(onClick = { navigateToProfile(context) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
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
                                2 -> navigateToEmprestimos(context)
                                3 -> navigateToReservations(context)
                                4 -> { /* Já está na tela Produzir */ }
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
                                fontWeight = if (selectedItemIndex == item.index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp))
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
        },
        floatingActionButton = {
            Chatbot(context = context)
        },
        floatingActionButtonPosition = FabPosition.Start,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Envie seus trabalhos para avaliação do comitê editorial da biblioteca",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                UploadBox(
                    titulo = "Upload da foto da produção:",
                    descricao = "Arraste sua foto\nFormatos aceitos: .png, .jpg, .jpeg - Máx 20MB",
                    onSelect = { fotoSelecionada = "foto.png" }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                CategoriaDropdown(selected = categoria, onSelect = { categoria = it })

                Spacer(modifier = Modifier.height(16.dp))

                UploadBox(
                    titulo = "Upload da produção:",
                    descricao = "Arraste seu PDF ou DOC\nFormatos aceitos: .pdf, .docx - Máx 20MB",
                    onSelect = { arquivoSelecionado = "trabalho.pdf" }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (titulo.isNotBlank() && arquivoSelecionado != null) {
                            showDialog = true
                        } else {
                            Toast.makeText(context, "Preencha o título e selecione um arquivo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar para avaliação", color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Dicas rápidas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Use títulos claros, inclua palavras-chaves e revise sua formatação para ficar dentro das normas da biblioteca.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(
                            "Confirmação",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            "Tem certeza que quer submeter essa produção? Após a confirmação o resultado será entregue em até 7 dias úteis.",
                            fontSize = 16.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                Toast.makeText(context, "Envio realizado com sucesso!", Toast.LENGTH_LONG).show()
                                onBack()
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
                            onClick = { showDialog = false }
                        ) {
                            Text("Não")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun UploadBox(titulo: String, descricao: String, onSelect: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(titulo, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = descricao,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSelect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Selecionar arquivo")
                }
            }
        }
    }
}

@Composable
fun CategoriaDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Cordel", "Artigo", "Conto", "Produção")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Categoria:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                label = { Text("Selecione a categoria") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = categoria,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (categoria == "Escrever por SCAMPER") {
                                    Text(
                                        text = "Substitua, Combine, Adapte, Modifique, Procure outros usos, Elimine, Rearranje",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelect(categoria)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

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

private fun navigateToExposicoes(context: Context) {
    context.startActivity(Intent(context, ExposicoesActivity::class.java))
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProduzirScreenPreview() {
    UniforLibraryTheme {
        ProduzirScreen(onBack = {})
    }
}
