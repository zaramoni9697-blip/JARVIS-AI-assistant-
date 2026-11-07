package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.repository.UserRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.navigation.MainAppContainer
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val userRepository = remember { UserRepository(this) }
                val authViewModel = remember { AuthViewModel(userRepository) }

                LaunchedEffect(Unit) {
                    authViewModel.attemptAutoSignIn(this@MainActivity)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JarvisBackground
                ) {
                    Box(modifier = Modifier.safeDrawingPadding()) {
                        AppRoot(authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppRoot(authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()

    if (currentUser == null) {
        AuthScreen(
            authViewModel = authViewModel,
            onAuthSuccess = {}
        )
    } else {
        MainAppContainer(
            user = currentUser!!,
            userProfile = userProfile,
            authViewModel = authViewModel
        )
    }
}
