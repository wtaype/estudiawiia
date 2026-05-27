package com.estudiawii.app.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.estudiawii.app.core.streak.StreakState
import com.estudiawii.app.ui.components.GlassCard
import com.estudiawii.app.ui.components.GoldPill
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText

@Composable
fun RachaPage(state: StreakState) {
    val textColor = if (WiCss.isDark) WiCss.txe else WiCss.tx
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.78f) {
                GoldPill("Racha")
                Text("Tu racha", style = WiText.display.copy(color = textColor), modifier = Modifier.padding(top = 18.dp))
                Text("Una senal amable para volver cada dia a tu app.", style = WiText.body.copy(color = textColor), modifier = Modifier.padding(top = 10.dp))
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.78f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .size(96.dp)
                            .background(WiCss.warning.copy(alpha = if (state.activeToday) 0.24f else 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (state.activeToday) WiCss.warning else WiCss.mco.copy(alpha = 0.55f),
                            modifier = Modifier.size(54.dp),
                        )
                    }
                    Text("${state.current}", style = WiText.display.copy(color = textColor), modifier = Modifier.padding(top = 44.dp))
                    Text("dias activos", style = WiText.h3.copy(color = textColor), modifier = Modifier.padding(top = 8.dp))
                    Text(
                        "Suma una vez por dia al abrir EstudiaWii. Si faltas un dia, usa una gracia mensual automaticamente.",
                        style = WiText.body.copy(color = textColor),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StreakMiniCard("Mejor racha", "${state.best}", textColor, Modifier.weight(1f))
                StreakMiniCard("Gracias libres", "${state.graceLeft}/3", textColor, Modifier.weight(1f))
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.54f) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(42.dp).background(WiCss.mco.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Favorite, null, tint = WiCss.mco, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Regla amable", style = WiText.h3.copy(color = textColor))
                        Text("La racha es local y rapida; perfecta como base para sincronizar despues si tu app lo necesita.", style = WiText.small.copy(color = textColor), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakMiniCard(title: String, value: String, textColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    GlassCard(modifier, intensity = 0.52f) {
        Text(value, style = WiText.h2.copy(color = textColor))
        Text(title, style = WiText.small.copy(color = textColor), modifier = Modifier.padding(top = 8.dp))
    }
}
