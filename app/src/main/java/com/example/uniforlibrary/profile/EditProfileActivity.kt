package com.example.uniforlibrary.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.login.LoginActivity
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
fun EditProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }

    var showPhotoDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var tempEmail by remember { mutableStateOf("") }
    var tempPhone by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Atualizar dados quando o perfil for carregado
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            email = profile.email
            phone = formatPhoneNumber(profile.telefone)
            nome = profile.nome
            matricula = profile.matricula
            curso = profile.curso
        }
    }

    // Observar mudanças de estado
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Loading -> {
                isLoading = true
            }
            is ProfileState.Success -> {
                isLoading = false
                Toast.makeText(
                    context,
                    (profileState as ProfileState.Success).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetState()
            }
            is ProfileState.Error -> {
                isLoading = false
                Toast.makeText(
                    context,
                    (profileState as ProfileState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetState()
            }
            is ProfileState.ProfileLoaded -> {
                isLoading = false
            }
            else -> {
                isLoading = false
            }
        }
    }

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
                .weight(1f)
                .verticalScroll(rememberScrollState())
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
                text = nome.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // User Details
            if (curso.isNotBlank()) {
                Text(
                    text = curso,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            if (matricula.isNotBlank()) {
                Text(
                    text = "Matrícula: $matricula",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

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

        // Logout Button (Fixed at bottom)
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sair",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sair da Conta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
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
            onDismissRequest = {
                showEditEmailDialog = false
                tempEmail = ""
                currentPassword = ""
                showPassword = false
            },
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
                        label = { Text("Novo Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Senha Atual") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword)
                                        "Ocultar senha"
                                    else
                                        "Mostrar senha"
                                )
                            }
                        }
                    )

                    Text(
                        text = "Por segurança, você precisa confirmar sua senha para alterar o email.",
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
                        onClick = {
                            showEditEmailDialog = false
                            tempEmail = ""
                            currentPassword = ""
                            showPassword = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            viewModel.updateEmail(tempEmail, currentPassword)
                            showEditEmailDialog = false
                            tempEmail = ""
                            currentPassword = ""
                            showPassword = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = tempEmail.isNotBlank() && currentPassword.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Salvar")
                        }
                    }
                }
            }
        )
    }

    // Edit Phone Dialog
    if (showEditPhoneDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditPhoneDialog = false
                tempPhone = ""
            },
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
                        label = { Text("Telefone (opcional)") },
                        placeholder = { Text("85999999999") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Text(
                        text = "Digite apenas números (DDD + número). Campo opcional.",
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
                        onClick = {
                            showEditPhoneDialog = false
                            tempPhone = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            viewModel.updatePhone(tempPhone)
                            showEditPhoneDialog = false
                            tempPhone = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Salvar")
                        }
                    }
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sair",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Sair da Conta",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja realmente sair da sua conta? Você precisará fazer login novamente para acessar.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()

                            // Navegar para tela de login e limpar backstack
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sim, Sair")
                    }

                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Não")
                    }
                }
            }
        )
    }
}

// Função auxiliar para formatar telefone
fun formatPhoneNumber(phone: String): String {
    if (phone.isBlank()) return ""

    val digits = phone.filter { it.isDigit() }
    return when (digits.length) {
        11 -> "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}"
        10 -> "(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6)}"
        else -> phone
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    UniforLibraryTheme {
        EditProfileScreen()
    }
}