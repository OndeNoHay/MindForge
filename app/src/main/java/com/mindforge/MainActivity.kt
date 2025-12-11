package com.mindforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mindforge.core.difficulty.DifficultyLevel
import com.mindforge.core.ui.theme.MindForgeTheme
import com.mindforge.game.memorymatrix.ui.MemoryMatrixScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Single Activity Architecture with Jetpack Compose
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindForgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Launch Memory Matrix game directly for testing
                    MemoryMatrixScreen(
                        difficulty = DifficultyLevel.EASY,
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}
