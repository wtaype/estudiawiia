package com.estudiawii.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.estudiawii.app.ui.theme.WiCss

/**
 * Modificador premium para agregar animación Shimmer (brillo de carga) a cualquier elemento.
 */
@Composable
fun Modifier.shimmerAnimation(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    )

    // Gradiente con efecto de brillo metálico premium
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.28f),
        Color.White.copy(alpha = 0.05f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 250f, translateAnim + 250f)
    )

    return this.background(brush)
}

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Int = 120,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(WiCss.glassShape(0.55f))
            .border(WiCss.glassBorder(0.55f), WiCss.glassShape(0.55f))
            .shimmerAnimation(),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun SkeletonCursosPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera simulada
        SkeletonCard(height = 140) {
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerAnimation())
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.width(200.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerAnimation())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerAnimation())
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Cursos simulados
        repeat(3) {
            SkeletonCard(height = 130) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).shimmerAnimation())
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.width(140.dp).height(18.dp).clip(RoundedCornerShape(4.dp)).shimmerAnimation())
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.width(90.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerAnimation())
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).clip(RoundedCornerShape(1.dp)).shimmerAnimation())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).shimmerAnimation())
                        }
                    }
                }
            }
        }
    }
}
