package com.mindforge.game.nameface.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.mindforge.game.nameface.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameFaceScreen(
    difficulty: DifficultyLevel,
    onNavigateBack: () -> Unit,
    viewModel: NameFaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    LaunchedEffect(difficulty) {
        viewModel.initializeGame(difficulty)
        viewModel.handleEvent(NameFaceEvent.StartGame)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Name-Face Associator") },
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
                    when (uiState!!.nameFacePhase) {
                        NameFacePhase.STUDY -> StudyPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        NameFacePhase.TEST -> TestPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        NameFacePhase.REVIEW -> ReviewPhaseScreen(
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
private fun StudyPhaseScreen(
    state: NameFaceState,
    onEvent: (NameFaceEvent) -> Unit
) {
    val currentPerson = state.peopleToLearn.getOrNull(state.currentStudyIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        Text(
            text = "Ronda ${state.currentRound}/${state.totalRounds}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Estudia: ${state.currentStudyIndex + 1}/${state.peopleToLearn.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (state.currentStudyIndex + 1).toFloat() / state.peopleToLearn.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (currentPerson != null) {
            PersonCard(
                person = currentPerson,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Memoriza la información...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersonCard(
    person: PersonCard,
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showAvatar) {
                // Avatar circle with initials
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(person.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${person.firstName.first()}${person.lastName.first()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = person.fullName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (person.role != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = person.role,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            if (person.company != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = person.company,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (person.department != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Departamento: ${person.department}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TestPhaseScreen(
    state: NameFaceState,
    onEvent: (NameFaceEvent) -> Unit
) {
    val currentQuestion = state.currentQuestion
    var selectedPersonId by remember(currentQuestion) { mutableStateOf<String?>(null) }
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
            Text(
                text = currentQuestion.questionText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // For FACE_FROM_NAME, show person reference
            if (currentQuestion.type == QuestionType.FACE_FROM_NAME) {
                // Show options as avatar cards
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentQuestion.options) { option ->
                        OptionPersonCard(
                            person = option,
                            isSelected = selectedPersonId == option.id,
                            isCorrect = isAnswered && option.id == currentQuestion.correctPerson.id,
                            isWrong = isAnswered && selectedPersonId == option.id && option.id != currentQuestion.correctPerson.id,
                            onClick = {
                                if (!isAnswered) {
                                    selectedPersonId = option.id
                                }
                            }
                        )
                    }
                }
            } else {
                // Show face of correct person
                PersonCard(
                    person = currentQuestion.correctPerson,
                    modifier = Modifier.fillMaxWidth(),
                    showAvatar = currentQuestion.type != QuestionType.ROLE
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Show text options
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentQuestion.options) { option ->
                        val optionText = when (currentQuestion.type) {
                            QuestionType.NAME -> option.fullName
                            QuestionType.ROLE -> option.role ?: "Sin rol"
                            else -> option.fullName
                        }

                        OptionCard(
                            text = optionText,
                            isSelected = selectedPersonId == option.id,
                            isCorrect = isAnswered && option.id == currentQuestion.correctPerson.id,
                            isWrong = isAnswered && selectedPersonId == option.id && option.id != currentQuestion.correctPerson.id,
                            onClick = {
                                if (!isAnswered) {
                                    selectedPersonId = option.id
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = {
                    if (!isAnswered && selectedPersonId != null) {
                        onEvent(NameFaceEvent.AnswerQuestion(currentQuestion.id, selectedPersonId!!))
                        showFeedback = true
                    } else if (isAnswered) {
                        if (state.currentQuestionIndex < state.questions.size - 1) {
                            onEvent(NameFaceEvent.NextQuestion)
                        } else {
                            onEvent(NameFaceEvent.CompleteTest)
                        }
                        showFeedback = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedPersonId != null
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
                        text = if (wasCorrect) "¡Correcto! ✓" else "Incorrecto ✗",
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
private fun OptionPersonCard(
    person: PersonCard,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFE53935)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isCorrect && !isWrong) { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(person.avatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${person.firstName.first()}${person.lastName.first()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (person.role != null) {
                    Text(
                        text = person.role,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isCorrect) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Correcto",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewPhaseScreen(
    state: NameFaceState,
    onEvent: (NameFaceEvent) -> Unit
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
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = {
                if (state.currentRound < state.totalRounds) {
                    onEvent(NameFaceEvent.NextRound)
                } else {
                    onEvent(NameFaceEvent.FinishGame)
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
    result: NameFaceResult,
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
                DetailRow("Respuestas correctas", "${result.correctAnswers}/${result.totalPeople}")
                DetailRow("Rondas perfectas", "${result.perfectRounds}/${result.totalRounds}")
                DetailRow("Tiempo promedio de estudio", "${result.averageStudyTime / 1000}s")
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
