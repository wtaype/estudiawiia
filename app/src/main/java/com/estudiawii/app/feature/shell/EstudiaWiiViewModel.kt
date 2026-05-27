package com.estudiawii.app.feature.shell

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.estudiawii.app.core.auth.AuthRepository
import com.estudiawii.app.core.model.Smile
import kotlinx.coroutines.tasks.await
import com.estudiawii.app.core.course.*
import com.estudiawii.app.ui.theme.OroThemeColors
import com.estudiawii.app.ui.theme.WiThemeColors
import com.estudiawii.app.ui.theme.WiThemes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EstudiaWiiViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val prefs = application.getSharedPreferences("EstudiaWii_prefs", Context.MODE_PRIVATE)
    private val initialCachedProfile = loadCachedProfile(prefs)

    // Keep the constructor cheap: the first Compose frame should not parse course JSON.
    private val _uiState = MutableStateFlow(
        EstudiaWiiUiState(
            booting = false,
            hasAuthSession = initialCachedProfile != null,
            profile = initialCachedProfile,
            courses = emptyList(),
            coursesLoading = initialCachedProfile != null
        )
    )
    val uiState: StateFlow<EstudiaWiiUiState> = _uiState.asStateFlow()

    private val _activeTheme = MutableStateFlow(
        prefs.getString("active_theme", "Oro")
            .let { savedName -> WiThemes.find { it.name == savedName } ?: OroThemeColors }
    )
    val activeTheme: StateFlow<WiThemeColors> = _activeTheme.asStateFlow()

    init {
        viewModelScope.launch {
            loadLocalCoursesAsync()
            loadSessionAsync()
        }
    }

    private suspend fun loadLocalCoursesAsync() {
        val localCourses = withContext(Dispatchers.IO) {
            CourseStore.loadCourses(getApplication())
        }
        _uiState.value = _uiState.value.copy(
            courses = localCourses,
            coursesLoading = false
        )
    }

    /**
     * Carga la sesión de forma completamente asíncrona en Dispatchers.IO.
     * Primero lee el caché local (SharedPreferences) sin tocar Firebase.
     * Si hay caché, muestra el perfil en milisegundos y luego refresca en segundo plano.
     */
    private suspend fun loadSessionAsync() {
        // Inicializar Firebase manualmente en Dispatchers.IO para evitar bloquear el hilo principal
        withContext(Dispatchers.IO) {
            runCatching {
                com.google.firebase.FirebaseApp.initializeApp(getApplication())
            }
        }

        // 1. Comprobar si hay sesión de Firebase (solo accede a la caché local del SDK, muy rápido)
        val hasSession = withContext(Dispatchers.IO) {
            runCatching { authRepository.isLoggedIn }.getOrDefault(false)
        }

        if (!hasSession) {
            // Sin sesión: mostrar login directamente
            clearCachedProfile(prefs)
            _uiState.value = _uiState.value.copy(
                booting = false,
                profile = null,
                hasAuthSession = false,
                googlePending = false,
                googleEmail = "",
            )
            return
        }

        // 2. Intentar leer caché local primero (0ms de red, instantáneo)
        val cachedProfile = withContext(Dispatchers.IO) {
            loadCachedProfile(prefs)
        }

        if (cachedProfile != null) {
            // Perfil en caché: mostrar home INMEDIATAMENTE sin esperar a Firestore
            _uiState.value = _uiState.value.copy(
                booting = false,
                profile = cachedProfile,
                hasAuthSession = true,
                googlePending = false,
                googleEmail = "",
            )
            // Refrescar perfil desde Firestore en segundo plano (silenciosamente)
            refreshProfileFromFirestoreAsync()
            syncCursosFromFirestoreAsync(cachedProfile.usuario)
        } else {
            // 3. Sin caché: obtener perfil desde Firestore (primera vez o perfil limpiado)
            val freshProfile = withContext(Dispatchers.IO) {
                runCatching { authRepository.getSessionProfile() }.getOrNull()
            }
            when {
                freshProfile != null -> {
                    saveCachedProfile(prefs, freshProfile)
                    _uiState.value = _uiState.value.copy(
                        booting = false,
                        profile = freshProfile,
                        hasAuthSession = true,
                        googlePending = false,
                        googleEmail = "",
                    )
                    syncCursosFromFirestoreAsync(freshProfile.usuario)
                }
                else -> {
                    // Sesión Firebase existe pero sin perfil Firestore (registro Google incompleto)
                    val email = withContext(Dispatchers.IO) {
                        runCatching { authRepository.currentEmail }.getOrDefault("")
                    }
                    _uiState.value = _uiState.value.copy(
                        booting = false,
                        profile = null,
                        hasAuthSession = false,
                        googlePending = true,
                        googleEmail = email.orEmpty(),
                    )
                }
            }
        }
    }

    /**
     * Refresca el perfil desde Firestore silenciosamente mientras el usuario ya está en home.
     * No bloquea la UI ni el arranque.
     */
    private fun refreshProfileFromFirestoreAsync() {
        viewModelScope.launch {
            val fresh = withContext(Dispatchers.IO) {
                runCatching { authRepository.getSessionProfile() }.getOrNull()
            }
            if (fresh != null) {
                saveCachedProfile(prefs, fresh)
                _uiState.value = _uiState.value.copy(profile = fresh)
            }
        }
    }

    fun changeTheme(theme: WiThemeColors) {
        _activeTheme.value = theme
        prefs.edit().putString("active_theme", theme.name).apply()
    }

    fun setProfile(profile: Smile?) {
        if (profile != null) {
            saveCachedProfile(prefs, profile)
            syncCursosFromFirestoreAsync(profile.usuario)
        } else {
            clearCachedProfile(prefs)
        }
        _uiState.value = _uiState.value.copy(
            booting = false,
            profile = profile,
            hasAuthSession = profile != null,
            googlePending = false,
            googleEmail = "",
        )
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun logout(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { authRepository.logout(context) }
        }
        clearCachedProfile(prefs)
        // Limpiar caché local de cursos al cerrar sesión
        CourseStore.saveCourses(getApplication(), emptyList())
        _uiState.value = _uiState.value.copy(
            booting = false,
            profile = null,
            hasAuthSession = false,
            googlePending = false,
            googleEmail = "",
            courses = emptyList()
        )
    }

    fun updatePhoto(url: String) {
        val profile = _uiState.value.profile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true)
            runCatching {
                withContext(Dispatchers.IO) {
                    authRepository.updateProfilePhoto(profile.usuario, url.ifBlank { null })
                    authRepository.getSessionProfile()
                }
            }.onSuccess { fresh ->
                if (fresh != null) {
                    setProfile(fresh)
                    _uiState.value = _uiState.value.copy(authLoading = false, message = "Foto actualizada")
                } else {
                    _uiState.value = _uiState.value.copy(authLoading = false, error = "No se pudo recuperar el perfil")
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(authLoading = false, error = it.message ?: "Error al actualizar foto")
            }
        }
    }

    fun recoverPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true)
            runCatching {
                withContext(Dispatchers.IO) { authRepository.recover(email) }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(authLoading = false, message = "Enlace enviado al correo")
            }.onFailure {
                _uiState.value = _uiState.value.copy(authLoading = false, error = it.message ?: "Error al enviar correo")
            }
        }
    }

    private fun saveCachedProfile(prefs: SharedPreferences, profile: Smile) {
        prefs.edit().apply {
            putString("profile_uid", profile.uid)
            putString("profile_usuario", profile.usuario)
            putString("profile_nombre", profile.nombre)
            putString("profile_apellidos", profile.apellidos)
            putString("profile_email", profile.email)
            putString("profile_avatar", profile.avatar)
            putString("profile_plan", profile.plan)
            putString("profile_rol", profile.rol)
            putString("profile_estado", profile.estado)
            putBoolean("profile_activo", profile.activo)
            putString("profile_registradoCon", profile.registradoCon)
            putBoolean("profile_terminos", profile.terminos)
            putString("profile_tema", profile.tema)
            putBoolean("profile_verificado", profile.verificado)
            putString("profile_segmento", profile.segmento)
            putBoolean("profile_has_cache", true)
            apply()
        }
    }

    private fun loadCachedProfile(prefs: SharedPreferences): Smile? {
        if (!prefs.getBoolean("profile_has_cache", false)) return null
        return Smile(
            uid = prefs.getString("profile_uid", "").orEmpty(),
            usuario = prefs.getString("profile_usuario", "").orEmpty(),
            nombre = prefs.getString("profile_nombre", "").orEmpty(),
            apellidos = prefs.getString("profile_apellidos", "").orEmpty(),
            email = prefs.getString("profile_email", "").orEmpty(),
            avatar = prefs.getString("profile_avatar", null),
            plan = prefs.getString("profile_plan", "free").orEmpty(),
            rol = prefs.getString("profile_rol", "usuario").orEmpty(),
            estado = prefs.getString("profile_estado", "activo").orEmpty(),
            activo = prefs.getBoolean("profile_activo", true),
            registradoCon = prefs.getString("profile_registradoCon", "correo").orEmpty(),
            terminos = prefs.getBoolean("profile_terminos", true),
            tema = prefs.getString("profile_tema", "Paz").orEmpty(),
            verificado = prefs.getBoolean("profile_verificado", false),
            segmento = prefs.getString("profile_segmento", "publico").orEmpty(),
        )
    }

    private fun clearCachedProfile(prefs: SharedPreferences) {
        prefs.edit().apply {
            remove("profile_uid")
            remove("profile_usuario")
            remove("profile_nombre")
            remove("profile_apellidos")
            remove("profile_email")
            remove("profile_avatar")
            remove("profile_plan")
            remove("profile_rol")
            remove("profile_estado")
            remove("profile_activo")
            remove("profile_registradoCon")
            remove("profile_terminos")
            remove("profile_tema")
            remove("profile_verificado")
            remove("profile_segmento")
            remove("profile_has_cache")
            apply()
        }
    }

    fun addCourse(curso: Curso) {
        val activeProfile = _uiState.value.profile
        val updatedCurso = if (activeProfile != null) {
            curso.copy(usuario = activeProfile.usuario, email = activeProfile.email)
        } else {
            curso
        }

        val currentCourses = _uiState.value.courses.filter { it.id != updatedCurso.id } + updatedCurso
        CourseStore.saveCourses(getApplication(), currentCourses)
        _uiState.value = _uiState.value.copy(courses = currentCourses)

        // Sincronizar asíncronamente con Firestore
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestore.collection("cursos")
                    .document(updatedCurso.id)
                    .set(updatedCurso.toFirestoreMap())
                    .await()
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    fun deleteCourse(courseId: String) {
        val currentCourses = _uiState.value.courses.filter { it.id != courseId }
        CourseStore.saveCourses(getApplication(), currentCourses)
        _uiState.value = _uiState.value.copy(courses = currentCourses)

        // Eliminar asíncronamente de Firestore
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestore.collection("cursos")
                    .document(courseId)
                    .delete()
                    .await()
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    /**
     * Descarga asíncronamente los cursos de Firestore en segundo plano,
     * activando el Skeleton Shimmer UI si la lista local está vacía.
     */
    private fun syncCursosFromFirestoreAsync(usuario: String) {
        viewModelScope.launch {
            val localEmpty = _uiState.value.courses.isEmpty()
            if (localEmpty) {
                _uiState.value = _uiState.value.copy(coursesLoading = true)
            }
            
            withContext(Dispatchers.IO) {
                runCatching {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val snapshot = firestore.collection("cursos")
                        .whereEqualTo("usuario", usuario)
                        .get()
                        .await()
                    
                    val freshCursos = snapshot.documents.map { it.toCurso() }
                    
                    // Actualizar estado e interfaz en el hilo principal
                    withContext(Dispatchers.Main) {
                        CourseStore.saveCourses(getApplication(), freshCursos)
                        _uiState.value = _uiState.value.copy(
                            courses = freshCursos,
                            coursesLoading = false
                        )
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(coursesLoading = false)
                    }
                }
            }
        }
    }
}

// ==========================================
// Mapeadores y utilidades de Firestore
// ==========================================
private fun Curso.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "usuario" to usuario,
        "email" to email,
        "curso" to curso, // Mapea curso en lugar de nombre
        "docente" to docente,
        "ciclo" to ciclo,
        "estado" to estado,
        "color" to color,
        "links" to links.map { mapOf("nombre" to it.nombre, "url" to it.url) },
        "notas" to notas,
        "creado" to if (creadoEn > 0) com.google.firebase.Timestamp(java.util.Date(creadoEn)) else com.google.firebase.firestore.FieldValue.serverTimestamp(), // server timestamp creado
        "actualizado" to com.google.firebase.firestore.FieldValue.serverTimestamp(), // siempre server timestamp
        "pin" to pin, // pin booleano
        "horarios" to horarios.map { 
            mapOf(
                "dia" to it.dia,
                "inicio" to it.inicio,
                "fin" to it.fin,
                "desde" to com.google.firebase.Timestamp(java.util.Date(it.desde)), // Guarda Timestamp nativo
                "hasta" to com.google.firebase.Timestamp(java.util.Date(it.hasta)) // Guarda Timestamp nativo
            )
        }
    )
}

@Suppress("UNCHECKED_CAST")
private fun com.google.firebase.firestore.DocumentSnapshot.toCurso(): Curso {
    val rawLinks = get("links") as? List<*>
    val linksList = mutableListOf<EnlaceCurso>()
    rawLinks?.forEach { item ->
        (item as? Map<*, *>)?.let { lMap ->
            val nombre = lMap["nombre"]?.toString().orEmpty()
            val url = lMap["url"]?.toString().orEmpty()
            if (url.isNotBlank()) {
                linksList.add(EnlaceCurso(nombre, url))
            }
        }
    }
    
    // Retrocompatibilidad con maper de links antiguo (Map)
    if (linksList.isEmpty() && get("links") is Map<*, *>) {
        val map = get("links") as Map<*, *>
        val keys = listOf("pit", "zoom", "meet", "youtube", "whatsapp")
        keys.forEach { key ->
            val url = map[key]?.toString().orEmpty()
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

    val rawHorarios = get("horarios") as? List<*>
    val horariosList = mutableListOf<HorarioClase>()
    rawHorarios?.forEach { item ->
        (item as? Map<*, *>)?.let { hMap ->
            val dia = (hMap["dia"] as? Number)?.toInt() ?: 1
            val inicio = hMap["inicio"]?.toString().orEmpty()
            val fin = hMap["fin"]?.toString().orEmpty()
            val desde = when (val d = hMap["desde"]) {
                is com.google.firebase.Timestamp -> d.toDate().time
                is Number -> d.toLong()
                else -> System.currentTimeMillis()
            }
            val hasta = when (val h = hMap["hasta"]) {
                is com.google.firebase.Timestamp -> h.toDate().time
                is Number -> h.toLong()
                else -> System.currentTimeMillis() + 120 * 24 * 60 * 60 * 1000L
            }
            horariosList.add(HorarioClase(dia, inicio, fin, desde, hasta))
        }
    }

    val creado = getTimestamp("creado")?.toDate()?.time 
        ?: (get("creado") as? Number)?.toLong() 
        ?: getLong("creadoEn") 
        ?: System.currentTimeMillis()

    val actualizado = getTimestamp("actualizado")?.toDate()?.time 
        ?: (get("actualizado") as? Number)?.toLong() 
        ?: getLong("actualizadoEn") 
        ?: System.currentTimeMillis()

    val isPinned = getBoolean("pin") ?: false

    return Curso(
        id = getString("id") ?: id,
        usuario = getString("usuario").orEmpty(),
        email = getString("email").orEmpty(),
        curso = getString("curso") ?: getString("nombre") ?: getString("name").orEmpty(),
        docente = getString("docente") ?: getString("teacher").orEmpty(),
        ciclo = getString("ciclo") ?: "2026-I",
        estado = getString("estado") ?: "activo",
        color = getString("color") ?: "#2563EB",
        links = linksList,
        notas = getString("notas").orEmpty(),
        creadoEn = creado,
        actualizadoEn = actualizado,
        horarios = horariosList,
        pin = isPinned
    )
}
