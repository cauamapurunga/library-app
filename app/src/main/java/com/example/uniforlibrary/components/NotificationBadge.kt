package com.example.uniforlibrary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Badge de notificação para mostrar contagem
 * Usa em ícones de notificação na navbar ou toolbar
 */
@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.Red),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > maxCount) "$maxCount+" else count.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Badge posicionado sobre um ícone
 */
@Composable
fun BadgeBox(
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        if (count > 0) {
            NotificationBadge(
                count = count,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
    }
}

