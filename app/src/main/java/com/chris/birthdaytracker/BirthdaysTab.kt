package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun BirthdaysTab(
    contacts: List<ContactModel>,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    BirthdaysScreen(
        contacts = contacts,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing,
        showSearch = true
    )
}
