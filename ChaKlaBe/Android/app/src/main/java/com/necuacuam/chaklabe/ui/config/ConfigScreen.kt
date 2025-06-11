package com.necuacuam.chaklali.ui.screens.config

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun ConfigScreen(
    ipAddress: String,
    onIpAddressChange: (String) -> Unit,
    isStreaming: Boolean
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Configuration", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ipAddress,
            onValueChange = { if (!isStreaming) onIpAddressChange(it) },
            label = { Text("Camera IP Address") },
            enabled = !isStreaming,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (isStreaming) {
            Text(
                "IP cannot be changed while streaming.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
