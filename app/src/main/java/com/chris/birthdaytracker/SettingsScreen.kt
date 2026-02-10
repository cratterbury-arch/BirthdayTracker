package com.chris.birthdaytracker

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val soundEnabled by SettingsStore.soundEnabled(context)
        .collectAsState(initial = true)

    val confettiEnabled by SettingsStore.confettiEnabled(context)
        .collectAsState(initial = true)

    val notificationsEnabled by SettingsStore.notificationsEnabled(context)
        .collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("Settings", style = MaterialTheme.typography.titleLarge)

        SettingToggle(
            icon = Icons.Default.VolumeUp,
            label = "Sound",
            checked = soundEnabled
        ) {
            scope.launch {
                SettingsStore.setSoundEnabled(context, !soundEnabled)
            }
        }

        SettingToggle(
            icon = Icons.Default.Celebration,
            label = "Confetti",
            checked = confettiEnabled
        ) {
            scope.launch {
                SettingsStore.setConfettiEnabled(context, !confettiEnabled)
            }
        }

        SettingToggle(
            icon = Icons.Default.Notifications,
            label = "Notifications",
            checked = notificationsEnabled
        ) {
            scope.launch {
                SettingsStore.setNotificationsEnabled(context, !notificationsEnabled)
            }
        }

        Divider()

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                BirthdayNotificationScheduler.scheduleTestNotification(context)
            }
        ) {
            Icon(Icons.Default.NotificationsActive, null)
            Spacer(Modifier.width(8.dp))
            Text("Send test notification (10s)")
        }

        Divider()

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:youremail@example.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Birthday Tracker feedback")
                }
                context.startActivity(intent)
            }
        ) {
            Icon(Icons.Default.Email, null)
            Spacer(Modifier.width(8.dp))
            Text("Send feedback")
        }
    }
}

@Composable
private fun SettingToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Text(label)
        }
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}
