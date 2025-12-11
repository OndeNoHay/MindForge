package com.mindforge.game.meetingrecall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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
import com.mindforge.game.meetingrecall.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRecallScreen(
    difficulty: DifficultyLevel,
    onNavigateBack: () -> Unit,
    viewModel: MeetingRecallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    LaunchedEffect(difficulty) {
        viewModel.initializeGame(difficulty)
        viewModel.handleEvent(MeetingRecallEvent.StartGame)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meeting Recall") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState == null -> {
                    LoadingScreen()
                }
                uiState?.phase == GamePhase.FINISHED && gameResult != null -> {
                    ResultsScreen(
                        result = gameResult!!,
                        onNavigateBack = onNavigateBack
                    )
                }
                uiState != null -> {
                    when (uiState!!.meetingRecallPhase) {
                        MeetingRecallPhase.READING -> ReadingPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        MeetingRecallPhase.TEST -> TestPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        MeetingRecallPhase.REVIEW -> ReviewPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ReadingPhaseScreen(
    state: MeetingRecallState,
    onEvent: (MeetingRecallEvent) -> Unit
) {
    val meeting = state.currentMeeting ?: return
    val timeRemaining = state.timeRemaining ?: 0L
    val secondsRemaining = (timeRemaining / 1000).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ronda ${state.currentRound}/${state.totalRounds}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (secondsRemaining <= 10) {
                        Color(0xFFE53935).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Text(
                    text = "${secondsRemaining}s",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (secondsRemaining <= 10) Color(0xFFE53935) else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Memoriza los detalles de la reunión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meeting content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            MeetingCard(meeting = meeting)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual finish button (optional)
        OutlinedButton(
            onClick = { onEvent(MeetingRecallEvent.FinishReading) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar Test Ahora")
        }
    }
}

@Composable
private fun MeetingCard(meeting: Meeting) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Title and date
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (meeting.location != null) {
                    DetailChip("📍 ${meeting.location}")
                }
                DetailChip("📅 ${meeting.date}")
                if (meeting.duration != null) {
                    DetailChip("⏱️ ${meeting.duration}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Attendees
            Text(
                text = "Asistentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            meeting.attendees.forEach { attendee ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${attendee.name} - ${attendee.role}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Topics
            meeting.topics.forEachIndexed { index, topic ->
                Text(
                    text = "Tema ${index + 1}: ${topic.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (topic.keyPoints.isNotEmpty()) {
                    Text(
                        "Puntos Clave:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    topic.keyPoints.forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)) {
                            Text("• ", style = MaterialTheme.typography.bodyMedium)
                            Text(point, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (topic.decisions.isNotEmpty()) {
                    Text(
                        "Decisiones:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                    topic.decisions.forEach { decision ->
                        Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)) {
                            Text("✓ ", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CAF50))
                            Text(decision, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (index < meeting.topics.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (meeting.actionItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Tareas Asignadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                meeting.actionItems.forEach { item ->
                    ActionItemCard(item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ActionItemCard(item: ActionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.task,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Asignado a: ${item.assignee}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.deadline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TestPhaseScreen(
    state: MeetingRecallState,
    onEvent: (MeetingRecallEvent) -> Unit
) {
    val currentQuestion = state.currentQuestion
    var selectedAnswer by remember(currentQuestion) { mutableStateOf<String?>(null) }
    var showFeedback by remember(currentQuestion) { mutableStateOf(false) }
    val isAnswered = currentQuestion?.let { state.answeredQuestions.containsKey(it.id) } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Progress
        Text(
            text = "Ronda ${state.currentRound}/${state.totalRounds}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pregunta ${state.currentQuestionIndex + 1}/${state.questions.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { (state.currentQuestionIndex + 1).toFloat() / state.questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question
        if (currentQuestion != null) {
            if (currentQuestion.isDetailQuestion) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFB300).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⭐",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pregunta de detalle - Bonus",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = currentQuestion.questionText,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentQuestion.options) { option ->
                    OptionCard(
                        text = option,
                        isSelected = selectedAnswer == option,
                        isCorrect = isAnswered && option == currentQuestion.correctAnswer,
                        isWrong = isAnswered && selectedAnswer == option && option != currentQuestion.correctAnswer,
                        onClick = {
                            if (!isAnswered) {
                                selectedAnswer = option
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = {
                    if (!isAnswered && selectedAnswer != null) {
                        onEvent(MeetingRecallEvent.AnswerQuestion(currentQuestion.id, selectedAnswer!!))
                        showFeedback = true
                    } else if (isAnswered) {
                        if (state.currentQuestionIndex < state.questions.size - 1) {
                            onEvent(MeetingRecallEvent.NextQuestion)
                        } else {
                            onEvent(MeetingRecallEvent.CompleteTest)
                        }
                        showFeedback = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null
            ) {
                Text(
                    if (!isAnswered) "Confirmar" else {
                        if (state.currentQuestionIndex < state.questions.size - 1) "Siguiente" else "Ver Resultados"
                    }
                )
            }

            // Feedback
            AnimatedVisibility(
                visible = showFeedback && isAnswered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val wasCorrect = state.answeredQuestions[currentQuestion.id] == true
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (wasCorrect) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFE53935).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = if (wasCorrect) "¡Correcto! ✓" else "Incorrecto ✗\nRespuesta correcta: ${currentQuestion.correctAnswer}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (wasCorrect) Color(0xFF4CAF50) else Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFE53935)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCorrect || isWrong -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isCorrect && !isWrong) { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCorrect || isWrong) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (isCorrect) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Correcto",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ReviewPhaseScreen(
    state: MeetingRecallState,
    onEvent: (MeetingRecallEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ronda ${state.currentRound} Completada",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Results summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(state.accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Precisión",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Correctas", state.correctAnswers.toString(), Color(0xFF4CAF50))
                    StatItem("Incorrectas", state.incorrectAnswers.toString(), Color(0xFFE53935))
                    if (state.detailQuestionsCorrect > 0) {
                        StatItem("Bonus", state.detailQuestionsCorrect.toString(), Color(0xFFFFB300))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = {
                if (state.currentRound < state.totalRounds) {
                    onEvent(MeetingRecallEvent.NextRound)
                } else {
                    onEvent(MeetingRecallEvent.FinishGame)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.currentRound < state.totalRounds) {
                    "Siguiente Ronda (${state.currentRound + 1}/${state.totalRounds})"
                } else {
                    "Finalizar Juego"
                }
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultsScreen(
    result: MeetingRecallResult,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (result.passed) "¡Excelente!" else "Intenta de nuevo",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (result.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main stats card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = result.score.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Puntos", style = MaterialTheme.typography.bodyMedium)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "+${result.xpEarned}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text("XP", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Detailed stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Precisión", "${(result.accuracy * 100).toInt()}%")
                DetailRow("Respuestas correctas", "${result.correctAnswers}/${result.totalQuestions}")
                DetailRow("Rondas perfectas", "${result.perfectRounds}/${result.totalRounds}")
                if (result.detailQuestionsCorrect > 0) {
                    DetailRow("Preguntas de detalle", result.detailQuestionsCorrect.toString())
                }
                DetailRow("Tiempo promedio de lectura", "${result.averageReadingTime / 1000}s")
                DetailRow("Tiempo total", "${result.timeTaken / 1000}s")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al Menú")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
