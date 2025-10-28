package com.example.uniforlibrary.exposicoesAdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniforlibrary.R
import com.example.uniforlibrary.components.AdminBottomNav
import com.example.uniforlibrary.exposicoes.ReadingActivity
import com.example.uniforlibrary.profile.EditProfileActivity
import com.example.uniforlibrary.ui.theme.UniforLibraryTheme

class ExposicaoDetailAdm_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniforLibraryTheme {
                ExposicaoDetailAdmScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposicaoDetailAdmScreen(onBack: () -> Unit) {
    val context = LocalContext.current

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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notificações */ }) {
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder
                contentDescription = "Book Cover",
                modifier = Modifier
                    .size(180.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("PathExileLORE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Narak ★5", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "Categoria", value = "História", modifier = Modifier.weight(1f))
                InfoChip(label = "Ano", value = "2020", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoChip(label = "Autor", value = "Narak", modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navigateToReading(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LER", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailSection()
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DetailSection() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Descrição")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTabIndex) {
            0 -> Text(
                "A história de Path of Exile é um RPG de ação sombrio onde o jogador, um exilado criminoso, é enviado para a perigosa terra de Wraeclast, evitando assim o reinado divino de Oriath.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}

private fun navigateToReading(context: Context) {
    context.startActivity(Intent(context, ReadingActivity::class.java))
}

private fun navigateToProfile(context: Context) {
    context.startActivity(Intent(context, EditProfileActivity::class.java))
}

@Preview(showBackground = true)
@Composable
fun ExposicaoDetailAdmScreenPreview() {
    UniforLibraryTheme {
        ExposicaoDetailAdmScreen(onBack = {})
    }
}
