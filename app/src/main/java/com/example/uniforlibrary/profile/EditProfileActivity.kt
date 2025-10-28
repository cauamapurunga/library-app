package com.example.uniforlibrary.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditProfileScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("clodoaldo@unifor.br") }
    var phone by remember { mutableStateOf("(85) 99999-9999") }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }
    var tempEmail by remember { mutableStateOf("") }
    var tempPhone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Perfil") },
            navigationIcon = {
                IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture with Edit Button
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                FloatingActionButton(
                    onClick = { showPhotoDialog = true },
                    modifier = Modifier.size(36.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar foto",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = "CLODOALDO DA SILVA ROCHA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // User Details
            Text(
                text = "Análise e Desen. de Sistemas",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Matrícula: 2025101",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Contact Information Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Informações de contato",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field with Edit Icon
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                trailingIcon = {
                    IconButton(onClick = {
                        tempEmail = email
                        showEditEmailDialog = true
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field with Edit Icon
            OutlinedTextField(
                value = phone,
                onValueChange = {},
                label = { Text("Telefone") },
                trailingIcon = {
                    IconButton(onClick = {
                        tempPhone = phone.filter { it.isDigit() }
                        showEditPhoneDialog = true
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar telefone",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                ),
                enabled = false
            )
        }
    }

    // Photo Selection Dialog
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = {
                Text(
                    text = "Editar foto de perfil",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Escolha uma opção:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showPhotoDialog = false
                            // TODO: Abrir câmera
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Tirar Foto")
                    }

                    OutlinedButton(
                        onClick = {
                            showPhotoDialog = false
                            // TODO: Abrir galeria
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Escolher da Galeria")
                    }

                    TextButton(
                        onClick = { showPhotoDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    // Edit Email Dialog
    if (showEditEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEditEmailDialog = false },
            title = {
                Text(
                    text = "Editar Email",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showEditEmailDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            email = tempEmail
                            showEditEmailDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        )
    }

    // Edit Phone Dialog
    if (showEditPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showEditPhoneDialog = false },
            title = {
                Text(
                    text = "Editar Telefone",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { newValue ->
                            // Aceita apenas números e limita a 11 dígitos
                            if (newValue.all { it.isDigit() } && newValue.length <= 11) {
                                tempPhone = newValue
                            }
                        },
                        label = { Text("Telefone") },
                        placeholder = { Text("85999999999") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Text(
                        text = "Digite apenas números (DDD + número)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showEditPhoneDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            // Formatar o telefone
                            phone = if (tempPhone.length == 11) {
                                "(${tempPhone.substring(0, 2)}) ${tempPhone.substring(2, 7)}-${tempPhone.substring(7)}"
                            } else if (tempPhone.length == 10) {
                                "(${tempPhone.substring(0, 2)}) ${tempPhone.substring(2, 6)}-${tempPhone.substring(6)}"
                            } else {
                                tempPhone
                            }
                            showEditPhoneDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = tempPhone.length >= 10
                    ) {
                        Text("Salvar")
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    UniforLibraryTheme {
        EditProfileScreen()
    }
}
