package com.estudiawii.app.feature.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estudiawii.app.R
import com.estudiawii.app.core.auth.AuthRepository
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.ui.components.GlassCard
import com.estudiawii.app.ui.components.GoldPill
import com.estudiawii.app.ui.components.WiButton
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiText
import com.estudiawii.app.ui.theme.WiThemeColors
import com.estudiawii.app.ui.theme.WiiDisplayFontFamily
import com.estudiawii.app.ui.theme.WiiFontFamily
import com.estudiawii.app.ui.theme.premiumBackground
import kotlinx.coroutines.launch

private enum class AuthMode { Login, Register, Recover, GoogleProfile }

@Composable
fun AuthScreen(
    auth: AuthRepository,
    activeTheme: WiThemeColors,
    onAuthenticated: (Smile) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var mode by remember { mutableStateOf(AuthMode.Login) }
    var loading by remember { mutableStateOf(false) }
    var emailOrUser by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var googleEmail by remember { mutableStateOf("") }

    fun show(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    fun runAuth(block: suspend () -> Smile) {
        scope.launch {
            loading = true
            runCatching { block() }
                .onSuccess(onAuthenticated)
                .onFailure { show(it.message ?: "No se pudo completar la accion") }
            loading = false
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            show("Google cancelo el inicio de sesion")
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            loading = true
            runCatching { auth.loginWithGoogle(result.data) }
                .onSuccess { profile ->
                    if (profile == null) {
                        googleEmail = auth.currentEmail.orEmpty()
                        usuario = ""
                        acceptedTerms = false
                        mode = AuthMode.GoogleProfile
                    } else {
                        onAuthenticated(profile)
                    }
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true ->
                            "Error de configuracion Google. Verifica el SHA-1 en Firebase Console."
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Sin conexion a internet. Verifica tu red."
                        else -> e.message ?: "Google no pudo iniciar sesion"
                    }
                    show(msg)
                }
            loading = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .premiumBackground()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = CircleShape,
                        ambientColor = WiCss.mco.copy(alpha = 0.28f),
                        spotColor = WiCss.mco.copy(alpha = 0.45f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Anillo exterior de gradiente difuso
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(
                            Brush.radialGradient(listOf(WiCss.mco.copy(alpha = 0.55f), WiCss.mco.copy(alpha = 0.18f))),
                            CircleShape,
                        ),
                )
                // Logo clipeado
                Image(
                    painter = painterResource(R.drawable.blankwii_logo),
                    contentDescription = "EstudiaWii",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text("EstudiaWii", fontFamily = WiiDisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = WiCss.mco, textAlign = TextAlign.Center)
            Text("Aplicación premium para estudiar excelente", fontFamily = WiiDisplayFontFamily, fontSize = 15.sp, color = WiCss.tx1, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            GlassCard(Modifier.fillMaxWidth(), intensity = 0.72f) {
                AnimatedContent(targetState = mode, label = "auth-mode") { currentMode ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        when (currentMode) {
                            AuthMode.Login -> {
                                AuthTitle("Bienvenido", "Ingresa para acceder funciones muy personalizadas.", centered = true)
                                GoogleAccessButton(loading = loading) {
                                    runCatching { googleLauncher.launch(auth.googleIntent(context)) }
                                        .onFailure { show(it.message ?: "Google no esta disponible") }
                                }
                                AuthField(emailOrUser, { emailOrUser = it }, "Email o usuario", Icons.Rounded.Email)
                                AuthPassword(password, { password = it }, "Contrasena")
                                WiButton("Entrar", onClick = {
                                    runAuth { auth.login(emailOrUser, password) }
                                }, loading = loading, modifier = Modifier.fillMaxWidth())
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AuthTextAction("Olvide mi contrasena", Icons.AutoMirrored.Rounded.Help) { mode = AuthMode.Recover }
                                    AuthTextAction("Crear cuenta nueva", Icons.Rounded.PersonAdd) { mode = AuthMode.Register }
                                }
                            }

                            AuthMode.Register -> {
                                AuthTitle("Crear cuenta", "Prepara tu perfil para personalizar la app.", centered = true)
                                GoogleAccessButton(loading = loading) {
                                    runCatching { googleLauncher.launch(auth.googleIntent(context)) }
                                        .onFailure { show(it.message ?: "Google no esta disponible") }
                                }
                                AuthField(usuario, { usuario = it }, "Usuario", Icons.Rounded.Badge)
                                AuthField(email, { email = it }, "Email", Icons.Rounded.Email, keyboardType = KeyboardType.Email)
                                AuthField(nombre, { nombre = it }, "Nombre", Icons.Rounded.Person)
                                AuthField(apellidos, { apellidos = it }, "Apellidos", Icons.Rounded.Person)
                                AuthPassword(password, { password = it }, "Contrasena")
                                AuthPassword(confirmPassword, { confirmPassword = it }, "Confirmar contrasena")
                                TermsRow(
                                    checked = acceptedTerms,
                                    onCheckedChange = { acceptedTerms = it },
                                    onTerms = { uriHandler.openUri("https://EstudiaWii.web.app/terminos") },
                                    onPrivacy = { uriHandler.openUri("https://EstudiaWii.web.app/privacidad") },
                                )
                                WiButton("Crear cuenta", onClick = {
                                    when {
                                        password != confirmPassword -> show("Las contrasenas no coinciden")
                                        !acceptedTerms -> show("Acepta terminos y privacidad")
                                        else -> runAuth { auth.register(usuario, nombre, apellidos, email, password) }
                                    }
                                }, loading = loading, modifier = Modifier.fillMaxWidth())
                                AuthTextAction("Ya tengo cuenta", Icons.Rounded.Person, Modifier.align(Alignment.CenterHorizontally)) { mode = AuthMode.Login }
                            }

                            AuthMode.Recover -> {
                                AuthTitle("Recuperar acceso", "Te enviaremos un enlace al correo.", centered = true)
                                AuthField(email, { email = it }, "Email", Icons.Rounded.Email, keyboardType = KeyboardType.Email)
                                WiButton("Enviar enlace", onClick = {
                                    scope.launch {
                                        loading = true
                                        runCatching { auth.recover(email) }
                                            .onSuccess {
                                                show("Listo, revisa tu correo")
                                                mode = AuthMode.Login
                                            }
                                            .onFailure { show(it.message ?: "No se pudo enviar el correo") }
                                        loading = false
                                    }
                                }, loading = loading, modifier = Modifier.fillMaxWidth())
                                AuthTextAction("Volver a ingresar", Icons.Rounded.Person, Modifier.align(Alignment.CenterHorizontally)) { mode = AuthMode.Login }
                            }

                            AuthMode.GoogleProfile -> {
                                AuthTitle("Casi listo", "Elige tu usuario y acepta los terminos para guardar tu perfil.", centered = true)
                                if (googleEmail.isNotBlank()) {
                                    GoldPill(
                                        text = googleEmail,
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                    )
                                }
                                AuthField(
                                    value = usuario,
                                    onValueChange = { v ->
                                        usuario = v.lowercase()
                                            .filter { it.isLetterOrDigit() || it == '_' || it == '-' }
                                            .take(32)
                                    },
                                    label = "Usuario",
                                    icon = Icons.Rounded.Badge,
                                )
                                if (usuario.isNotBlank() && usuario.length < 4) {
                                    Text(
                                        "Minimo 4 caracteres (letras, numeros, _ o -)",
                                        style = WiText.small,
                                        color = WiCss.error,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                TermsRow(
                                    checked = acceptedTerms,
                                    onCheckedChange = { acceptedTerms = it },
                                    onTerms = { uriHandler.openUri("https://EstudiaWii.web.app/terminos") },
                                    onPrivacy = { uriHandler.openUri("https://EstudiaWii.web.app/privacidad") },
                                )
                                WiButton("Guardar perfil", onClick = {
                                    when {
                                        usuario.length < 4 -> show("El usuario necesita al menos 4 caracteres")
                                        !acceptedTerms -> show("Acepta terminos y privacidad")
                                        else -> runAuth { auth.completeGoogleRegistration(usuario) }
                                    }
                                }, loading = loading, modifier = Modifier.fillMaxWidth())
                                AuthTextAction(
                                    "Cancelar y volver",
                                    Icons.Rounded.Person,
                                    Modifier.align(Alignment.CenterHorizontally),
                                ) {
                                    auth.logout(context)
                                    googleEmail = ""
                                    usuario = ""
                                    acceptedTerms = false
                                    mode = AuthMode.Login
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(34.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { uriHandler.openUri("https://wtaype.me/") }
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("2026 Creado con ", style = WiText.small, color = WiCss.tx2)
                Icon(Icons.Rounded.Favorite, null, tint = WiCss.mco, modifier = Modifier.size(15.dp))
                Text(" por Wilder Taype", style = WiText.small, color = WiCss.tx2, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(28.dp))
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun AuthTitle(title: String, subtitle: String, centered: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            title,
            fontFamily = WiiDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = WiCss.tx1,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            subtitle,
            fontFamily = WiiDisplayFontFamily,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = WiCss.tx1,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GoogleAccessButton(loading: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            onClick = onClick,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(CircleShape)
                .border(1.dp, WiCss.brd.copy(alpha = 0.55f), CircleShape),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
        ) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(R.drawable.ic_google_logo), contentDescription = "Google", modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Text("Continua con Google", fontFamily = WiiDisplayFontFamily, fontWeight = FontWeight.SemiBold, color = WiCss.tx1)
            }
        }
    }
}

@Composable
private fun AuthTextAction(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontFamily = WiiDisplayFontFamily, fontWeight = FontWeight.SemiBold, color = WiCss.mco)
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = WiiDisplayFontFamily) },
        leadingIcon = { Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
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

@Composable
private fun AuthPassword(value: String, onValueChange: (String) -> Unit, label: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = WiiDisplayFontFamily) },
        leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = WiCss.mco, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = WiCss.mco)
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
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

@Composable
private fun TermsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = WiCss.mco),
        )
        Spacer(Modifier.width(4.dp))
        Text("Acepto ", style = WiText.small, color = WiCss.tx1)
        Text(
            "terminos",
            style = WiText.small,
            color = WiCss.mco,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onTerms),
        )
        Text(" y ", style = WiText.small, color = WiCss.tx1)
        Text(
            "privacidad",
            style = WiText.small,
            color = WiCss.mco,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onPrivacy),
        )
    }
}
