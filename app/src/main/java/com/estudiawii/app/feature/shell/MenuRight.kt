package com.estudiawii.app.feature.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.ui.components.AvatarImage
import com.estudiawii.app.ui.components.GlassCard
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText

@Composable
fun MenuRight(
    open: Boolean,
    profile: Smile?,
    onClose: () -> Unit,
    onNavigate: (WiPage) -> Unit,
    onLogout: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(open, enter = fadeIn(tween(180)), exit = fadeOut(tween(180))) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.10f)).clickable(onClick = onClose))
        }
        AnimatedVisibility(
            visible = open,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = fadeIn(tween(180)) + slideInHorizontally(tween(260)) { it },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.86f)
                    .widthIn(max = 380.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(WiCss.wb)
                    .border(
                        1.dp,
                        WiCss.brd.copy(alpha = 0.46f),
                        RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
                    )
                    .statusBarsPadding(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { MenuProfileHeader(profile, onClose) }
                item { MenuSection("EstudiaWii") }
                items(WiPage.mainPages.take(4)) { page -> MenuRoute(page) { onNavigate(page) } }
                item { MenuSection("Acerca") }
                items(WiPage.aboutPages) { page -> MenuRoute(page) { onNavigate(page) } }
                item {
                    Spacer(Modifier.height(8.dp))
                    LogoutButton(onLogout)
                }
            }
        }
    }
}

@Composable
private fun MenuProfileHeader(profile: Smile?, onClose: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), intensity = 0.82f) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                AvatarImage(profile = profile, size = 54.dp)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(profile?.nombreCompleto?.ifBlank { "EstudiaWii" } ?: "EstudiaWii", style = WiText.h2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(profile?.usuario?.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "Plantilla premium", style = WiText.small, maxLines = 1)
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileChip(profile?.plan?.uppercase().orEmpty().ifBlank { "FREE" }, Icons.Rounded.Verified)
                    ProfileChip(profile?.rol?.replaceFirstChar { it.titlecase() }.orEmpty().ifBlank { "Usuario" }, Icons.Rounded.AccountCircle)
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(WiCss.white.copy(alpha = 0.62f))
            ) {
                Icon(Icons.Rounded.Close, "Cerrar", tint = WiCss.mco, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun MenuSection(title: String) {
    Text(title.uppercase(), style = WiText.label.copy(color = WiCss.mco), modifier = Modifier.padding(top = 12.dp, start = 4.dp, bottom = 2.dp))
}

@Composable
private fun MenuRoute(page: WiPage, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WiCss.mco.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(page.icon, null, tint = WiCss.mco, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(if (page.isMain) page.label.replace("Pantalla", "Pagina") else page.label, style = WiText.h3, maxLines = 1)
            Text(page.subtitle, style = WiText.tiny, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(WiCss.mco.copy(alpha = 0.20f))
            .border(
                1.dp,
                WiCss.brd.copy(alpha = 0.42f),
                RoundedCornerShape(99.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(5.dp))
        Text(text, style = WiText.tiny.copy(color = WiCss.mco, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WiCss.white.copy(alpha = 0.34f))
            .border(
                1.dp,
                WiCss.brd.copy(alpha = 0.34f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onLogout)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = WiCss.error)
        Text("Cerrar sesion", style = WiText.h3.copy(color = WiCss.error), modifier = Modifier.padding(start = 12.dp))
    }
}
