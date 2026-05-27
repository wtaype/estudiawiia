package com.estudiawii.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText

@Composable
fun AvatarImage(
    profile: Smile?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val initials = profile?.nombreCompleto
        ?.takeIf { it.isNotBlank() }
        ?.split(Regex("\\s+"))
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?: profile?.usuario?.take(2)?.uppercase()
        ?: "BW"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(WiCss.mco.copy(alpha = 0.18f))
            .border(1.5.dp, WiCss.brd, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!profile?.avatar.isNullOrBlank()) {
            AsyncImage(
                model = profile?.avatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                initials,
                style = WiText.tiny.copy(
                    color = WiCss.mco,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.32f).sp,
                ),
                maxLines = 1,
            )
        }
    }
}
