package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WidgetSettingsActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            var showBackground by remember { mutableStateOf(prefs.getBoolean("show_bg_active", true)) }
            var transparency by remember { mutableStateOf(prefs.getFloat("alpha_active", 0.7f)) }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Widget Appearance", style = MaterialTheme.typography.headlineMedium)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show Party Background")
                        Checkbox(checked = showBackground, onCheckedChange = { showBackground = it })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Background Opacity: ${(transparency * 100).toInt()}%")
                    Slider(value = transparency, onValueChange = { transparency = it }, valueRange = 0f..1f)

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            saveSettings(showBackground, transparency)
                            finishConfiguration()
                        }
                    ) {
                        Text("Save & Apply")
                    }
                }
            }
        }
    }

    private fun saveSettings(showBg: Boolean, alpha: Float) {
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("show_bg_active", showBg)
            .putFloat("alpha_active", alpha)
            .apply()

        MainScope().launch {
            BirthdayWidget().updateAll(this@WidgetSettingsActivity)
        }
    }

    private fun finishConfiguration() {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}