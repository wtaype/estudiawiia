package com.estudiawii.app.feature.shell

import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.core.course.Curso

data class EstudiaWiiUiState(
    val booting: Boolean = true,
    val hasAuthSession: Boolean = false,
    val profile: Smile? = null,
    val authLoading: Boolean = false,
    val googlePending: Boolean = false,
    val googleEmail: String = "",
    val message: String? = null,
    val error: String? = null,
    val courses: List<Curso> = emptyList(),
    val coursesLoading: Boolean = false
) {
    val isLoggedIn: Boolean get() = profile != null || (hasAuthSession && !googlePending)
}

