package com.estudiawii.app.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.ui.components.AvatarImage
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText

import androidx.compose.foundation.layout.offset

@Composable
fun Header(
    profile: Smile?,
    currentPage: WiPage,
    onHome: () -> Unit,
    onStreak: () -> Unit,
    onNotifications: () -> Unit,
    onMenu: () -> Unit,
    streakCount: Int = 0,
    streakActive: Boolean = false,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(WiCss.bg.copy(alpha = 0.90f))
            .statusBarsPadding()
            .height(56.dp)
            .border(0.5.dp, WiCss.brd.copy(alpha = 0.22f))
            .padding(horizontal = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onHome),
            contentAlignment = Alignment.Center,
        ) {
            Text("EstudiaWii", style = WiText.h2.copy(color = WiCss.mco, fontSize = 24.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        }
        Box(Modifier.align(Alignment.CenterStart)) {
            if (showBack) HeaderIcon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack) else ProfileAvatar(profile, onHome)
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Botón Racha Premium (Compacto, Sin Fondo ni Borde) ──────────────
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clickable(onClick = onStreak)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = if (streakCount > 0) "Racha $streakCount" else "Racha",
                    tint = if (streakActive) WiCss.warning else WiCss.tx.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = streakCount.toString(),
                    color = if (streakActive) WiCss.tx else WiCss.tx.copy(alpha = 0.6f),
                    style = WiText.small.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    modifier = Modifier.offset(y = (-1.5).dp)
                )
            }

            HeaderIcon(Icons.Rounded.Notifications, "Notificaciones", onNotifications)
            HeaderIcon(Icons.Rounded.Menu, "Menu", onMenu)
        }
    }
}

@Composable
private fun ProfileAvatar(profile: Smile?, onClick: () -> Unit) {
    AvatarImage(profile = profile, size = 34.dp, modifier = Modifier.clickable(onClick = onClick))
}

@Composable
private fun HeaderIcon(icon: ImageVector, label: String, onClick: () -> Unit, tint: androidx.compose.ui.graphics.Color = WiCss.mco, badge: String? = null) {
    IconButton(onClick = onClick, modifier = Modifier.size(34.dp)) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            if (!badge.isNullOrBlank()) {
                Text(
                    badge,
                    color = WiCss.tx,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.TopEnd).padding(start = 16.dp),
                )
            }
        }
    }
}
