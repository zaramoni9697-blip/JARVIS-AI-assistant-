package com.example.ui.navigation

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserProfile
import com.example.data.repository.AdminRepository
import com.example.data.repository.ConversationRepository
import com.example.data.repository.MemoryRepository
import com.example.ui.admin.AdminViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.coding.CodingViewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.jarvis.JarvisViewModel
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodingAssistantScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceAssistantScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.google.firebase.auth.FirebaseUser

enum class AppScreen {
    DASHBOARD,
    VOICE,
    CHAT,
    CODING,
    MEMORY,
    DEVICE,
    ADMIN,
    SETTINGS
}

@Composable
fun MainAppContainer(
    user: FirebaseUser,
    userProfile: UserProfile?,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // Repositories initialized only after user is authenticated
    val memoryRepository = remember(user.uid) { MemoryRepository(context) }
    val conversationRepository = remember(user.uid) { ConversationRepository(context) }
    val adminRepository = remember(user.uid) { AdminRepository(context) }

    val jarvisViewModel = remember(user.uid) {
        JarvisViewModel(
            application = application,
            userId = user.uid,
            memoryRepository = memoryRepository,
            conversationRepository = conversationRepository
        ).apply {
            userLanguagePreference = userProfile?.languagePreference ?: "auto"
        }
    }

    val codingViewModel: CodingViewModel = viewModel()
    val adminViewModel = remember(user.uid) {
        AdminViewModel(adminRepository)
    }

    val pendingConfirmation by jarvisViewModel.pendingConfirmation.collectAsState()

    val isAdmin = userProfile?.role == "admin" ||
            user.email?.equals("mfoysalalom7@gmail.com", ignoreCase = true) == true

    // Back handling for sub-screens
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    jarvisViewModel = jarvisViewModel,
                    userProfile = userProfile,
                    isAdmin = isAdmin,
                    onNavigateToVoice = { currentScreen = AppScreen.VOICE },
                    onNavigateToChat = { currentScreen = AppScreen.CHAT },
                    onNavigateToCoding = { currentScreen = AppScreen.CODING },
                    onNavigateToMemory = { currentScreen = AppScreen.MEMORY },
                    onNavigateToDevice = { currentScreen = AppScreen.DEVICE },
                    onNavigateToAdmin = { currentScreen = AppScreen.ADMIN },
                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                )
            }

            AppScreen.VOICE -> {
                VoiceAssistantScreen(
                    jarvisViewModel = jarvisViewModel,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }

            AppScreen.CHAT -> {
                ChatScreen(
                    jarvisViewModel = jarvisViewModel,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }

            AppScreen.CODING -> {
                CodingAssistantScreen(
                    codingViewModel = codingViewModel,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }

            AppScreen.MEMORY -> {
                MemoryScreen(
                    jarvisViewModel = jarvisViewModel,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }

            AppScreen.DEVICE -> {
                DeviceAssistantScreen(
                    jarvisViewModel = jarvisViewModel,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }

            AppScreen.ADMIN -> {
                AdminScreen(
                    adminViewModel = adminViewModel,
                    isAdminUser = isAdmin,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }

            AppScreen.SETTINGS -> {
                SettingsScreen(
                    userProfile = userProfile,
                    currentLanguageMode = jarvisViewModel.userLanguagePreference,
                    onLanguageSelected = { lang ->
                        jarvisViewModel.userLanguagePreference = lang
                        authViewModel.updateLanguagePreference(lang)
                    },
                    authViewModel = authViewModel,
                    onBack = { currentScreen = AppScreen.DASHBOARD }
                )
            }
        }

        // Sensitive action confirmation modal
        pendingConfirmation?.let { action ->
            ConfirmationDialog(
                title = action.title,
                description = action.description,
                onConfirm = { jarvisViewModel.confirmPendingAction() },
                onDismiss = { jarvisViewModel.dismissConfirmation() }
            )
        }
    }
}
