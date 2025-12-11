package com.mindforge.game.memorymatrix.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindforge.game.core.difficulty.DifficultyLevel
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.memorymatrix.model.CellPosition
import com.mindforge.game.memorymatrix.model.MemoryMatrixState

/**
 * Main screen for the Memory Matrix game
 */
@Composable
fun MemoryMatrixScreen(
    viewModel: MemoryMatrixViewModel = hiltViewModel(),
    difficulty: DifficultyLevel = DifficultyLevel.EASY,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeGame(difficulty)
    }

    Scaffold(
        topBar = {
            MemoryMatrixTopBar(
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MemoryMatrixUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MemoryMatrixUiState.Playing -> {
                    MemoryMatrixGameContent(
                        gameState = state.gameState,
                        onCellTapped = viewModel::onCellTapped,
                        onSubmit = viewModel::onSubmitSelections,
                        onNextRound = viewModel::onNextRound,
                        onFinish = viewModel::onFinishGame
                    )
                }
                is MemoryMatrixUiState.Finished -> {
                    MemoryMatrixResultScreen(
                        result = state.result,
                        onPlayAgain = { viewModel.initializeGame(difficulty) },
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryMatrixTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { Text("Memory Matrix") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Text("←", style = MaterialTheme.typography.headlineMedium)
            }
        }
    )
}

@Composable
private fun MemoryMatrixGameContent(
    gameState: MemoryMatrixState,
    onCellTapped: (CellPosition) -> Unit,
    onSubmit: () -> Unit,
    onNextRound: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game info header
        GameInfoHeader(gameState)

        Spacer(modifier = Modifier.height(24.dp))

        // Instructions
        InstructionText(gameState)

        Spacer(modifier = Modifier.height(32.dp))

        // Memory grid
        MemoryGrid(
            gameState = gameState,
            onCellTapped = onCellTapped,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        GameActionButtons(
            gameState = gameState,
            onSubmit = onSubmit,
            onNextRound = onNextRound,
            onFinish = onFinish
        )
    }
}

@Composable
private fun GameInfoHeader(gameState: MemoryMatrixState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(label = "Round", value = "${gameState.currentRound}/${gameState.totalRounds}")
            InfoItem(label = "Score", value = gameState.score.toString())
            InfoItem(label = "Level", value = gameState.difficulty.name)
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InstructionText(gameState: MemoryMatrixState) {
    val text = when {
        gameState.isShowingPattern -> "Memorize the pattern..."
        gameState.isAcceptingInput -> "Tap the cells that were highlighted"
        gameState.phase == GamePhase.REVIEW -> "Review your answer"
        else -> "Get ready!"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun MemoryGrid(
    gameState: MemoryMatrixState,
    onCellTapped: (CellPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val gridSize = gameState.gridSize
        val maxCellSize = (maxWidth.value.coerceAtMost(maxHeight.value) / gridSize - 8).dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until gridSize) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until gridSize) {
                        val position = CellPosition(row, col)
                        MatrixCell(
                            position = position,
                            isTarget = position in gameState.targetCells,
                            isSelected = position in gameState.selectedCells,
                            isRevealed = position in gameState.revealedCells,
                            isShowingPattern = gameState.isShowingPattern,
                            isAcceptingInput = gameState.isAcceptingInput,
                            onTap = { onCellTapped(position) },
                            size = maxCellSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixCell(
    position: CellPosition,
    isTarget: Boolean,
    isSelected: Boolean,
    isRevealed: Boolean,
    isShowingPattern: Boolean,
    isAcceptingInput: Boolean,
    onTap: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    val backgroundColor = when {
        isShowingPattern && isTarget -> MaterialTheme.colorScheme.primary
        isRevealed && isTarget && isSelected -> Color(0xFF4CAF50) // Correct
        isRevealed && isTarget && !isSelected -> Color(0xFFFFC107) // Missed
        isRevealed && !isTarget && isSelected -> Color(0xFFF44336) // Wrong
        isSelected && isAcceptingInput -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cell_scale"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isAcceptingInput) { onTap() },
        contentAlignment = Alignment.Center
    ) {
        // Optional: Add feedback icons
        AnimatedVisibility(
            visible = isRevealed,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            when {
                isTarget && isSelected -> Text("✓", style = MaterialTheme.typography.headlineSmall)
                isTarget && !isSelected -> Text("○", style = MaterialTheme.typography.headlineSmall)
                !isTarget && isSelected -> Text("✗", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun GameActionButtons(
    gameState: MemoryMatrixState,
    onSubmit: () -> Unit,
    onNextRound: () -> Unit,
    onFinish: () -> Unit
) {
    when (gameState.phase) {
        GamePhase.PLAYING -> {
            if (gameState.isAcceptingInput) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = gameState.selectedCells.isNotEmpty()
                ) {
                    Text("Submit Answer")
                }
            }
        }
        GamePhase.REVIEW -> {
            Button(
                onClick = {
                    if (gameState.currentRound >= gameState.totalRounds) {
                        onFinish()
                    } else {
                        onNextRound()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (gameState.currentRound >= gameState.totalRounds) "Finish Game" else "Next Round")
            }
        }
        else -> {}
    }
}

@Composable
private fun MemoryMatrixResultScreen(
    result: com.mindforge.game.memorymatrix.model.MemoryMatrixResult,
    onPlayAgain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultRow("Final Score", result.finalScore.toString())
                ResultRow("XP Earned", "+${result.xpEarned}")
                ResultRow("Accuracy", "${(result.accuracy * 100).toInt()}%")
                ResultRow("Correct Selections", result.correctSelections.toString())
                ResultRow("Perfect Rounds", "${result.perfectRounds}/${result.totalRounds}")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play Again")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Menu")
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
