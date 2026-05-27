package com.estudiawii.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText
import com.estudiawii.app.ui.theme.WiiFontFamily
import com.estudiawii.app.ui.theme.softGlassShadow
import java.time.LocalDate
import java.time.LocalDateTime

fun saludar(): String = when (LocalDateTime.now().hour) {
    in 0..11 -> "Buenos dias"
    in 12..17 -> "Buenas tardes"
    else -> "Buenas noches"
}

fun wiDia(): String {
    val now = LocalDate.now()
    val dias = listOf("Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")
    val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    return "${dias[now.dayOfWeek.value % 7]}, ${now.dayOfMonth} ${meses[now.monthValue - 1]}"
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    intensity: Float = 0.55f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = WiCss.glassShape(intensity)
    val cardContent: @Composable () -> Unit = {
        Column(Modifier.padding(18.dp)) {
            content()
        }
    }
    if (onClick == null) {
        Card(
            modifier = modifier.softGlassShadow(),
            shape = shape,
            colors = WiCss.glassColors(intensity),
            border = WiCss.glassBorder(intensity),
            content = { cardContent() },
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier.softGlassShadow(),
            shape = shape,
            colors = WiCss.glassColors(intensity),
            border = WiCss.glassBorder(intensity),
            content = { cardContent() },
        )
    }
}

@Composable
fun GoldPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(WiCss.mco.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text.uppercase(), style = WiText.label, color = WiCss.mco, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun WiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    loading: Boolean = false
) {
    val alpha = if (loading) 0.5f else 1.0f
    val gradButton = Brush.linearGradient(
        listOf(
            WiCss.mco.copy(alpha = alpha),
            WiCss.hva.copy(alpha = alpha)
        )
    )

    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(gradButton),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = WiCss.white),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(color = WiCss.white, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontFamily = WiiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun WiField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = WiiFontFamily) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = WiCss.mco, modifier = Modifier.size(20.dp)) } },
        visualTransformation = visualTransformation,
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WiCss.mco,
            unfocusedBorderColor = WiCss.brd.copy(alpha = 0.60f),
            focusedContainerColor = WiCss.inp.copy(alpha = 0.80f),
            unfocusedContainerColor = WiCss.inp.copy(alpha = 0.50f),
            focusedTextColor = WiCss.tx1,
            unfocusedTextColor = WiCss.tx1,
        ),
    )
}
