package com.chris.birthdaytracker

import android.accounts.AccountManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role


@Composable
fun SettingsScreen(onDataChanged: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val soundEnabled by SettingsStore
        .soundEnabled(context)
        .collectAsState(initial = true)

    val confettiEnabled by SettingsStore
        .confettiEnabled(context)
        .collectAsState(initial = true)

    val notificationsEnabled by SettingsStore
        .notificationsEnabled(context)
        .collectAsState(initial = true)

    val currentTheme by SettingsStore
        .getTheme(context)
        .collectAsState(initial = "system")

    val enabledCalendarAccounts by SettingsStore
        .enabledCalendarAccounts(context)
        .collectAsState(initial = emptySet())

    val disabledPhoneAccounts by SettingsStore
        .disabledPhoneAccounts(context)
        .collectAsState(initial = emptySet())

    val googleAccounts = remember {
        try {
            AccountManager.get(context).getAccountsByType("com.google").map { it.name }
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    var testCountdown by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(testCountdown) {
        if (testCountdown != null) {
            while (testCountdown!! > 0) {
                delay(1_000)
                testCountdown = testCountdown!! - 1
            }
            testCountdown = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Text("General", style = MaterialTheme.typography.titleMedium)

        IconSwitchRow(
            icon = Icons.Default.VolumeUp,
            title = "Sound",
            checked = soundEnabled,
            onToggle = {
                scope.launch {
                    SettingsStore.setSound(context, !soundEnabled)
                }
            }
        )

        IconSwitchRow(
            icon = Icons.Default.Celebration,
            title = "Confetti",
            checked = confettiEnabled,
            onToggle = {
                scope.launch {
                    SettingsStore.setConfetti(context, !confettiEnabled)
                }
            }
        )

        Divider()

        Text("Theme", style = MaterialTheme.typography.titleMedium)
        
        ThemeSelectionSection(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                scope.launch {
                    SettingsStore.setTheme(context, theme)
                    BirthdayApplication.applyTheme(theme)
                }
            }
        )

        Divider()

        Text("Birthday Sources", style = MaterialTheme.typography.titleMedium)
        Text("By default, we import birthdays from your phone's address book. You can also sync specific Google Calendars below.", 
            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        if (googleAccounts.isEmpty()) {
            Text(
                "No Google accounts found on device.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            googleAccounts.forEach { email ->
                val calendarEnabled = email in enabledCalendarAccounts
                val phoneDisabled = email in disabledPhoneAccounts

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(Modifier.height(8.dp))

                        IconSwitchRow(
                            icon = Icons.Default.ContactPage,
                            title = "Phone Contacts",
                            checked = !phoneDisabled,
                            onToggle = {
                                scope.launch {
                                    SettingsStore.togglePhoneAccount(context, email)
                                    onDataChanged()
                                    BirthdayWidgetProvider.refreshAllWidgets(context)
                                }
                            }
                        )

                        IconSwitchRow(
                            icon = Icons.Default.CalendarToday,
                            title = "Google Calendar",
                            checked = calendarEnabled,
                            onToggle = {
                                scope.launch {
                                    SettingsStore.toggleCalendarAccount(context, email)
                                    onDataChanged()
                                    BirthdayWidgetProvider.refreshAllWidgets(context)
                                }
                            }
                        )
                    }
                }
            }
        }

        Divider()

        Text("Notifications", style = MaterialTheme.typography.titleMedium)

        IconSwitchRow(
            icon = Icons.Default.Notifications,
            title = "Birthday notifications",
            checked = notificationsEnabled,
            onToggle = {
                scope.launch {
                    SettingsStore.setNotifications(context, !notificationsEnabled)
                }
            }
        )

        Button(
            onClick = {
                BirthdayNotificationScheduler.scheduleTestNotification(context)
                testCountdown = 5
            },
            enabled = testCountdown == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (testCountdown == null)
                    "Send test notification (5s)"
                else
                    "Sending in ${testCountdown}s…"
            )
        }

        Divider()

        Text("About", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://cratterbury-arch.github.io/BirthdayTracker/privacy-policy.html")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Default.PrivacyTip, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Privacy Policy")
        }

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:cratterbury@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Birthday Tracker Feedback")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Email, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send feedback")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            getAppVersion(context),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun ThemeSelectionSection(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val options = listOf(
        "light" to "Light",
        "dark" to "Dark",
        "system" to "Match System"
    )

    Column(Modifier.selectableGroup()) {
        options.forEach { (value, label) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (value == currentTheme),
                        onClick = { onThemeSelected(value) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (value == currentTheme),
                    onClick = null // null recommended for accessibility with selectable modifier
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
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
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 14.sp)
        }
        Switch(
            checked = checked, 
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.8f)
        )
    }
}

private fun getAppVersion(context: android.content.Context): String {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    return "Version ${info.versionName}"
}
