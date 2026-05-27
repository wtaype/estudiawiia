package com.estudiawii.app.feature.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.ui.components.AvatarImage
import com.estudiawii.app.ui.components.GlassCard
import com.estudiawii.app.ui.components.GoldPill
import com.estudiawii.app.ui.components.WiButton
import com.estudiawii.app.ui.components.WiField
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText
import com.estudiawii.app.ui.theme.WiThemeColors
import com.estudiawii.app.ui.theme.WiThemes

private enum class AjustesPage { Main, Personal, Notifications, Password }

private data class NotificationPrefs(
    val reminders: Boolean = true,
    val updates: Boolean = true,
    val premium: Boolean = true,
)

private object NotificationPrefsStore {
    private const val PREFS = "EstudiaWii_notification_prefs"
    private const val KEY_REMINDERS = "reminders"
    private const val KEY_UPDATES = "updates"
    private const val KEY_PREMIUM = "premium"

    fun load(context: android.content.Context): NotificationPrefs {
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        return NotificationPrefs(
            reminders = prefs.getBoolean(KEY_REMINDERS, true),
            updates = prefs.getBoolean(KEY_UPDATES, true),
            premium = prefs.getBoolean(KEY_PREMIUM, true),
        )
    }

    fun save(context: android.content.Context, prefs: NotificationPrefs) {
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_REMINDERS, prefs.reminders)
            .putBoolean(KEY_UPDATES, prefs.updates)
            .putBoolean(KEY_PREMIUM, prefs.premium)
            .apply()
    }
}

@Composable
fun Ajustes(
    state: EstudiaWiiUiState,
    viewModel: EstudiaWiiViewModel,
    onNavigate: (WiPage) -> Unit,
    onLogout: () -> Unit,
) {
    var page by remember { mutableStateOf(AjustesPage.Main) }
    val activeTheme by viewModel.activeTheme.collectAsState()

    fun backToMain() {
        page = AjustesPage.Main
    }

    when (page) {
        AjustesPage.Main -> AjustesMain(
            profile = state.profile,
            activeTheme = activeTheme,
            onSelectTheme = { viewModel.changeTheme(it) },
            onPersonal = { page = AjustesPage.Personal },
            onNotifications = { page = AjustesPage.Notifications },
            onPassword = { page = AjustesPage.Password },
            onNavigate = onNavigate,
            onLogout = onLogout,
        )
        AjustesPage.Personal -> SettingsSubPage(onBack = ::backToMain) { PersonalInfo(state.profile) }
        AjustesPage.Notifications -> SettingsSubPage(onBack = ::backToMain) { NotificationSettings() }
        AjustesPage.Password -> SettingsSubPage(onBack = ::backToMain) { PasswordSettings(state.profile, state.authLoading) { viewModel.recoverPassword(it) } }
    }
}

@Composable
private fun AjustesMain(
    profile: Smile?,
    activeTheme: WiThemeColors,
    onSelectTheme: (WiThemeColors) -> Unit,
    onPersonal: () -> Unit,
    onNotifications: () -> Unit,
    onPassword: () -> Unit,
    onNavigate: (WiPage) -> Unit,
    onLogout: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Tarjeta de Perfil Premium
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.78f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(104.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarImage(profile = profile, size = 104.dp)
                    }
                    Text(profile?.nombreCompleto?.ifBlank { profile.usuario } ?: "Usuario EstudiaWii", style = WiText.h2, modifier = Modifier.padding(top = 12.dp))
                    Text(profile?.email.orEmpty(), style = WiText.small, modifier = Modifier.padding(top = 2.dp))
                    
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileChip(profile?.plan?.uppercase().orEmpty().ifBlank { "FREE" }, Icons.Rounded.Verified)
                        ProfileChip(profile?.rol?.lowercase().orEmpty().ifBlank { "usuario" }, Icons.Rounded.Person)
                    }
                }
            }
        }

        // Sección de Selección de Tema Personalizado
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.62f) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Título + nombre del tema activo a la derecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Personalizar tema", style = WiText.h3.copy(color = WiCss.tx1))
                        Text(
                            text = activeTheme.name,
                            style = WiText.tiny.copy(
                                color = activeTheme.mco,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    Text("Selecciona una paleta de color profesional.", style = WiText.tiny, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WiThemes.forEach { theme ->
                            val isSelected = theme.name == activeTheme.name
                            // Color del círculo: Futuro usa su color de fondo oscuro, resto usa mco
                            val circleColor = if (theme.name == "Futuro") Color(0xFF21273B) else theme.mco
                            // Color del check según luminosidad
                            val checkColor = when (theme.name) {
                                "Oro", "Paz" -> Color.Black
                                "Futuro" -> Color.White
                                else -> Color.White
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 46.dp else 42.dp)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            width = 2.5.dp,
                                            color = circleColor,
                                            shape = CircleShape
                                        ) else Modifier
                                    )
                                    .padding(if (isSelected) 3.dp else 0.dp)
                                    .clip(CircleShape)
                                    .background(circleColor)
                                    .clickable { onSelectTheme(theme) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Seleccionado",
                                        tint = checkColor,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tarjeta de Configuración de la Cuenta
        item {
            Text("Ajustes de cuenta", style = WiText.label, color = WiCss.mco, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.58f) {
                Column {
                    SettingsRow(Icons.Rounded.Person, "Informacion personal", profile?.usuario?.let { "@$it" }.orEmpty(), onPersonal)
                    HorizontalDivider(color = WiCss.brd.copy(alpha = 0.22f))
                    SettingsRow(Icons.Rounded.Notifications, "Notificacion", "Mensajes y recordatorios", onNotifications)
                    HorizontalDivider(color = WiCss.brd.copy(alpha = 0.22f))
                    SettingsRow(Icons.Rounded.Password, "Contrasena", "Recuperar acceso por correo", onPassword)
                }
            }
        }

        // Botón de Cierre de Sesión
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(WiCss.white.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        WiCss.error.copy(alpha = 0.34f),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(onClick = onLogout)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = WiCss.error)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesion", style = WiText.h3.copy(color = WiCss.error))
            }
            Text(
                "EstudiaWii v1.0.0 — Plantilla Premium",
                style = WiText.tiny,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            )
        }
    }
}

@Composable
private fun PersonalInfo(profile: Smile?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), intensity = 0.78f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WiCss.mco.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, null, tint = WiCss.mco, modifier = Modifier.size(24.dp))
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Informacion personal", style = WiText.h2)
                        Text("Detalles de tu sesion en la nube.", style = WiText.small)
                    }
                }
            }
        }

        if (profile == null) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("No se pudo cargar la sesion del perfil.", style = WiText.body, textAlign = TextAlign.Center)
                }
            }
        } else {
            item {
                GlassCard(Modifier.fillMaxWidth(), intensity = 0.58f) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoColumn("Correo electronico", profile.email)
                        HorizontalDivider(color = WiCss.brd.copy(alpha = 0.15f))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(Modifier.weight(1f)) { InfoColumn("Nombre", profile.nombre.ifBlank { "No registrado" }) }
                            Box(Modifier.weight(1f)) { InfoColumn("Apellidos", profile.apellidos.ifBlank { "No registrado" }) }
                        }
                        HorizontalDivider(color = WiCss.brd.copy(alpha = 0.15f))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(Modifier.weight(1f)) { InfoColumn("Usuario", "@${profile.usuario}") }
                            Box(Modifier.weight(1f)) { InfoColumn("Rol", profile.rol.lowercase()) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettings() {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(NotificationPrefsStore.load(context)) }

    fun update(next: NotificationPrefs) {
        prefs = next
        NotificationPrefsStore.save(context, next)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), intensity = 0.78f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WiCss.mco.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Notifications, null, tint = WiCss.mco, modifier = Modifier.size(24.dp))
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Notificacion", style = WiText.h2)
                        Text("Controla que avisos quieres recibir.", style = WiText.small)
                    }
                }
            }
        }
        item {
            var notificationMessage by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(notificationMessage) {
                if (notificationMessage != null) {
                    kotlinx.coroutines.delay(3000)
                    notificationMessage = null
                }
            }

            GlassCard(Modifier.fillMaxWidth(), intensity = 0.72f) {
                Text("Simulador de Alertas FCM", style = WiText.h3)
                Text(
                    "Prueba de forma inmediata el canal de notificaciones push de EstudiaWii en tu celular.",
                    style = WiText.small,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )
                
                androidx.compose.material3.Button(
                    onClick = {
                        notificationMessage = "Simulación activada! En 2 segundos recibirás la alerta."
                        val intent = android.content.Intent("com.estudiawii.app.SIMULATE_NOTIFICATION")
                        context.sendBroadcast(intent)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = WiCss.mco),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Notifications, null, tint = WiCss.txa)
                    Spacer(Modifier.width(6.dp))
                    Text("Simular Alerta de Clase", color = WiCss.txa)
                }

                if (notificationMessage != null) {
                    Text(
                        text = notificationMessage.orEmpty(),
                        color = WiCss.mco,
                        style = WiText.body.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.58f) {
                Column {
                    PreferenceRow("Recordatorios", "Avisos diarios y seguimiento suave", prefs.reminders) {
                        update(prefs.copy(reminders = it))
                    }
                    HorizontalDivider(color = WiCss.brd.copy(alpha = 0.22f))
                    PreferenceRow("Novedades", "Actualizaciones importantes de la app", prefs.updates) {
                        update(prefs.copy(updates = it))
                    }
                    HorizontalDivider(color = WiCss.brd.copy(alpha = 0.22f))
                    PreferenceRow("Mensajes premium", "Consejos, logros y contenido especial", prefs.premium) {
                        update(prefs.copy(premium = it))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label.uppercase(), style = WiText.label.copy(color = WiCss.mco, fontWeight = FontWeight.SemiBold))
        Text(
            value,
            style = WiText.body.copy(color = WiCss.tx1, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PasswordSettings(profile: Smile?, loading: Boolean, onRecover: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), intensity = 0.78f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WiCss.mco.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Password, null, tint = WiCss.mco, modifier = Modifier.size(24.dp))
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Contraseña y seguridad", style = WiText.h2)
                        Text("Acciones de proteccion de cuenta.", style = WiText.small)
                    }
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.58f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Correo registrado", style = WiText.h3)
                    Text(profile?.email.orEmpty().ifBlank { "Sin correo registrado" }, style = WiText.body)
                    Spacer(Modifier.height(6.dp))
                    WiButton(
                        text = "Enviar enlace de recuperacion",
                        onClick = { profile?.email?.takeIf { it.isNotBlank() }?.let(onRecover) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Lock,
                        loading = loading
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSubPage(onBack: () -> Unit, content: @Composable () -> Unit) {
    BackHandler(onBack = onBack)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        if (totalDrag > 100f) onBack()
                    },
                )
            },
    ) {
        content()
    }
}

@Composable
private fun ProfileChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(WiCss.mco.copy(alpha = 0.18f))
            .border(1.dp, WiCss.brd.copy(alpha = 0.42f), RoundedCornerShape(99.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(5.dp))
        Text(text, style = WiText.tiny.copy(color = WiCss.mco, fontWeight = FontWeight.Bold), maxLines = 1)
    }
}

@Composable
private fun PreferenceRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = WiText.h3.copy(fontSize = 17.sp))
            Text(subtitle, style = WiText.small, modifier = Modifier.padding(top = 3.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WiCss.mco.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = WiText.h3.copy(fontSize = 17.sp))
            if (subtitle.isNotBlank()) Text(subtitle, style = WiText.small, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Rounded.PlayArrow, null, tint = WiCss.mco.copy(alpha = 0.50f), modifier = Modifier.size(16.dp))
    }
}
