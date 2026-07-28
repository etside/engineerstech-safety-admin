package com.etsafety.admin.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etsafety.admin.data.PassphraseGenerator
import com.etsafety.admin.data.PremiumUser
import com.etsafety.admin.data.PremiumUserDao
import com.etsafety.admin.ui.theme.Blue40
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    dao: PremiumUserDao,
    onUserSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var identifier by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf("gold") }
    var selectedMonths by remember { mutableIntStateOf(1) }
    var generatedPassphrase by remember { mutableStateOf("") }
    var tierExpanded by remember { mutableStateOf(false) }
    var monthsExpanded by remember { mutableStateOf(false) }

    val tierOptions = listOf("gold", "platinum")
    val monthOptions = (1..12).toList()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add New User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Identifier Input
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("Email or Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tier Dropdown
            ExposedDropdownMenuBox(
                expanded = tierExpanded,
                onExpandedChange = { tierExpanded = !tierExpanded }
            ) {
                TextField(
                    value = selectedTier.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tier") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tierExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Months Dropdown
            ExposedDropdownMenuBox(
                expanded = monthsExpanded,
                onExpandedChange = { monthsExpanded = !monthsExpanded }
            ) {
                TextField(
                    value = "$selectedMonths month${if (selectedMonths > 1) "s" else ""}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Duration") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthsExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Generate Passphrase Button
            OutlinedButton(
                onClick = {
                    if (identifier.isNotBlank()) {
                        val expiryTimestamp = System.currentTimeMillis() +
                                selectedMonths.toLong() * 30L * 24 * 60 * 60 * 1000
                        generatedPassphrase = PassphraseGenerator.generatePassphrase(
                            identifier.trim().lowercase(),
                            selectedTier,
                            expiryTimestamp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Blue40
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Passphrase")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generated Passphrase Display
            if (generatedPassphrase.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Generated Passphrase",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generatedPassphrase,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val expiryTimestamp = System.currentTimeMillis() +
                                selectedMonths.toLong() * 30L * 24 * 60 * 60 * 1000
                        Text(
                            text = "Expires: ${dateFormat.format(Date(expiryTimestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Passphrase", generatedPassphrase)
                                    clipboard.setPrimaryClip(clip)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Passphrase copied to clipboard")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.height(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }

                            OutlinedButton(
                                onClick = {
                                    val expiryTs = System.currentTimeMillis() +
                                            selectedMonths.toLong() * 30L * 24 * 60 * 60 * 1000
                                    generatedPassphrase = PassphraseGenerator.generatePassphrase(
                                        identifier.trim().lowercase(),
                                        selectedTier,
                                        expiryTs
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.height(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Regenerate")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        if (identifier.isNotBlank() && generatedPassphrase.isNotBlank()) {
                            val expiryTimestamp = System.currentTimeMillis() +
                                    selectedMonths.toLong() * 30L * 24 * 60 * 60 * 1000
                            val user = PremiumUser(
                                identifier = identifier.trim().lowercase(),
                                tier = selectedTier,
                                expiryTimestamp = expiryTimestamp,
                                createdAt = System.currentTimeMillis()
                            )
                            scope.launch {
                                dao.insertUser(user)
                                snackbarHostState.showSnackbar("User saved successfully")
                                // Reset form
                                identifier = ""
                                selectedTier = "gold"
                                selectedMonths = 1
                                generatedPassphrase = ""
                                onUserSaved()
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill in all fields and generate a passphrase")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save User",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
