package com.estudiawii.app.feature.shell

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.estudiawii.app.core.auth.AuthRepository
import com.estudiawii.app.core.model.Smile
import com.estudiawii.app.core.streak.StreakState
import com.estudiawii.app.core.streak.StreakStore
import com.estudiawii.app.feature.auth.AuthScreen
import com.estudiawii.app.ui.theme.EstudiaWiiTheme
import com.estudiawii.app.ui.theme.WiCss
import com.estudiawii.app.ui.theme.WiThemeColors
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EstudiaWiiApp(viewModel: EstudiaWiiViewModel) {
    val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EstudiaWiiTheme(activeTheme) {
        EstudiaWiiShell(
            activeTheme = activeTheme,
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
private fun EstudiaWiiShell(
    activeTheme: WiThemeColors,
    uiState: EstudiaWiiUiState,
    viewModel: EstudiaWiiViewModel
) {
    val context = LocalContext.current
    val auth = remember { AuthRepository() }
    val snackbar = remember { SnackbarHostState() }
    var currentPage by remember { mutableStateOf(WiPage.Inicio) }
    var lastMainPage by remember { mutableStateOf(WiPage.Inicio) }
    var menuOpen by remember { mutableStateOf(false) }
    var backStack by remember { mutableStateOf<List<WiPage>>(emptyList()) }
    var horizontalDrag by remember { mutableStateOf(0f) }
    var streak by remember { mutableStateOf(StreakState()) }

    if (uiState.profile == null) {
        AuthScreen(
            auth = auth,
            activeTheme = activeTheme,
            onAuthenticated = { viewModel.setProfile(it) },
        )
        return
    }

    val activeProfile = uiState.profile

    LaunchedEffect(activeProfile.uid) {
        streak = StreakStore.touch(context)
    }

    fun openPage(page: WiPage, addToHistory: Boolean = true) {
        if (page == currentPage) return
        if (addToHistory && !page.isMain) backStack = (backStack + currentPage).takeLast(8)
        if (page.isMain) {
            backStack = emptyList()
            lastMainPage = page
        }
        currentPage = page
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            val previous = backStack.last()
            backStack = backStack.dropLast(1)
            openPage(previous, addToHistory = false)
        } else if (!currentPage.isMain) {
            openPage(lastMainPage, addToHistory = false)
        }
    }

    fun goMainStep(direction: Int) {
        val pages = WiPage.mainPages
        val index = pages.indexOf(currentPage).takeIf { it >= 0 } ?: pages.indexOf(lastMainPage).coerceAtLeast(0)
        val nextIndex = index + direction
        when {
            direction > 0 && index == pages.lastIndex -> menuOpen = true
            nextIndex in pages.indices -> openPage(pages[nextIndex], addToHistory = false)
        }
    }

    val canGoBack = !currentPage.isMain || backStack.isNotEmpty()
    BackHandler(enabled = canGoBack) { goBack() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
        topBar = {
            Header(
                profile = activeProfile,
                currentPage = currentPage,
                onHome = { openPage(WiPage.Inicio, addToHistory = false) },
                onStreak = { openPage(WiPage.Racha) },
                onNotifications = { openPage(WiPage.Meses) },
                onMenu = { menuOpen = true },
                streakCount = streak.current,
                streakActive = streak.activeToday,
                showBack = false,
                onBack = ::goBack,
            )
        },
        bottomBar = {
            Column {
                NavMain(lastMainPage) { page -> openPage(page, addToHistory = false) }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WiCss.bg)
                .pointerInput(currentPage, menuOpen) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, amount -> horizontalDrag += amount },
                        onDragEnd = {
                            when {
                                horizontalDrag > 90f && !currentPage.isMain -> goBack()
                                horizontalDrag < -90f && currentPage.isMain -> goMainStep(1)
                                horizontalDrag > 90f && currentPage.isMain -> goMainStep(-1)
                            }
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f },
                    )
                },
        ) {
            TemplatePage(
                page = currentPage,
                state = uiState,
                streak = streak,
                viewModel = viewModel,
                onNavigate = { openPage(it) },
                onLogout = { viewModel.logout(context) }
            )
        }
    }

    MenuRight(
        open = menuOpen,
        profile = activeProfile,
        onClose = { menuOpen = false },
        onNavigate = {
            menuOpen = false
            openPage(it)
        },
        onLogout = {
            viewModel.logout(context)
            menuOpen = false
        },
    )
}
