package com.example.uniforlibrary.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniforlibrary.R
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ForgotPasswordScreen()
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    var emailOuMatricula by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    // Observar mudanças no estado de autenticação
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, (authState as AuthState.Success).message, Toast.LENGTH_LONG).show()
                (context as? Activity)?.finish()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Unifor Logo",
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = "Recuperar Senha",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Digite sua matrícula ou email para receber um link de redefinição de senha",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = emailOuMatricula,
            onValueChange = { emailOuMatricula = it },
            label = { Text("Matrícula ou Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = authState !is AuthState.Loading
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.resetPassword(emailOuMatricula) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("ENVIAR LINK", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as? Activity)?.finish()
            },
            enabled = authState !is AuthState.Loading
        ) {
            Text("Voltar para o login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    UniforLibraryTheme {
        ForgotPasswordScreen()
    }
}
