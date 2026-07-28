package com.etsafety.admin.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.etsafety.admin.data.PremiumUser
import com.etsafety.admin.data.PremiumUserDao
import com.etsafety.admin.ui.theme.Green50
import com.etsafety.admin.ui.theme.Red50
import com.etsafety.admin.ui.theme.Amber50
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    dao: PremiumUserDao
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<PremiumUser?>(null) }
    var showEditDialog by remember { mutableStateOf<PremiumUser?>(null) }

    val allUsers by dao.getAllUsers().collectAsState(initial = emptyList())
    val currentTime = System.currentTimeMillis()
    val expiryLimit = currentTime + 7L * 24 * 60 * 60 * 1000

    val filteredUsers = allUsers.filter { user ->
        searchQuery.isBlank() || user.identifier.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User List",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${allUsers.size} total users",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val duplicates = mutableListOf<Long>()
                            val identifierGroups = allUsers.groupBy { it.identifier }
                            identifierGroups.forEach { (identifier, users) ->
                                if (users.size > 1) {
                                    // Keep the oldest, delete the rest
                                    val sorted = users.sortedBy { it.createdAt }
                                    duplicates.addAll(sorted.drop(1).map { it.id })
                                }
                            }
                            if (duplicates.isNotEmpty()) {
                                dao.deleteUsersByIds(duplicates)
                                snackbarHostState.showSnackbar("Removed ${duplicates.size} duplicate entries")
                            } else {
                                snackbarHostState.showSnackbar("No duplicates found")
                            }
                        }
                    }
                ) {
                    Text("Remove Duplicates")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search users...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User List
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No users found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredUsers,
                        key = { it.id }
                    ) { user ->
                        val isExpiringSoon = user.expiryTimestamp > currentTime && user.expiryTimestamp <= expiryLimit
                        val isExpired = user.expiryTimestamp <= currentTime
                        val isActive = user.expiryTimestamp > currentTime

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteDialog = user
                                    false
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> Red50
                                        else -> Color.Transparent
                                    },
                                    label = "swipe_bg"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            UserCard(
                                user = user,
                                isActive = isActive,
                                isExpiringSoon = isExpiringSoon,
                                isExpired = isExpired,
                                onClick = { showEditDialog = user }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Dialog
    showDeleteDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this user?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            dao.deleteUser(user)
                            snackbarHostState.showSnackbar("User deleted")
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Red50)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Dialog
    showEditDialog?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { showEditDialog = null },
            onSave = { updatedUser ->
                scope.launch {
                    dao.updateUser(updatedUser)
                    snackbarHostState.showSnackbar("User updated")
                }
                showEditDialog = null
            }
        )
    }
}

@Composable
private fun UserCard(
    user: PremiumUser,
    isActive: Boolean,
    isExpiringSoon: Boolean,
    isExpired: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val statusColor = when {
        isExpired -> Red50
        isExpiringSoon -> Amber50
        else -> Green50
    }
    val statusText = when {
        isExpired -> "Expired"
        isExpiringSoon -> "Expiring Soon"
        else -> "Active"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.identifier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.tier.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Expires: ${dateFormat.format(Date(user.expiryTimestamp))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    user: PremiumUser,
    onDismiss: () -> Unit,
    onSave: (PremiumUser) -> Unit
) {
    var selectedTier by remember { mutableStateOf(user.tier) }
    var selectedMonths by remember { mutableStateOf(1) }
    var tierExpanded by remember { mutableStateOf(false) }
    var monthsExpanded by remember { mutableStateOf(false) }

    val tierOptions = listOf("gold", "platinum")
    val monthOptions = (1..12).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column {
                Text(
                    text = user.identifier,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tier Selection
                ExposedDropdownMenuBox(
                    expanded = tierExpanded,
                    onExpandedChange = { tierExpanded = !tierExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTier.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tier") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = tierExpanded,
                        onDismissRequest = { tierExpanded = false }
                    ) {
                        tierOptions.forEach { tier ->
                            DropdownMenuItem(
                                text = { Text(tier.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedTier = tier
                                    tierExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Extend Expiry
                ExposedDropdownMenuBox(
                    expanded = monthsExpanded,
                    onExpandedChange = { monthsExpanded = !monthsExpanded }
                ) {
                    OutlinedTextField(
                        value = "$selectedMonths month${if (selectedMonths > 1) "s" else ""}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Extend Expiry By") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = monthsExpanded,
                        onDismissRequest = { monthsExpanded = false }
                    ) {
                        monthOptions.forEach { months ->
                            DropdownMenuItem(
                                text = { Text("$months month${if (months > 1) "s" else ""}") },
                                onClick = {
                                    selectedMonths = months
                                    monthsExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val extendedExpiry = user.expiryTimestamp +
                            selectedMonths.toLong() * 30L * 24 * 60 * 60 * 1000
                    onSave(
                        user.copy(
                            tier = selectedTier,
                            expiryTimestamp = extendedExpiry
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
