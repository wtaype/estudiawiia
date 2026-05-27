package com.estudiawii.app.core.course

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class EnlaceCurso(
    val nombre: String, // "Zoom", "WhatsApp", "YouTube", "PIT", "Asistencia", etc.
    val url: String
)

@Immutable
data class HorarioClase(
    val dia: Int, // 1 = Lunes, 2 = Martes, 3 = Miércoles, 4 = Jueves, 5 = Viernes, 6 = Sábado, 7 = Domingo
    val inicio: String, // Formato "HH:MM" (ej. "08:30")
    val fin: String, // Formato "HH:MM" (ej. "10:00")
    val desde: Long = System.currentTimeMillis(), // Fecha inicio clases
    val hasta: Long = System.currentTimeMillis() + 120 * 24 * 60 * 60 * 1000L // 4 meses después por defecto
) {
    val nombreDia: String get() = when (dia) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> "Día Desconocido"
    }
}

@Immutable
data class Curso(
    val id: String = java.util.UUID.randomUUID().toString(),
    val usuario: String = "",
    val email: String = "",
    val curso: String = "", // Reemplaza nombre por curso en Firestore/local
    val docente: String = "",
    val ciclo: String = "2026-I",
    val estado: String = "activo",
    val color: String = "#2563EB",
    val links: List<EnlaceCurso> = emptyList(), // Lista dinámica de enlaces personalizados
    val notas: String = "", // Notas y observaciones del curso
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis(),
    val horarios: List<HorarioClase> = emptyList(),
    val pin: Boolean = false // Agrega campo pin
) {
    // Getters de compatibilidad
    val name: String get() = curso
    val nombre: String get() = curso
    val teacher: String get() = docente
    val description: String get() = ""
    val schedules: List<ClassSchedule> get() = horarios.map { ClassSchedule(it.dia, it.inicio, it.fin, it.desde, it.hasta) }
}

// ==========================================
// Clases de compatibilidad heredadas
// ==========================================
typealias ClassSchedule = HorarioClase
typealias Course = Curso

val ClassSchedule.dayOfWeek: Int get() = dia
val ClassSchedule.startTime: String get() = inicio
val ClassSchedule.endTime: String get() = fin
val ClassSchedule.dayName: String get() = nombreDia
