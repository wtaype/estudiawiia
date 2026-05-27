package com.estudiawii.app.feature.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.ui.graphics.vector.ImageVector

enum class WiPage(val label: String, val subtitle: String, val icon: ImageVector, val isMain: Boolean = true) {
    Inicio("Inicio", "Mi panel", Icons.Rounded.Home),
    Cursos("Cursos", "Mis asignaturas", Icons.Rounded.Widgets),
    Live("En vivo", "Clases de hoy", Icons.Rounded.PlayCircle),
    Meses("Calendario", "Planificación mensual", Icons.Rounded.DateRange),
    Ajustes("Ajustes", "Temas y preferencias", Icons.Rounded.Settings),
    Acerca("Acerca", "Información de la app", Icons.Rounded.Info, isMain = false),
    Terminos("Términos", "Condiciones de uso", Icons.Rounded.Description, isMain = false),
    Privacidad("Privacidad", "Política base", Icons.Rounded.Security, isMain = false),
    Contacto("Contacto", "Canales de soporte", Icons.Rounded.Mail, isMain = false),
    Racha("Racha", "Progreso diario", Icons.Rounded.LocalFireDepartment, isMain = false),
    Feedback("Feedback", "Ideas y mejoras", Icons.Rounded.Feedback, isMain = false);

    companion object {
        val mainPages = listOf(Inicio, Cursos, Live, Meses, Ajustes)
        val aboutPages = listOf(Acerca, Terminos, Privacidad, Contacto, Racha, Feedback)
    }
}
