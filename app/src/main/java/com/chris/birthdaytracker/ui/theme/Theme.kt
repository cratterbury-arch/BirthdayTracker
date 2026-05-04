package com.chris.birthdaytracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.chris.birthdaytracker.SettingsStore

private val OledDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD8B4FE),
    onPrimary = Color(0xFF2A063D),
    primaryContainer = Color(0xFF3B1458),
    onPrimaryContainer = Color(0xFFF3E8FF),

    secondary = Color(0xFFC084FC),
    onSecondary = Color(0xFF240B36),
    secondaryContainer = Color(0xFF2D123F),
    onSecondaryContainer = Color(0xFFF3E8FF),

    tertiary = Color(0xFFFFC1E3),
    onTertiary = Color(0xFF3A1026),
    tertiaryContainer = Color(0xFF4A1933),
    onTertiaryContainer = Color(0xFFFFE5F2),

    background = Color(0xFF000000),
    onBackground = Color(0xFFF8F4FF),

    surface = Color(0xFF050407),
    onSurface = Color(0xFFF8F4FF),

    surfaceVariant = Color(0xFF17121F),
    onSurfaceVariant = Color(0xFFD8CFE5),

    outline = Color(0xFF8D7A99),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val ElegantLightColorScheme = lightColorScheme(
    primary = Color(0xFF7E22CE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = Color(0xFF2E1046),

    secondary = Color(0xFF9333EA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF2E1046),

    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1D1B20),

    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1D1B20),

    surfaceVariant = Color(0xFFF0E7F7),
    onSurfaceVariant = Color(0xFF4C4452)
)

@Composable
fun BirthdayTrackerTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val themeSetting by SettingsStore.getTheme(context).collectAsState(initial = "system")

    val darkTheme = when (themeSetting) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) {
        OledDarkColorScheme
    } else {
        ElegantLightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Black.toArgb()
            window.navigationBarColor = Color.Black.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}