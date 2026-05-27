package com.estudiawii.app.core.course

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object CourseStore {
    private const val PREFS = "EstudiaWii_courses"
    private const val KEY_COURSES = "courses_json"

    fun saveCourses(context: Context, cursos: List<Curso>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (curso in cursos) {
            val jsonCurso = JSONObject().apply {
                put("id", curso.id)
                put("usuario", curso.usuario)
                put("email", curso.email)
                put("curso", curso.curso) // Guarda curso en lugar de nombre
                put("docente", curso.docente)
                put("ciclo", curso.ciclo)
                put("estado", curso.estado)
                put("color", curso.color)
                put("notas", curso.notas)
                put("creadoEn", curso.creadoEn)
                put("actualizadoEn", curso.actualizadoEn)
                put("pin", curso.pin) // Agrega pin
                
                // Serializar links (Lista dinámica de EnlaceCurso)
                val linksArray = JSONArray()
                for (link in curso.links) {
                    val jsonLink = JSONObject().apply {
                        put("nombre", link.nombre)
                        put("url", link.url)
                    }
                    linksArray.put(jsonLink)
                }
                put("links", linksArray)
                
                // Serializar horarios
                val horariosArray = JSONArray()
                for (h in curso.horarios) {
                    val jsonH = JSONObject().apply {
                        put("dia", h.dia)
                        put("inicio", h.inicio)
                        put("fin", h.fin)
                        put("desde", h.desde)
                        put("hasta", h.hasta)
                    }
                    horariosArray.put(jsonH)
                }
                put("horarios", horariosArray)
            }
            jsonArray.put(jsonCurso)
        }
        prefs.edit().putString(KEY_COURSES, jsonArray.toString()).apply()
    }

    fun loadCourses(context: Context): List<Curso> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_COURSES, null)
        if (jsonStr.isNullOrBlank()) {
            val samples = getSampleCourses()
            saveCourses(context, samples)
            return samples
        }
        val cursos = mutableListOf<Curso>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val jsonCurso = jsonArray.getJSONObject(i)
                val id = jsonCurso.getString("id")
                val usuario = jsonCurso.optString("usuario", "")
                val email = jsonCurso.optString("email", "")
                val cursoName = jsonCurso.optString("curso", jsonCurso.optString("nombre", jsonCurso.optString("name", "")))
                val docente = jsonCurso.optString("docente", jsonCurso.optString("teacher", ""))
                val ciclo = jsonCurso.optString("ciclo", "2026-I")
                val estado = jsonCurso.optString("estado", "activo")
                val color = jsonCurso.optString("color", "#2563EB")
                val notas = jsonCurso.optString("notas", "")
                val creadoEn = jsonCurso.optLong("creadoEn", System.currentTimeMillis())
                val actualizadoEn = jsonCurso.optLong("actualizadoEn", System.currentTimeMillis())
                val pin = jsonCurso.optBoolean("pin", false)
                
                // Deserializar links (como lista de EnlaceCurso)
                val linksList = mutableListOf<EnlaceCurso>()
                if (jsonCurso.has("links")) {
                    val rawLinks = jsonCurso.get("links")
                    if (rawLinks is JSONArray) {
                        for (j in 0 until rawLinks.length()) {
                            val jsonLink = rawLinks.getJSONObject(j)
                            linksList.add(
                                EnlaceCurso(
                                    nombre = jsonLink.getString("nombre"),
                                    url = jsonLink.getString("url")
                                )
                            )
                        }
                    } else if (rawLinks is JSONObject) {
                        // Retrocompatibilidad con el mapa de enlaces antiguo
                        val keys = listOf("pit", "zoom", "meet", "youtube", "whatsapp")
                        keys.forEach { key ->
                            val url = rawLinks.optString(key, "")
                            if (url.isNotBlank()) {
                                val cleanName = when (key) {
                                    "pit" -> "PIT Virtual"
                                    "zoom" -> "Zoom"
                                    "meet" -> "Google Meet"
                                    "youtube" -> "YouTube"
                                    "whatsapp" -> "WhatsApp"
                                    else -> key.replaceFirstChar { it.uppercase() }
                                }
                                linksList.add(EnlaceCurso(cleanName, url))
                            }
                        }
                    }
                }
                
                // Deserializar horarios
                val horariosList = mutableListOf<HorarioClase>()
                if (jsonCurso.has("horarios")) {
                    val horariosArray = jsonCurso.getJSONArray("horarios")
                    for (j in 0 until horariosArray.length()) {
                        val jsonH = horariosArray.getJSONObject(j)
                        horariosList.add(
                            HorarioClase(
                                dia = jsonH.getInt("dia"),
                                inicio = jsonH.getString("inicio"),
                                fin = jsonH.getString("fin"),
                                desde = jsonH.optLong("desde", System.currentTimeMillis()),
                                hasta = jsonH.optLong("hasta", System.currentTimeMillis() + 120 * 24 * 60 * 60 * 1000L)
                            )
                        )
                    }
                } else if (jsonCurso.has("schedules")) {
                    val schedulesArray = jsonCurso.getJSONArray("schedules")
                    for (j in 0 until schedulesArray.length()) {
                        val jsonSchedule = schedulesArray.getJSONObject(j)
                        horariosList.add(
                            HorarioClase(
                                dia = jsonSchedule.getInt("dayOfWeek"),
                                inicio = jsonSchedule.getString("startTime"),
                                fin = jsonSchedule.getString("endTime")
                            )
                        )
                    }
                }
                
                cursos.add(
                    Curso(
                        id = id,
                        usuario = usuario,
                        email = email,
                        curso = cursoName,
                        docente = docente,
                        ciclo = ciclo,
                        estado = estado,
                        color = color,
                        links = linksList,
                        notas = notas,
                        creadoEn = creadoEn,
                        actualizadoEn = actualizadoEn,
                        horarios = horariosList,
                        pin = pin
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getSampleCourses()
        }
        return cursos
    }

    fun getSampleCourses(): List<Curso> {
        return listOf(
            Curso(
                id = "1",
                usuario = "wilder",
                email = "wilder@email.com",
                curso = "Matemáticas Aplicadas",
                docente = "Prof. Alejandro Silva",
                ciclo = "2026-I",
                estado = "activo",
                color = "#FFDA34", // Oro
                links = listOf(
                    EnlaceCurso("PIT Virtual", "https://pit-virtual.uni.edu.pe/my/"),
                    EnlaceCurso("Zoom", "https://zoom.us/j/123456")
                ),
                horarios = listOf(
                    HorarioClase(1, "08:30", "10:00"), // Lunes
                    HorarioClase(3, "08:30", "10:00")  // Miércoles
                )
            ),
            Curso(
                id = "2",
                usuario = "wilder",
                email = "wilder@email.com",
                curso = "Desarrollo Android Premium",
                docente = "Ing. Wilder Taype",
                ciclo = "2026-I",
                estado = "activo",
                color = "#25B62A", // Paz
                links = listOf(
                    EnlaceCurso("PIT Virtual", "https://pit-virtual.uni.edu.pe/my/"),
                    EnlaceCurso("Google Meet", "https://meet.google.com/abc-defg-hij"),
                    EnlaceCurso("WhatsApp", "https://chat.whatsapp.com/invite/XYZ")
                ),
                horarios = listOf(
                    HorarioClase(2, "10:30", "12:00"), // Martes
                    HorarioClase(4, "10:30", "12:00")  // Jueves
                )
            ),
            Curso(
                id = "3",
                usuario = "wilder",
                email = "wilder@email.com",
                curso = "Arquitectura de Software",
                docente = "Dra. Laura Ortega",
                ciclo = "2026-I",
                estado = "activo",
                color = "#1978D7", // Cielo
                links = listOf(
                    EnlaceCurso("PIT Virtual", "https://pit-virtual.uni.edu.pe/my/"),
                    EnlaceCurso("YouTube", "https://youtube.com/playlist?list=solid")
                ),
                horarios = listOf(
                    HorarioClase(5, "09:00", "11:30")  // Viernes
                )
            )
        )
    }
}
