package com.necuacuam.chaklabe.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.necuacuam.chaklabe.viewmodel.ChaKlaBeViewModel

@Composable
fun ChaKlaBeMainScreen(
    isStreaming: Boolean,
    isLocating: Boolean,
    onToggleStream: () -> Unit,
    onTakePicture: () -> Unit,
    streamedFrame: ImageBitmap? = null,
    statusMessage: String,
    onSettingsClick: () -> Unit,
    viewModel: ChaKlaBeViewModel,
    cameraIpAddress: String = viewModel.cameraIpAddress,
    rotationAngle: Int = viewModel.rotationAngle,
    onRotateClockwise: () -> Unit = { viewModel.rotateClockwise() },
    onRotateCounterClockwise: () -> Unit = { viewModel.rotateCounterClockwise() },

    )
 {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onSettingsClick,
                enabled = !isLocating && !isStreaming
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (streamedFrame != null) {
                Image(
                    bitmap = streamedFrame,
                    contentDescription = "Live Stream",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                        .rotate(rotationAngle.toFloat())
                )
            } else {
                // Placeholder
                Text("No Stream", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onToggleStream,
                enabled = !isLocating && cameraIpAddress.isNotBlank()
            ) {
                Text(if (isStreaming) "Stop Stream" else "Start Stream")
            }

            Button(onClick = onTakePicture,
                enabled = isStreaming) {
                Text("Take Picture")
            }

            Row {
                Button(onClick = onRotateCounterClockwise, enabled = isStreaming) {
                    Text("⟲")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = onRotateClockwise, enabled = isStreaming) {
                    Text("⟳")
                }
            }

        }
    }
}
