package com.chris.birthdaytracker

import androidx.compose.runtime.*
import com.chris.birthdaytracker.ui.theme.BirthdayTrackerTheme

@Composable
fun SplashHost() {
    BirthdayTrackerTheme {
        var showSplash by remember { mutableStateOf(true) }

        if (showSplash) {
            AnimatedSplashScreen {
                showSplash = false
            }
        } else {
            AppRoot()
        }
    }
}
