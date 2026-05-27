package com.estudiawii.app.feature.shell

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.estudiawii.app.R
import com.estudiawii.app.core.course.*
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.core.streak.StreakState
import com.estudiawii.app.ui.components.*
import com.estudiawii.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun TemplatePage(
    page: WiPage,
    state: EstudiaWiiUiState,
    streak: StreakState,
    viewModel: EstudiaWiiViewModel,
    onNavigate: (WiPage) -> Unit,
    onLogout: () -> Unit
) {
    when (page) {
        WiPage.Inicio -> HomePage(state, streak, onNavigate)
        WiPage.Cursos -> CursosPage(state, viewModel)
        WiPage.Live -> LivePage(state)
        WiPage.Meses -> MesesPage(state)
        WiPage.Ajustes -> Ajustes(state, viewModel, onNavigate, onLogout)
        WiPage.Racha -> RachaPage(streak)
        else -> InfoPage(page)
    }
}

// ==========================================
// 1. HOME / INICIO SCREEN
// ==========================================
@Composable
private fun HomePage(state: EstudiaWiiUiState, streak: StreakState, onNavigate: (WiPage) -> Unit) {
    val today = LocalDate.now()
    val currentDayOfWeek = today.dayOfWeek.value
    val todayEpoch = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    
    // Obtener la siguiente clase de hoy (si hay alguna)
    val classesToday = state.courses.flatMap { course ->
        course.schedules.filter { 
            it.dayOfWeek == currentDayOfWeek && todayEpoch >= it.desde && todayEpoch <= it.hasta
        }.map { Pair(course, it) }
    }.sortedBy { it.second.startTime }

    val nextClass = classesToday.find {
        val parsedTime = runCatching { LocalTime.parse(it.second.startTime) }.getOrDefault(LocalTime.MAX)
        LocalTime.now().isBefore(parsedTime)
    } ?: classesToday.firstOrNull()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera con saludo del estudiante
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.78f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        GoldPill(wiDia())
                        Text(
                            text = "${saludar()}, ${state.profile?.nombre?.ifBlank { state.profile.usuario } ?: "Estudiante"}",
                            style = WiText.h1.copy(color = WiCss.tx1),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            text = "Bienvenido a EstudiaWii. Tu espacio de organización rápido, fluido y eficiente.",
                            style = WiText.body,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Box(
                        Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(WiCss.mco.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.blankwii_logo),
                            contentDescription = null,
                            modifier = Modifier.size(46.dp).clip(CircleShape)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickStat(state.courses.size.toString(), "Cursos", Icons.Rounded.Widgets, Modifier.weight(1f))
                    QuickStat("${streak.current} días", "Racha", Icons.Rounded.LocalFireDepartment, Modifier.weight(1f))
                    QuickStat("FCM", "Listo", Icons.Rounded.NotificationsActive, Modifier.weight(1f))
                }
            }
        }

        // Tarjeta con información de la siguiente clase
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.72f) {
                Text("Mi próxima sesión", style = WiText.label, color = WiCss.mco)
                if (nextClass != null) {
                    val (course, schedule) = nextClass
                    Text(course.name, style = WiText.h2, modifier = Modifier.padding(top = 6.dp))
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(course.color))))
                        Text(
                            text = "Hoy a las ${schedule.startTime} - ${schedule.endTime}",
                            style = WiText.body.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Text(
                        text = "Impartido por ${course.teacher}",
                        style = WiText.small,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text("No hay clases programadas hoy", style = WiText.h3, modifier = Modifier.padding(top = 6.dp))
                    Text("¡Aprovecha este tiempo libre para repasar tus apuntes!", style = WiText.body, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // Accesos directos rápidos
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ActionTile("Ver Horario", "Clases de hoy", Icons.Rounded.PlayCircle, { onNavigate(WiPage.Live) }, Modifier.weight(1f))
                ActionTile("Calendario", "Plan mensual", Icons.Rounded.DateRange, { onNavigate(WiPage.Meses) }, Modifier.weight(1f))
            }
        }

        item {
            ActionTile("Mis Asignaturas", "Gestionar cursos", Icons.Rounded.Widgets, { onNavigate(WiPage.Cursos) }, Modifier.fillMaxWidth())
        }
    }
}

// ==========================================
// 2. CURSOS / ASSIGNMENTS SCREEN
// ==========================================
@Composable
private fun CursosPage(state: EstudiaWiiUiState, viewModel: EstudiaWiiViewModel) {
    var editingCurso by remember { mutableStateOf<Curso?>(null) }
    var addingCurso by remember { mutableStateOf<Boolean>(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(Modifier.fillMaxWidth(), intensity = 0.78f) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GoldPill("Cursos")
                        Text(
                            text = "Agrega tus Cursos",
                            style = WiText.h1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            text = "Organiza tus asignaturas, añade tus enlaces de clases y configura tus horarios fácilmente. Todo sincronizado en tiempo real.",
                            style = WiText.body,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(14.dp))
                    
                    Button(
                        onClick = { addingCurso = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WiCss.mco),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, null, tint = WiCss.txa)
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar Nueva Asignatura", style = WiText.h3.copy(color = WiCss.txa))
                    }
                }
            }

            if (state.coursesLoading) {
                item {
                    SkeletonCursosPage()
                }
            } else if (state.courses.isEmpty()) {
                item {
                    GlassCard(Modifier.fillMaxWidth(), intensity = 0.58f) {
                        Text(
                            "Aún no tienes asignaturas registradas.",
                            style = WiText.h3,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    }
                }
            } else {
                items(state.courses) { course ->
                    CourseCard(
                        course = course,
                        onEdit = { editingCurso = course },
                        onDelete = { viewModel.deleteCourse(course.id) }
                    )
                }
            }
        }

        // Hoja deslizable de Agregar (Registro Rápido)
        if (addingCurso) {
            CourseSheet(
                curso = null,
                onDismiss = { addingCurso = false },
                onSave = { course ->
                    viewModel.addCourse(course)
                    addingCurso = false
                }
            )
        }

        // Hoja deslizable de Editar (Edición Completa)
        if (editingCurso != null) {
            CourseSheet(
                curso = editingCurso,
                onDismiss = { editingCurso = null },
                onSave = { course ->
                    viewModel.addCourse(course)
                    editingCurso = null
                }
            )
        }
    }
}

private fun paraId(courseName: String): String {
    val firstWord = courseName.trim().split("\\s+".toRegex()).firstOrNull()?.lowercase() ?: "curso"
    val normalized = java.text.Normalizer.normalize(firstWord, java.text.Normalizer.Form.NFD)
    val withoutAccents = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    val cleanWord = withoutAccents.filter { it.isLetterOrDigit() }.ifBlank { "curso" }
    return "$cleanWord${System.currentTimeMillis()}"
}

private fun autoDetectPlatform(url: String): String {
    val clean = url.trim().lowercase()
    return when {
        clean.contains("youtube.com") || clean.contains("youtu.be") -> "YouTube"
        clean.contains("zoom.us") -> "Zoom"
        clean.contains("whatsapp.com") || clean.contains("chat.whatsapp.com") -> "WhatsApp"
        clean.contains("meet.google.com") -> "Google Meet"
        clean.contains("pit-virtual") -> "PIT Virtual"
        clean.contains("drive.google.com") -> "Google Drive"
        clean.contains("classroom.google.com") -> "Classroom"
        else -> ""
    }
}

private fun getLinkIcon(nombre: String): ImageVector {
    val name = nombre.lowercase()
    return when {
        name.contains("pit") -> Icons.Rounded.School
        name.contains("zoom") -> Icons.Rounded.Videocam
        name.contains("meet") -> Icons.Rounded.VideoCall
        name.contains("youtube") -> Icons.Rounded.PlayCircle
        name.contains("whatsapp") -> Icons.Rounded.Chat
        name.contains("asistencia") -> Icons.Rounded.Assignment
        name.contains("drive") -> Icons.Rounded.Cloud
        name.contains("classroom") -> Icons.Rounded.Class
        else -> Icons.Rounded.Link
    }
}

@Composable
private fun CourseCard(course: Curso, onEdit: () -> Unit, onDelete: () -> Unit) {
    val df = remember { java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val defaultColor = WiCss.mco
    val courseColor = remember(course.color, defaultColor) {
        runCatching { Color(android.graphics.Color.parseColor(course.color)) }.getOrDefault(defaultColor)
    }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .softGlassShadow(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WiCss.wb, contentColor = WiCss.tx1),
        border = BorderStroke(1.5.dp, WiCss.mco.copy(alpha = 0.8f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(courseColor)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(course.nombre, style = WiText.h2)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Docente: ${course.docente}", style = WiText.small)
                        Spacer(Modifier.width(8.dp))
                        Text("•", style = WiText.small)
                        Spacer(Modifier.width(8.dp))
                        Text("Ciclo: ${course.ciclo}", style = WiText.small)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, "Editar", tint = WiCss.mco, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, "Eliminar", tint = WiCss.error, modifier = Modifier.size(20.dp))
                }
            }
            
            if (course.notas.isNotBlank()) {
                Text(
                    text = "\"${course.notas}\"",
                    style = WiText.body.copy(color = WiCss.tx2.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(WiCss.softSurface(0.12f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }

            // Barra inferior de enlaces directos dinámicos en formato Píldoras {icono} {nombre}
            val linksList = course.links
            if (linksList.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    linksList.forEach { link ->
                        val url = link.url.trim()
                        val hasLink = url.isNotBlank()
                        val icon = getLinkIcon(link.nombre)
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (hasLink) courseColor.copy(alpha = 0.12f) else WiCss.softSurface(0.08f))
                                .border(
                                    width = 1.dp,
                                    color = if (hasLink) courseColor.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = hasLink) {
                                    runCatching { uriHandler.openUri(url) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = link.nombre,
                                tint = if (hasLink) courseColor else WiCss.tx3.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = link.nombre,
                                style = WiText.tiny.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasLink) courseColor else WiCss.tx3.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }

            if (course.horarios.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = WiCss.brd.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))
                Text("Horarios programados:", style = WiText.label)
                course.horarios.forEach { schedule ->
                    val desdeStr = runCatching { 
                        java.time.Instant.ofEpochMilli(schedule.desde)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .format(df)
                    }.getOrDefault("Inicio")
                    val hastaStr = runCatching { 
                        java.time.Instant.ofEpochMilli(schedule.hasta)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .format(df)
                    }.getOrDefault("Fin")
                    
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(courseColor)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(schedule.nombreDia, style = WiText.body.copy(fontWeight = FontWeight.Bold))
                            }
                            Text("${schedule.inicio} - ${schedule.fin}", style = WiText.small.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Text(
                            text = "Vigencia: $desdeStr al $hastaStr",
                            style = WiText.tiny.copy(color = WiCss.tx3),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseSheetPreview(
    nombre: String,
    docente: String,
    ciclo: String,
    colorHex: String
) {
    val defaultColor = WiCss.mco
    val courseColor = remember(colorHex, defaultColor) {
        runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(defaultColor)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = courseColor.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, courseColor.copy(alpha = 0.78f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(courseColor)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = nombre.trim().split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.take(1).uppercase() }
                    .ifBlank { "W" }
                
                Text(
                    text = initials,
                    style = WiText.h2.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }
            
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nombre.ifBlank { "Nueva Asignatura" },
                    style = WiText.h2.copy(color = WiCss.tx1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Docente: ${docente.ifBlank { "Por asignar" }}",
                    style = WiText.body.copy(color = WiCss.tx2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ciclo: $ciclo",
                    style = WiText.small.copy(color = WiCss.tx3),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ColorChooser(selectedColor: String, onSelect: (String) -> Unit) {
    val colors = listOf(
        "#2563EB" to "Azul",
        "#10B981" to "Paz",
        "#8B5CF6" to "Mora",
        "#EF4444" to "Dulce",
        "#F59E0B" to "Oro",
        "#EC4899" to "Rosado"
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { (colorHex, _) ->
            val color = Color(android.graphics.Color.parseColor(colorHex))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selectedColor.equals(colorHex, true)) 3.dp else 0.dp,
                        color = if (selectedColor.equals(colorHex, true)) Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorHex) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseSheet(
    curso: Curso?,
    onDismiss: () -> Unit,
    onSave: (Curso) -> Unit
) {
    val isEditing = curso != null
    val context = LocalContext.current
    val df = remember { java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var dayClickedTrigger by remember { mutableStateOf(0) }
    
    val premiumColors = listOf(
        "#2563EB", // Azul Real
        "#10B981", // Verde Esmeralda
        "#8B5CF6", // Violeta Profundo
        "#EF4444", // Rojo Coral
        "#F59E0B", // Ámbar Cálido
        "#EC4899", // Rosa Intenso
        "#06B6D4", // Turquesa
        "#F43F5E"  // Rosa Fresa
    )
    val defaultColor = remember { premiumColors.random() }
    
    // Estados del curso
    var nombre by remember(curso) { mutableStateOf(curso?.nombre.orEmpty()) }
    var docente by remember(curso) { mutableStateOf(curso?.docente.orEmpty()) }
    var color by remember(curso) { mutableStateOf(curso?.color ?: defaultColor) }
    var ciclo by remember(curso) { mutableStateOf(curso?.ciclo ?: "2026-I") }
    var estado by remember(curso) { mutableStateOf(curso?.estado ?: "activo") }
    var notas by remember(curso) { mutableStateOf(curso?.notas.orEmpty()) }
    
    // Lista dinámica de enlaces (vacía por defecto para nuevos cursos, las sugerencias van abajo)
    val tempLinks = remember(curso) {
        val list = mutableStateListOf<EnlaceCurso>()
        curso?.links?.let { list.addAll(it) }
        list
    }
    
    var newLinkUrl by remember { mutableStateOf("") }
    var newLinkName by remember { mutableStateOf("") }

    // Inicialización síncrona optimizada de mapas para evitar latencia (edit/carga instantánea)
    val nowTime = remember { java.time.LocalTime.now() }
    val defaultInicio = remember { String.format("%02d:%02d", nowTime.hour, nowTime.minute) }
    val defaultFin = remember { String.format("%02d:%02d", nowTime.plusHours(2).hour, nowTime.plusHours(2).minute) }

    val selectedDays = remember(curso) {
        val map = mutableStateMapOf<Int, Boolean>()
        for (i in 1..7) map[i] = false
        curso?.horarios?.forEach { map[it.dia] = true }
        map
    }

    val dayStartTimes = remember(curso) {
        val map = mutableStateMapOf<Int, String>()
        for (i in 1..7) map[i] = defaultInicio
        curso?.horarios?.forEach { map[it.dia] = it.inicio }
        map
    }

    val dayEndTimes = remember(curso) {
        val map = mutableStateMapOf<Int, String>()
        for (i in 1..7) map[i] = defaultFin
        curso?.horarios?.forEach { map[it.dia] = it.fin }
        map
    }

    val dayDesdeDates = remember(curso) {
        val map = mutableStateMapOf<Int, Long>()
        val now = System.currentTimeMillis()
        for (i in 1..7) map[i] = now
        curso?.horarios?.forEach { map[it.dia] = it.desde }
        map
    }

    val dayHastaDates = remember(curso) {
        val map = mutableStateMapOf<Int, Long>()
        val defaultHasta = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L
        for (i in 1..7) map[i] = defaultHasta
        curso?.horarios?.forEach { map[it.dia] = it.hasta }
        map
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeTheme = LocalWiThemeColors.current
    val coroutineScope = rememberCoroutineScope()

    // Cierre animado: slide-down suave antes de ejecutar onDismiss
    val animateDismiss: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            onDismiss()
        }
    }

    // Guardado animado: slide-down, luego guarda y cierra
    val animateSave: (Curso) -> Unit = { finalCurso ->
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            onSave(finalCurso)
        }
    }

    val cColor = remember(color) {
        runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(activeTheme.mco)
    }

    // Helper reusable para colores de inputs adaptados al tema
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = cColor,
        unfocusedBorderColor = activeTheme.brd.copy(alpha = 0.60f),
        focusedContainerColor = activeTheme.inp.copy(alpha = 0.80f),
        unfocusedContainerColor = activeTheme.inp.copy(alpha = 0.50f),
        focusedTextColor = activeTheme.tx1,
        unfocusedTextColor = activeTheme.tx1,
        focusedLabelColor = cColor,
        unfocusedLabelColor = activeTheme.tx3,
        cursorColor = cColor
    )

    val lazyListState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = { animateDismiss() },
        sheetState = sheetState,
        containerColor = activeTheme.bg, // Coloreado con el hermoso fondo del tema activo
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(activeTheme.brd.copy(alpha = 0.5f))
            )
        }
    ) {
        EstudiaWiiTheme(activeTheme) {
            val checkedDays by remember {
                derivedStateOf { selectedDays.filter { it.value }.keys.sorted() }
            }

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Cabecera
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEditing) "Editar Curso" else "Nueva Asignatura",
                        style = WiText.h1
                    )
                    IconButton(
                        onClick = { animateDismiss() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WiCss.softSurface(0.1f))
                    ) {
                        Icon(Icons.Rounded.Close, "Cerrar")
                    }
                }
            }

            // Vista previa reactiva
            item {
                CourseSheetPreview(
                    nombre = nombre,
                    docente = docente,
                    ciclo = ciclo,
                    colorHex = color
                )
            }

            // Nombre y Docente lado a lado (2 Columnas)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Curso") },
                        placeholder = { Text("Ej. Matemática II") },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrect = true
                        )
                    )
                    OutlinedTextField(
                        value = docente,
                        onValueChange = { docente = it },
                        label = { Text("Docente") },
                        placeholder = { Text("Ej. Ramos") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrect = true
                        )
                    )
                }
            }

            // Días de clases: Fila compacta de Iniciales (L, M, M, J, V, S, D)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Días de clases:", style = WiText.label)
                    val daysOfWeek = listOf(
                        1 to "L",
                        2 to "M",
                        3 to "M",
                        4 to "J",
                        5 to "V",
                        6 to "S",
                        7 to "D"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        daysOfWeek.forEach { (dayNum, initial) ->
                            val isChecked = selectedDays[dayNum] ?: false
                            val cColor = runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(WiCss.mco)
                            
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) cColor else WiCss.softSurface(0.12f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isChecked) Color.Transparent else WiCss.brd.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedDays[dayNum] = !isChecked
                                        dayClickedTrigger++
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = WiText.body.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChecked) Color.White else WiCss.tx1
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Horarios compactos para días seleccionados con scroll interno e inteligente
            if (checkedDays.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Horarios configurados:", style = WiText.label)
                        
                        // Smart Scroll Effect: Al cambiar el día activado mediante clic, se mueve al final automáticamente
                        val scrollState = rememberScrollState()
                        LaunchedEffect(dayClickedTrigger) {
                            if (dayClickedTrigger > 0) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WiCss.softSurface(0.06f))
                                .border(1.dp, WiCss.brd.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                checkedDays.forEach { dayNum ->
                                    val dayName = when (dayNum) {
                                        1 -> "Lunes"
                                        2 -> "Martes"
                                        3 -> "Miércoles"
                                        4 -> "Jueves"
                                        5 -> "Viernes"
                                        6 -> "Sábado"
                                        7 -> "Domingo"
                                        else -> "Día"
                                    }
                                    val cColor = runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(WiCss.mco)
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, cColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = cColor.copy(alpha = 0.04f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Horario $dayName",
                                                    style = WiText.body.copy(fontWeight = FontWeight.Bold, color = cColor)
                                                )
                                                IconButton(
                                                    onClick = { selectedDays[dayNum] = false },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Delete, null, tint = WiCss.error, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            
                                            // Inicio y Fin lado a lado
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val inicioVal = dayStartTimes[dayNum] ?: "08:30"
                                                OutlinedTextField(
                                                    value = inicioVal,
                                                    onValueChange = { dayStartTimes[dayNum] = it },
                                                    label = { Text("Inicio") },
                                                    placeholder = { Text("08:30") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    singleLine = true,
                                                    colors = fieldColors,
                                                    trailingIcon = {
                                                        IconButton(
                                                            onClick = {
                                                                val parts = inicioVal.split(":")
                                                                val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                                                                android.app.TimePickerDialog(context, { _, hour, minute ->
                                                                    dayStartTimes[dayNum] = String.format("%02d:%02d", hour, minute)
                                                                }, h, m, true).show()
                                                            }
                                                        ) {
                                                            Icon(Icons.Rounded.AccessTime, null, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                )
                                                
                                                val finVal = dayEndTimes[dayNum] ?: "10:00"
                                                OutlinedTextField(
                                                    value = finVal,
                                                    onValueChange = { dayEndTimes[dayNum] = it },
                                                    label = { Text("Fin") },
                                                    placeholder = { Text("10:00") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    singleLine = true,
                                                    colors = fieldColors,
                                                    trailingIcon = {
                                                        IconButton(
                                                            onClick = {
                                                                val parts = finVal.split(":")
                                                                val h = parts.getOrNull(0)?.toIntOrNull() ?: 10
                                                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                                                android.app.TimePickerDialog(context, { _, hour, minute ->
                                                                    dayEndTimes[dayNum] = String.format("%02d:%02d", hour, minute)
                                                                }, h, m, true).show()
                                                            }
                                                        ) {
                                                            Icon(Icons.Rounded.AccessTime, null, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                )
                                            }
                                            
                                            // Desde y Hasta (Fechas de vigencia) lado a lado
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val desdeVal = dayDesdeDates[dayNum] ?: System.currentTimeMillis()
                                                val desdeStr = remember(desdeVal) {
                                                    java.time.Instant.ofEpochMilli(desdeVal)
                                                        .atZone(java.time.ZoneId.systemDefault())
                                                        .toLocalDate()
                                                        .format(df)
                                                }
                                                OutlinedTextField(
                                                    value = desdeStr,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Desde") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    singleLine = true,
                                                    colors = fieldColors,
                                                    trailingIcon = {
                                                        IconButton(
                                                            onClick = {
                                                                val cal = java.util.Calendar.getInstance()
                                                                cal.timeInMillis = desdeVal
                                                                android.app.DatePickerDialog(
                                                                    context,
                                                                    { _, y, m, d ->
                                                                        val newCal = java.util.Calendar.getInstance()
                                                                        newCal.set(y, m, d)
                                                                        dayDesdeDates[dayNum] = newCal.timeInMillis
                                                                    },
                                                                    cal.get(java.util.Calendar.YEAR),
                                                                    cal.get(java.util.Calendar.MONTH),
                                                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                                                ).show()
                                                            }
                                                        ) {
                                                            Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                )
                                                
                                                val hastaVal = dayHastaDates[dayNum] ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)
                                                val hastaStr = remember(hastaVal) {
                                                    java.time.Instant.ofEpochMilli(hastaVal)
                                                        .atZone(java.time.ZoneId.systemDefault())
                                                        .toLocalDate()
                                                        .format(df)
                                                }
                                                OutlinedTextField(
                                                    value = hastaStr,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Hasta") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    singleLine = true,
                                                    colors = fieldColors,
                                                    trailingIcon = {
                                                        IconButton(
                                                            onClick = {
                                                                val cal = java.util.Calendar.getInstance()
                                                                cal.timeInMillis = hastaVal
                                                                android.app.DatePickerDialog(
                                                                    context,
                                                                    { _, y, m, d ->
                                                                        val newCal = java.util.Calendar.getInstance()
                                                                        newCal.set(y, m, d)
                                                                        dayHastaDates[dayNum] = newCal.timeInMillis
                                                                    },
                                                                    cal.get(java.util.Calendar.YEAR),
                                                                    cal.get(java.util.Calendar.MONTH),
                                                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                                                ).show()
                                                            }
                                                        ) {
                                                            Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Enlaces Dinámicos
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enlaces de videoconferencia y campus:", style = WiText.label)
                    
                    // Sugerencias de enlaces rápidos en formato de chips premium
                    val suggestions = listOf(
                        "PIT Virtual" to "https://pit-virtual.uni.edu.pe/my/",
                        "Zoom" to "",
                        "Google Meet" to "",
                        "WhatsApp" to "",
                        "YouTube" to ""
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        suggestions.forEach { (name, url) ->
                            val isPillActive = tempLinks.any { it.nombre.lowercase() == name.lowercase() }
                            val cColor = runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(WiCss.mco)
                            
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPillActive) cColor.copy(alpha = 0.18f) else WiCss.softSurface(0.08f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isPillActive) cColor else WiCss.brd.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        // SIEMPRE pre-llenamos los campos para revisión del usuario
                                        // (incluso PIT Virtual muestra sus campos antes de añadir)
                                        newLinkName = name
                                        newLinkUrl = url // Puede estar en blanco (ej. Zoom)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPillActive) Icons.Rounded.Check else Icons.Rounded.Add,
                                    contentDescription = name,
                                    tint = if (isPillActive) cColor else WiCss.tx3,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = name,
                                    style = WiText.tiny.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPillActive) cColor else WiCss.tx2
                                    )
                                )
                            }
                        }
                    }
                    
                    // Chips de enlaces agregados
                    if (tempLinks.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tempLinks.forEachIndexed { index, link ->
                                val icon = getLinkIcon(link.nombre)
                                val cColor = runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(WiCss.mco)
                                
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cColor.copy(alpha = 0.12f))
                                        .border(1.dp, cColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable { tempLinks.removeAt(index) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(icon, null, tint = cColor, modifier = Modifier.size(14.dp))
                                    Text(link.nombre, style = WiText.small.copy(fontWeight = FontWeight.Bold))
                                    Icon(Icons.Rounded.Close, null, tint = WiCss.error, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // Input inteligente de enlace con auto-detección
                    OutlinedTextField(
                        value = newLinkUrl,
                        onValueChange = { url ->
                            newLinkUrl = url
                            val detected = autoDetectPlatform(url)
                            if (detected.isNotEmpty()) {
                                newLinkName = detected
                            }
                        },
                        label = { Text("Agregar Enlace (URL)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = fieldColors,
                        trailingIcon = {
                            if (newLinkUrl.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val cleanName = newLinkName.ifBlank {
                                            autoDetectPlatform(newLinkUrl).ifBlank { "Enlace" }
                                        }
                                        tempLinks.add(EnlaceCurso(cleanName, newLinkUrl))
                                        newLinkUrl = ""
                                        newLinkName = ""
                                    },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WiCss.mco)
                                ) {
                                    Icon(Icons.Rounded.AddLink, null, tint = WiCss.txa, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    )

                    // Nombre personalizado opcional si se está escribiendo la URL
                    AnimatedVisibility(visible = newLinkUrl.isNotBlank() || newLinkName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newLinkName,
                                onValueChange = { newLinkName = it },
                                label = { Text("Nombre del enlace") },
                                placeholder = { Text("Ej. Zoom, WhatsApp, Drive") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    autoCorrect = true
                                )
                            )
                            Button(
                                onClick = {
                                    val cleanName = newLinkName.ifBlank {
                                        autoDetectPlatform(newLinkUrl).ifBlank { "Enlace" }
                                    }
                                    tempLinks.add(EnlaceCurso(cleanName, newLinkUrl))
                                    newLinkUrl = ""
                                    newLinkName = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WiCss.mco),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Check, null, tint = WiCss.txa, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Añadir", color = WiCss.txa, style = WiText.small)
                            }
                        }
                    }
                }
            }

            // HABILITADO EXCLUSIVAMENTE EN EDICIÓN COMPLETA (isEditing == true)
            if (isEditing) {
                // 1. Ciclo Académico
                item {
                    OutlinedTextField(
                        value = ciclo,
                        onValueChange = { ciclo = it },
                        label = { Text("Ciclo Académico") },
                        placeholder = { Text("Ej. 2026-I") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            autoCorrect = false
                        )
                    )
                }

                // 2. Notas / Observaciones (antes de Estado)
                item {
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas / Observaciones") },
                        placeholder = { Text("Ej. El salón es el Q3-401") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrect = true
                        )
                    )
                }

                // 3. Estado del curso (antes de Colores)
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Estado del curso:", style = WiText.body, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val activeColor = WiCss.mco
                            val inactiveColor = WiCss.tx3.copy(alpha = 0.2f)
                            
                            Button(
                                onClick = { estado = "activo" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (estado == "activo") activeColor else inactiveColor
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Activo", color = if (estado == "activo") Color.White else WiCss.tx1)
                            }
                            
                            Button(
                                onClick = { estado = "inactivo" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (estado == "inactivo") WiCss.error else inactiveColor
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Inactivo", color = if (estado == "inactivo") Color.White else WiCss.tx1)
                            }
                        }
                    }
                }

                // 4. Color Chooser (colores al último)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Elegir color de asignatura:", style = WiText.label)
                        ColorChooser(selectedColor = color, onSelect = { color = it })
                    }
                }
            }

            // Botón de Guardar
            item {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (nombre.isNotBlank()) {
                            val listHorarios = mutableListOf<HorarioClase>()
                            selectedDays.forEach { (day, checked) ->
                                if (checked) {
                                    listHorarios.add(
                                        HorarioClase(
                                            dia = day,
                                            inicio = dayStartTimes[day] ?: "08:30",
                                            fin = dayEndTimes[day] ?: "10:00",
                                            desde = dayDesdeDates[day] ?: System.currentTimeMillis(),
                                            hasta = dayHastaDates[day] ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)
                                        )
                                    )
                                }
                            }
                            val finalCurso = Curso(
                                id = curso?.id ?: paraId(nombre),
                                usuario = curso?.usuario.orEmpty(),
                                email = curso?.email.orEmpty(),
                                curso = nombre,
                                docente = docente,
                                ciclo = ciclo,
                                estado = estado,
                                color = color,
                                links = tempLinks.toList(),
                                notas = notas,
                                creadoEn = curso?.creadoEn ?: 0L, // 0L → serverTimestamp en Firestore
                                actualizadoEn = System.currentTimeMillis(),
                                horarios = listHorarios,
                                pin = curso?.pin ?: false
                            )
                            // Animación de cierre suave ANTES de guardar (butter-smooth)
                            animateSave(finalCurso)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WiCss.mco),
                    shape = RoundedCornerShape(14.dp),
                    enabled = nombre.isNotBlank()
                ) {
                    Text(
                        text = if (isEditing) "Guardar Cambios" else "Registrar Curso",
                        style = WiText.h3.copy(color = WiCss.txa, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        }
    }
}

// ==========================================
// 3. LIVE SCREEN / TEMPORIZADOR
// ==========================================
@Composable
private fun LivePage(state: EstudiaWiiUiState) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.value
    val todayEpoch = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val activeTheme = LocalWiThemeColors.current

    // Clases de hoy ordenadas por hora
    val classesToday = state.courses.flatMap { course ->
        course.schedules.filter {
            it.dayOfWeek == dayOfWeek && todayEpoch >= it.desde && todayEpoch <= it.hasta
        }.map { Pair(course, it) }
    }.sortedBy { it.second.startTime }

    // Próximas clases de la semana actual (días restantes de hoy en adelante, sin incluir hoy)
    data class WeekClass(val curso: Curso, val schedule: HorarioClase, val date: LocalDate)
    val upcomingWeek = mutableListOf<WeekClass>()
    for (offset in 1..6) {
        val date = today.plusDays(offset.toLong())
        val dow = date.dayOfWeek.value
        val epoch = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        state.courses.forEach { course ->
            course.schedules.filter {
                it.dayOfWeek == dow && epoch >= it.desde && epoch <= it.hasta
            }.forEach { upcomingWeek.add(WeekClass(course, it, date)) }
        }
        // Solo mostramos la semana actual (hasta domingo)
        if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY) break
    }
    val upcomingGrouped = upcomingWeek.sortedWith(compareBy({ it.date }, { it.schedule.startTime }))
        .groupBy { it.date }

    // Locale español para fechas
    val localeES = java.util.Locale("es", "PE")
    val monthNamesES = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val todayName = dayNames.getOrElse(dayOfWeek - 1) { "Hoy" }
    val monthName = monthNamesES.getOrElse(today.monthValue - 1) { "" }
    val todayFormatted = "$todayName, ${today.dayOfMonth} de $monthName del ${today.year}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header centrado ──────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Live", style = WiText.display)
                Text(todayFormatted, style = WiText.small.copy(color = activeTheme.tx3), textAlign = TextAlign.Center)
            }
        }

        // ── Cronómetro de estudio (compacto) ──────────────────────────────
        item { StudyTimerCard() }

        // ── Sección: Clases de HOY ──────────────────────────────────────────
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activeTheme.mco)
                )
                Text("Hoy · $todayName", style = WiText.label.copy(color = activeTheme.mco, fontWeight = FontWeight.Bold))
            }
        }

        if (classesToday.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(activeTheme.inp.copy(alpha = 0.5f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.WbSunny, null, tint = activeTheme.tx3, modifier = Modifier.size(32.dp))
                        Text("¡Día libre hoy!", style = WiText.h3.copy(color = activeTheme.tx1))
                        Text("No tienes clases programadas para hoy.", style = WiText.small.copy(color = activeTheme.tx3), textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
        items(classesToday) { (course, schedule) ->
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                val parsedStart = runCatching { LocalTime.parse(schedule.startTime) }.getOrNull()
                val parsedEnd = runCatching { LocalTime.parse(schedule.endTime) }.getOrNull()
                val now = LocalTime.now()
                val isLive = parsedStart != null && parsedEnd != null && !now.isBefore(parsedStart) && !now.isAfter(parsedEnd)
                val isFinished = parsedEnd != null && now.isAfter(parsedEnd)
                val isUpcoming = parsedStart != null && now.isBefore(parsedStart)

                val status = when {
                    isLive -> "En Vivo"
                    isFinished -> "Finalizado"
                    isUpcoming -> "Próximo"
                    else -> "Hoy"
                }
                val statusColor = when {
                    isLive -> activeTheme.mco
                    isFinished -> activeTheme.tx3
                    else -> WiCss.warning
                }
                val courseColor = runCatching {
                    Color(android.graphics.Color.parseColor(course.color))
                }.getOrDefault(activeTheme.mco)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLive) courseColor.copy(alpha = 0.12f) else activeTheme.inp.copy(alpha = 0.55f)
                    ),
                    border = if (isLive) androidx.compose.foundation.BorderStroke(1.5.dp, courseColor.copy(alpha = 0.6f))
                    else androidx.compose.foundation.BorderStroke(1.dp, activeTheme.brd.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Barra de color lateral
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(courseColor)
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(course.curso, style = WiText.h3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (course.docente.isNotBlank()) {
                                    Text(course.docente, style = WiText.small.copy(color = activeTheme.tx3))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Rounded.AccessTime, null, modifier = Modifier.size(12.dp), tint = activeTheme.tx3)
                                    Text("${schedule.startTime} – ${schedule.endTime}", style = WiText.small.copy(color = activeTheme.tx2))
                                }
                            }
                            // Badge de estado
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (isLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(activeTheme.mco)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(status, style = WiText.tiny.copy(color = statusColor, fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        // ── Links del curso ──────────────────────────────────────────
                        if (course.links.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                course.links.forEach { link ->
                                    val url = link.url.trim()
                                    val hasLink = url.isNotBlank()
                                    val icon = getLinkIcon(link.nombre)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (hasLink) courseColor.copy(alpha = 0.14f) else activeTheme.inp.copy(alpha = 0.3f))
                                            .border(
                                                width = 1.dp,
                                                color = if (hasLink) courseColor.copy(alpha = 0.5f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable(enabled = hasLink) {
                                                runCatching { uriHandler.openUri(url) }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = link.nombre,
                                            tint = if (hasLink) courseColor else activeTheme.tx3,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = link.nombre,
                                            style = WiText.tiny.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasLink) courseColor else activeTheme.tx3
                                            )
                                        )
                                        if (hasLink) {
                                            Icon(
                                                Icons.Rounded.OpenInNew,
                                                null,
                                                tint = courseColor.copy(alpha = 0.6f),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Sección: Esta Semana ──────────────────────────────────────────
        if (upcomingGrouped.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = activeTheme.tx3, modifier = Modifier.size(14.dp))
                    Text("Esta Semana", style = WiText.label.copy(color = activeTheme.tx3, fontWeight = FontWeight.Bold))
                }
            }

            upcomingGrouped.forEach { (date, classes) ->
                item {
                    val dow = date.dayOfWeek.value
                    val mn = monthNamesES.getOrElse(date.monthValue - 1) { "" }
                    val dn = dayNames.getOrElse(dow - 1) { "" }
                    val dayLabel = "$dn, ${date.dayOfMonth} de $mn"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(activeTheme.inp.copy(alpha = 0.40f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(dayLabel, style = WiText.label.copy(color = activeTheme.tx2, fontWeight = FontWeight.Bold))
                        classes.forEach { wc ->
                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            val courseColor = runCatching {
                                Color(android.graphics.Color.parseColor(wc.curso.color))
                            }.getOrDefault(activeTheme.mco)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(courseColor)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(wc.curso.curso, style = WiText.body.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (wc.curso.docente.isNotBlank()) {
                                            Text(wc.curso.docente, style = WiText.tiny.copy(color = activeTheme.tx3))
                                        }
                                    }
                                    Text(
                                        "${wc.schedule.startTime} – ${wc.schedule.endTime}",
                                        style = WiText.small.copy(color = activeTheme.tx2)
                                    )
                                }
                                // Links de la clase de la semana
                                if (wc.curso.links.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        wc.curso.links.forEach { link ->
                                            val url = link.url.trim()
                                            val hasLink = url.isNotBlank()
                                            val icon = getLinkIcon(link.nombre)
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (hasLink) courseColor.copy(alpha = 0.12f) else activeTheme.inp.copy(alpha = 0.3f))
                                                    .border(1.dp, if (hasLink) courseColor.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(10.dp))
                                                    .clickable(enabled = hasLink) { runCatching { uriHandler.openUri(url) } }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(icon, null, tint = if (hasLink) courseColor else activeTheme.tx3, modifier = Modifier.size(12.dp))
                                                Text(link.nombre, style = WiText.tiny.copy(fontWeight = FontWeight.Bold, color = if (hasLink) courseColor else activeTheme.tx3))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (classesToday.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(activeTheme.inp.copy(alpha = 0.3f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin clases programadas para el resto de la semana.", style = WiText.small.copy(color = activeTheme.tx3), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun StudyTimerCard() {
    var running by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(running) {
        if (running) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                elapsedSeconds++
            }
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    GlassCard(Modifier.fillMaxWidth(), intensity = 0.72f) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Temporizador de Estudio", style = WiText.label, color = WiCss.mco)
            Spacer(Modifier.height(10.dp))
            Text(
                text = timeString,
                style = WiText.display.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = WiCss.tx1
            )
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { running = !running },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (running) WiCss.error else WiCss.mco
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        null,
                        tint = WiCss.txa
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (running) "Pausar" else "Iniciar Estudio", color = WiCss.txa)
                }

                OutlinedButton(
                    onClick = {
                        running = false
                        elapsedSeconds = 0
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Reiniciar")
                }
            }
        }
    }
}

// ==========================================
// 4. MESES / CALENDAR SCREEN
// ==========================================
@Composable
private fun MesesPage(state: EstudiaWiiUiState) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var displayedYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val activeTheme = LocalWiThemeColors.current

    // Fechas en español
    val monthNamesES = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val dayNamesES = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    val displayedMonthLabel = "${monthNamesES.getOrElse(displayedYearMonth.monthValue - 1) { "" }} ${displayedYearMonth.year}"

    val daysInMonth = displayedYearMonth.lengthOfMonth()
    val firstOfMonth = displayedYearMonth.atDay(1)
    val startDayOfWeek = firstOfMonth.dayOfWeek.value // 1=Lun, 7=Dom

    // Clases del día seleccionado
    val selectedDayOfWeek = selectedDate.dayOfWeek.value
    val selectedEpoch = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val classesOnSelectedDay = state.courses.flatMap { course ->
        course.schedules.filter {
            it.dayOfWeek == selectedDayOfWeek && selectedEpoch >= it.desde && selectedEpoch <= it.hasta
        }.map { Pair(course, it) }
    }.sortedBy { it.second.startTime }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Calendario", style = WiText.display)
                    Text(
                        displayedMonthLabel,
                        style = WiText.small.copy(color = activeTheme.tx3)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(activeTheme.inp.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, null, tint = activeTheme.tx1, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(activeTheme.inp.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Rounded.ChevronRight, null, tint = activeTheme.tx1, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ── Cuadrícula del calendario ─────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.inp.copy(alpha = 0.45f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.brd.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Cabeceras L M M J V S D
                    val headers = listOf("L", "M", "M", "J", "V", "S", "D")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        headers.forEach { h ->
                            Text(
                                h,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = WiText.label.copy(color = activeTheme.tx3, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    val totalSlots = daysInMonth + (startDayOfWeek - 1)
                    val rowsCount = (totalSlots + 6) / 7

                    for (r in 0 until rowsCount) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (c in 0..6) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - (startDayOfWeek - 2)

                                if (dayNum in 1..daysInMonth) {
                                    val dateObj = displayedYearMonth.atDay(dayNum)
                                    val isSelected = dateObj == selectedDate
                                    val isToday = dateObj == today

                                    val dayOfWeekVal = dateObj.dayOfWeek.value
                                    val dateEpoch = dateObj.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                                    // Cursos que tienen clase ese día (filtrado por vigencia)
                                    val coursesThisDay = state.courses.filter { course ->
                                        course.schedules.any {
                                            it.dayOfWeek == dayOfWeekVal && dateEpoch >= it.desde && dateEpoch <= it.hasta
                                        }
                                    }

                                    val bgColor = when {
                                        isSelected -> activeTheme.mco
                                        isToday -> activeTheme.mco.copy(alpha = 0.18f)
                                        else -> Color.Transparent
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bgColor)
                                            .clickable {
                                                selectedDate = dateObj
                                            }
                                            .padding(vertical = 7.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = WiText.body.copy(
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> activeTheme.mco
                                                    else -> activeTheme.tx1
                                                }
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        // Puntos de colores de cursos
                                        if (coursesThisDay.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                coursesThisDay.take(3).forEach { course ->
                                                    val cColor = runCatching {
                                                        Color(android.graphics.Color.parseColor(course.color))
                                                    }.getOrDefault(activeTheme.mco)
                                                    Box(
                                                        Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White.copy(alpha = 0.85f) else cColor)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Panel de clases del día seleccionado ──────────────────────────
        item {
            val selDow = selectedDate.dayOfWeek.value
            val selMon = monthNamesES.getOrElse(selectedDate.monthValue - 1) { "" }
            val selDay = dayNamesES.getOrElse(selDow - 1) { "" }
            val dateLabel = "$selDay, ${selectedDate.dayOfMonth} de $selMon del ${selectedDate.year}"
            val isToday = selectedDate == today

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isToday) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape).background(activeTheme.mco)
                        )
                    }
                    Text(
                        dateLabel,
                        style = WiText.label.copy(
                            color = if (isToday) activeTheme.mco else activeTheme.tx2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (classesOnSelectedDay.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(activeTheme.inp.copy(alpha = 0.35f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sin clases para este día.",
                            style = WiText.body.copy(color = activeTheme.tx3),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    classesOnSelectedDay.forEach { (course, schedule) ->
                        val courseColor = runCatching {
                            Color(android.graphics.Color.parseColor(course.color))
                        }.getOrDefault(activeTheme.mco)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = courseColor.copy(alpha = 0.08f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, courseColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(courseColor)
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(course.curso, style = WiText.h3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (course.docente.isNotBlank()) {
                                        Text(course.docente, style = WiText.tiny.copy(color = activeTheme.tx3))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(schedule.startTime, style = WiText.body.copy(color = courseColor, fontWeight = FontWeight.Bold))
                                    Text(schedule.endTime, style = WiText.tiny.copy(color = activeTheme.tx3))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// ==========================================
// 6. INFO PANTALLAS (Acerca, Términos, etc.)
// ==========================================
@Composable
private fun InfoPage(page: WiPage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth(), intensity = 0.78f) {
                GoldPill(page.label)
                Text(page.label, style = WiText.display, modifier = Modifier.padding(top = 10.dp))
                val description = when (page) {
                    WiPage.Acerca -> "EstudiaWii es el asistente de estudios definitivo para organizarse de forma premium, fluida y eficiente."
                    WiPage.Privacidad -> "Tus datos están protegidos. Usamos encriptación segura y cumplimos con los estándares de privacidad más estrictos."
                    WiPage.Terminos -> "Condiciones legales simples y transparentes para garantizar el uso correcto y ético de la plataforma."
                    WiPage.Contacto -> "Canales oficiales para contactar al equipo de soporte de EstudiaWii y resolver tus dudas técnicas."
                    WiPage.Feedback -> "Tu opinión impulsa EstudiaWii. Compártenos tus ideas de mejora o reporta fallos técnicos."
                    else -> "Página informativa oficial de EstudiaWii."
                }
                Text(description, style = WiText.body, modifier = Modifier.padding(top = 8.dp))
            }
        }
        
        when (page) {
            WiPage.Acerca -> {
                item {
                    GlassCard(Modifier.fillMaxWidth(), intensity = 0.58f) {
                        Text("El propósito de EstudiaWii", style = WiText.h2, color = WiCss.mco)
                        Text(
                            "Esta aplicación fue diseñada para simplificar tu vida estudiantil al máximo. Ofrece control instantáneo de tus cursos, clases del día en vivo, temporizadores integrados y notificaciones push inteligentes.",
                            style = WiText.body,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ThemeSwatch(theme: WiThemeColors, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(theme.mco)
            .border(if (selected) 3.dp else 1.dp, if (selected) WiCss.mco else theme.brd.copy(alpha = 0.75f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(WiCss.wb.copy(alpha = 0.88f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = theme.mco, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(WiCss.mco.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, style = WiText.h3, maxLines = 1)
            Text(value, style = WiText.tiny, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickStat(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(WiCss.glassShape(0.35f))
            .background(WiCss.softSurface(if (WiCss.isDark) 0.72f else 0.34f))
            .border(WiCss.glassBorder(0.35f), WiCss.glassShape(0.35f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(18.dp))
        Text(value, style = WiText.h3.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp), maxLines = 1)
        Text(label, style = WiText.tiny, maxLines = 1)
    }
}

@Composable
private fun ActionTile(title: String, body: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier, intensity = 0.58f, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(WiCss.mco.copy(alpha = if (WiCss.isDark) 0.14f else 0.20f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = WiCss.mco, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, style = WiText.h3, maxLines = 1)
                Text(body, style = WiText.tiny, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Rounded.NorthEast, null, tint = WiCss.mco, modifier = Modifier.size(18.dp))
        }
    }
}
