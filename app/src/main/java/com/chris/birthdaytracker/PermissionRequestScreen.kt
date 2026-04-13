package com.chris.birthdaytracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionRequestScreen(
    onRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF111827),
                        Color(0xFF1F2937)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Image(
                painter = painterResource(id = R.drawable.splashscreen),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Birthday Tracker",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Feature List
            FeatureRow(
                icon = Icons.Default.Contacts,
                title = "Phone Contacts (Default)",
                description = "By default, we securely read birthdays from your phone's address book so you're ready to go instantly."
            )

            FeatureRow(
                icon = Icons.Default.Sync,
                title = "Optional Google Sync",
                description = "Want more? You can toggle Google Calendar sync in Settings to pull in even more dates."
            )

            FeatureRow(
                icon = Icons.Default.EditCalendar,
                title = "Manual Control",
                description = "Easily add, edit, or delete birthdays manually in both the list and the calendar views."
            )

            FeatureRow(
                icon = Icons.Default.NotificationsActive,
                title = "Smart Reminders",
                description = "Never miss a day with timely notifications for all your important dates."
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Your data stays private on your device and is never uploaded to any servers.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFBB86FC),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                lineHeight = 20.sp
            )
        }
    }
}
