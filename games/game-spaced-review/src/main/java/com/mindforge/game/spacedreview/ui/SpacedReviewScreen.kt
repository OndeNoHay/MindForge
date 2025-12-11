package com.mindforge.game.spacedreview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.spacedreview.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacedReviewScreen(
    difficulty: DifficultyLevel,
    onNavigateBack: () -> Unit,
    viewModel: SpacedReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    LaunchedEffect(difficulty) {
        viewModel.initializeGame(difficulty)
        viewModel.handleEvent(SpacedReviewEvent.StartSession)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spaced Review") },
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
                    when (uiState!!.spacedReviewPhase) {
                        SpacedReviewPhase.REVIEW -> ReviewScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        SpacedReviewPhase.RESULT -> {
                            // Results handled above
                        }
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
private fun ReviewScreen(
    state: SpacedReviewState,
    onEvent: (SpacedReviewEvent) -> Unit
) {
    val currentItem = state.currentItem

    if (currentItem == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No hay items para revisar hoy")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onEvent(SpacedReviewEvent.FinishSession) }) {
                    Text("Finalizar")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Item ${state.currentItemIndex + 1}/${state.reviewItems.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Category badge
            CategoryBadge(currentItem.category)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { (state.currentItemIndex + 1).toFloat() / state.reviewItems.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Pregunta",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentItem.question,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                if (state.showAnswer) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Respuesta",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentItem.answer,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action based on state
        if (!state.showAnswer) {
            // Show answer button
            Button(
                onClick = { onEvent(SpacedReviewEvent.ShowAnswer) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mostrar Respuesta")
            }
        } else if (!state.reviewedItems.containsKey(currentItem.id)) {
            // Rating buttons
            Text(
                "¿Qué tan bien recordaste?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecallQuality.values().reversed().forEach { quality ->
                    QualityButton(
                        quality = quality,
                        onClick = {
                            onEvent(SpacedReviewEvent.RateRecall(currentItem.id, quality))
                        }
                    )
                }
            }
        } else {
            // Next button
            Button(
                onClick = { onEvent(SpacedReviewEvent.NextItem) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.currentItemIndex < state.reviewItems.size - 1) {
                        "Siguiente Item"
                    } else {
                        "Ver Resultados"
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: ReviewCategory) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(category.color).copy(alpha = 0.2f)
    ) {
        Text(
            text = category.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color(category.color),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QualityButton(
    quality: RecallQuality,
    onClick: () -> Unit
) {
    val backgroundColor = when (quality) {
        RecallQuality.PERFECT -> Color(0xFF4CAF50)
        RecallQuality.CORRECT_HESITANT -> Color(0xFF8BC34A)
        RecallQuality.CORRECT_HARD -> Color(0xFFFFEB3B)
        RecallQuality.INCORRECT_EASY -> Color(0xFFFF9800)
        RecallQuality.INCORRECT_HARD -> Color(0xFFFF5722)
        RecallQuality.COMPLETE_BLACKOUT -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quality.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = CircleShape,
                color = backgroundColor
            ) {
                Text(
                    text = quality.value.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ResultsScreen(
    result: SpacedReviewResult,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (result.passed) "¡Excelente Sesión!" else "Buen Esfuerzo",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (result.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
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

        // Recall quality breakdown
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Calidad de Recuerdo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                RecallQualityRow("Perfecto", result.perfectRecalls, Color(0xFF4CAF50))
                RecallQualityRow("Bueno", result.goodRecalls, Color(0xFF8BC34A))
                RecallQualityRow("Regular", result.okayRecalls, Color(0xFFFFEB3B))
                RecallQualityRow("Pobre", result.poorRecalls, Color(0xFFFF5722))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Estadísticas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                DetailRow("Items revisados", result.itemsReviewed.toString())
                DetailRow("Precisión", "${(result.accuracy * 100).toInt()}%")
                DetailRow("Items pendientes hoy", result.itemsDueToday.toString())
                DetailRow("Items próximos", result.itemsDueSoon.toString())
                DetailRow("Tiempo total", "${result.timeTaken / 1000}s")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al Menú")
        }
    }
}

@Composable
private fun RecallQualityRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
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
