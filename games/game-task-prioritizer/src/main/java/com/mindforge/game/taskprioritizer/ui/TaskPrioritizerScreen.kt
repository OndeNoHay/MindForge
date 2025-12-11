package com.mindforge.game.taskprioritizer.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.taskprioritizer.model.*

/**
 * Main screen for the Task Prioritizer game
 */
@Composable
fun TaskPrioritizerScreen(
    viewModel: TaskPrioritizerViewModel = hiltViewModel(),
    difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeGame(difficulty)
    }

    Scaffold(
        topBar = {
            TaskPrioritizerTopBar(onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TaskPrioritizerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TaskPrioritizerUiState.Playing -> {
                    TaskPrioritizerGameContent(
                        gameState = state.gameState,
                        onPlaceTask = viewModel::onPlaceTask,
                        onDismissFeedback = viewModel::onDismissFeedback,
                        onSubmit = viewModel::onSubmitPlacements,
                        onNextRound = viewModel::onNextRound,
                        onFinish = viewModel::onFinishGame
                    )
                }
                is TaskPrioritizerUiState.Finished -> {
                    TaskPrioritizerResultScreen(
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
private fun TaskPrioritizerTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Task Prioritizer") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Text("←", style = MaterialTheme.typography.headlineMedium)
            }
        }
    )
}

@Composable
private fun TaskPrioritizerGameContent(
    gameState: TaskPrioritizerState,
    onPlaceTask: (String, EisenhowerQuadrant) -> Unit,
    onDismissFeedback: () -> Unit,
    onSubmit: () -> Unit,
    onNextRound: () -> Unit,
    onFinish: () -> Unit
) {
    var selectedTask by remember { mutableStateOf<TaskCard?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Game info header
        GameInfoHeader(gameState)

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        InstructionText(selectedTask)

        Spacer(modifier = Modifier.height(16.dp))

        // Eisenhower Matrix (4 quadrants)
        EisenhowerMatrix(
            gameState = gameState,
            selectedTask = selectedTask,
            onQuadrantClick = { quadrant ->
                selectedTask?.let { task ->
                    onPlaceTask(task.id, quadrant)
                    selectedTask = null
                }
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Available tasks
        AvailableTasksList(
            gameState = gameState,
            selectedTask = selectedTask,
            onTaskSelect = { task ->
                if (gameState.placedTasks.containsKey(task.id)) {
                    // Task already placed
                } else {
                    selectedTask = task
                }
            },
            modifier = Modifier.height(160.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        GameActionButtons(
            gameState = gameState,
            onSubmit = onSubmit,
            onNextRound = onNextRound,
            onFinish = onFinish
        )
    }

    // Feedback dialog
    if (gameState.showFeedback && gameState.feedbackTask != null) {
        FeedbackDialog(
            task = gameState.feedbackTask,
            isCorrect = gameState.isCorrectPlacement,
            onDismiss = onDismissFeedback
        )
    }
}

@Composable
private fun GameInfoHeader(gameState: TaskPrioritizerState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(label = "Ronda", value = "${gameState.currentRound}/${gameState.totalRounds}")
            InfoItem(label = "Puntos", value = gameState.score.toString())
            InfoItem(label = "Precisión", value = "${(gameState.accuracy * 100).toInt()}%")
            gameState.timeRemaining?.let {
                InfoItem(label = "Tiempo", value = "${it / 1000}s")
            }
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InstructionText(selectedTask: TaskCard?) {
    Text(
        text = if (selectedTask != null) {
            "Selecciona el cuadrante correcto para: ${selectedTask.title}"
        } else {
            "Selecciona una tarea y luego el cuadrante apropiado"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EisenhowerMatrix(
    gameState: TaskPrioritizerState,
    selectedTask: TaskCard?,
    onQuadrantClick: (EisenhowerQuadrant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f)) {
            QuadrantCard(
                quadrant = EisenhowerQuadrant.DO_FIRST,
                isSelected = selectedTask != null,
                tasksPlacedCount = gameState.placedTasks.count { it.value == EisenhowerQuadrant.DO_FIRST },
                onClick = { onQuadrantClick(EisenhowerQuadrant.DO_FIRST) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            QuadrantCard(
                quadrant = EisenhowerQuadrant.SCHEDULE,
                isSelected = selectedTask != null,
                tasksPlacedCount = gameState.placedTasks.count { it.value == EisenhowerQuadrant.SCHEDULE },
                onClick = { onQuadrantClick(EisenhowerQuadrant.SCHEDULE) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.weight(1f)) {
            QuadrantCard(
                quadrant = EisenhowerQuadrant.DELEGATE,
                isSelected = selectedTask != null,
                tasksPlacedCount = gameState.placedTasks.count { it.value == EisenhowerQuadrant.DELEGATE },
                onClick = { onQuadrantClick(EisenhowerQuadrant.DELEGATE) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            QuadrantCard(
                quadrant = EisenhowerQuadrant.ELIMINATE,
                isSelected = selectedTask != null,
                tasksPlacedCount = gameState.placedTasks.count { it.value == EisenhowerQuadrant.ELIMINATE },
                onClick = { onQuadrantClick(EisenhowerQuadrant.ELIMINATE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuadrantCard(
    quadrant: EisenhowerQuadrant,
    isSelected: Boolean,
    tasksPlacedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = isSelected) { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(quadrant.color).copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = quadrant.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quadrant.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (tasksPlacedCount > 0) {
                Text(
                    text = "$tasksPlacedCount tarea${if (tasksPlacedCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(quadrant.color)
                )
            }
        }
    }
}

@Composable
private fun AvailableTasksList(
    gameState: TaskPrioritizerState,
    selectedTask: TaskCard?,
    onTaskSelect: (TaskCard) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Tareas Disponibles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gameState.availableTasks) { task ->
                    TaskCardItem(
                        task = task,
                        isSelected = selectedTask?.id == task.id,
                        isPlaced = gameState.placedTasks.containsKey(task.id),
                        onClick = { onTaskSelect(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCardItem(
    task: TaskCard,
    isSelected: Boolean,
    isPlaced: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isPlaced) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPlaced -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
        } else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (isPlaced) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (!isPlaced) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                task.deadline?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⏰ $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun GameActionButtons(
    gameState: TaskPrioritizerState,
    onSubmit: () -> Unit,
    onNextRound: () -> Unit,
    onFinish: () -> Unit
) {
    when (gameState.phase) {
        GamePhase.PLAYING -> {
            if (gameState.allTasksPlaced) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar Respuestas")
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
                Text(
                    if (gameState.currentRound >= gameState.totalRounds)
                        "Finalizar Juego"
                    else
                        "Siguiente Ronda"
                )
            }
        }
        else -> {}
    }
}

@Composable
private fun FeedbackDialog(
    task: TaskCard,
    isCorrect: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCorrect) "¡Correcto! ✓" else "Incorrecto ✗",
                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        },
        text = {
            Column {
                Text("Tarea: ${task.title}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cuadrante correcto: ${task.correctQuadrant.displayName}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.correctQuadrant.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Continuar")
            }
        }
    )
}

@Composable
private fun TaskPrioritizerResultScreen(
    result: TaskPrioritizerResult,
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
            text = "¡Juego Completado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultRow("Puntuación Final", result.score.toString())
                ResultRow("XP Ganado", "+${result.xpEarned}")
                ResultRow("Precisión", "${(result.accuracy * 100).toInt()}%")
                ResultRow("Estado", if (result.passed) "✓ Aprobado" else "✗ Fallado")
                ResultRow("Colocaciones Correctas", result.correctPlacements.toString())
                ResultRow("Rondas Perfectas", "${result.perfectRounds}/${result.totalRounds}")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Jugar de Nuevo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al Menú")
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
