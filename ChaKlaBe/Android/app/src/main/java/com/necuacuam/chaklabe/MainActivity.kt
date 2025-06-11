package com.necuacuam.chaklabe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.necuacuam.chaklabe.ui.ChaKlaBeMainScreen
import com.necuacuam.chaklabe.ui.ChaKlaBeNavGraph
import com.necuacuam.chaklabe.viewmodel.ChaKlaBeViewModel
import com.necuacuam.chaklabe.ui.theme.ChaKlaBeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChaKlaBeTheme {
                val viewModel: ChaKlaBeViewModel = viewModel()
                val context = applicationContext

                // ✅ Safe way to run init(context) only once
                LaunchedEffect(Unit) {
                    viewModel.init(context)
                }

                val navController = rememberNavController()
                ChaKlaBeNavGraph(navController = navController, viewModel = viewModel)
            }
        }
    }
}
