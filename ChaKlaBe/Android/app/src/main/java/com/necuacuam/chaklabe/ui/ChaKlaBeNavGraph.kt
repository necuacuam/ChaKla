package com.necuacuam.chaklabe.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.*
import com.necuacuam.chaklabe.ui.config.ConfigScreen
import com.necuacuam.chaklabe.viewmodel.ChaKlaBeViewModel

@Composable
fun ChaKlaBeNavGraph(
    navController: NavHostController,
    viewModel: ChaKlaBeViewModel // or however you manage state
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            val context = LocalContext.current

            ChaKlaBeMainScreen(
                isStreaming = viewModel.isStreaming,
                isLocating = viewModel.isLocating,
                onToggleStream = { viewModel.toggleStream(context) },
                onTakePicture = { viewModel.takePicture(context) },
                streamedFrame = viewModel.streamedFrame,
                statusMessage = viewModel.statusMessage,
                onSettingsClick = { navController.navigate("config") },
                viewModel = viewModel
            )
        }
        composable("config") {
            ConfigScreen(
                ipAddress = viewModel.cameraIpAddress,
                onIpAddressChange = { viewModel.saveCameraIpAddress(it) }, // ✅ NEW
                isStreaming = viewModel.isStreaming
            )
        }
    }
}
