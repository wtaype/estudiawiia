package com.estudiawii.app.feature.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText

@Composable
fun NavMain(selectedPage: WiPage, onSelected: (WiPage) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(66.dp).background(WiCss.wb.copy(alpha = 0.95f)).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WiPage.mainPages.forEach { page ->
            NavMainItem(page, selectedPage == page, { onSelected(page) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavMainItem(page: WiPage, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg by animateColorAsState(if (selected) WiCss.mco.copy(alpha = if (WiCss.isDark) 0.16f else 0.10f) else Color.Transparent, label = "nav-bg")
    val tint by animateColorAsState(if (selected) WiCss.mco else WiCss.mco.copy(alpha = if (WiCss.isDark) 0.86f else 0.74f), label = "nav-tint")
    val iconSize by animateDpAsState(if (selected) 22.dp else 21.dp, label = "nav-icon-size")
    Column(
        modifier = modifier
            .height(66.dp)
            .background(bg)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(Modifier.padding(top = 2.dp).height(3.dp).fillMaxWidth().background(if (selected) WiCss.mco else Color.Transparent))
        Icon(page.icon, contentDescription = page.label, tint = tint, modifier = Modifier.padding(top = 11.dp).size(iconSize))
        Text(page.label, style = WiText.small.copy(color = tint, fontSize = 9.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
