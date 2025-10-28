package com.example.uniforlibrary.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.uniforlibrary.chatbot.ChatActivity

@Composable
fun Chatbot(context: Context) {
    FloatingActionButton(
        onClick = { context.startActivity(Intent(context, ChatActivity::class.java)) },
        modifier = Modifier.padding(16.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Default.Chat, contentDescription = "Chatbot", tint = Color.White)
    }
}
