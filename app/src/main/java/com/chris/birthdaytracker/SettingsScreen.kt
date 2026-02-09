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
import androidx.compose.ui.graphics.vector.ImageVector


@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val soundEnabled by SettingsStore.soundEnabled(context).collectAsState(true)
    val confettiEnabled by SettingsStore.confettiEnabled(context).collectAsState(true)
    val notificationsEnabled by SettingsStore.notificationsEnabled(context).collectAsState(true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("Settings", style = MaterialTheme.typography.titleLarge)

        IconSwitchRow(
            Icons.Default.VolumeUp,
            "Sound",
            soundEnabled
        ) {
            scope.launch {
                SettingsStore.setSound(context, !soundEnabled)
            }
        }

        IconSwitchRow(
            Icons.Default.Celebration,
            "Confetti",
            confettiEnabled
        ) {
            scope.launch {
                SettingsStore.setConfetti(context, !confettiEnabled)
            }
        }

        IconSwitchRow(
            Icons.Default.Notifications,
            "Notifications",
            notificationsEnabled
        ) {
            scope.launch {
                SettingsStore.setNotifications(context, !notificationsEnabled)
            }
        }

        Divider()

        Button(
            onClick = {
                BirthdayNotificationScheduler.scheduleTestNotification(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.NotificationsActive, null)
            Spacer(Modifier.width(8.dp))
            Text("Send test notification (10s)")
        }

        Divider()

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:you@example.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Birthday Tracker Feedback")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Email, null)
            Spacer(Modifier.width(8.dp))
            Text("Send feedback")
        }
    }
}

@Composable
private fun IconSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
        Switch(checked, onCheckedChange = { onToggle() })
    }
}
