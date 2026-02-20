package com.chris.birthdaytracker

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun SplashHost() {

    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        AnimatedSplashScreen {
            showSplash = false
        }
    } else {
        AppRoot()
    }
}
