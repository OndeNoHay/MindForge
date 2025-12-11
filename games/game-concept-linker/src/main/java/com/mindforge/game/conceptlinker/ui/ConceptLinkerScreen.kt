package com.mindforge.game.conceptlinker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.mindforge.game.conceptlinker.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConceptLinkerScreen(
    difficulty: DifficultyLevel,
    onNavigateBack: () -> Unit,
    viewModel: ConceptLinkerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    LaunchedEffect(difficulty) {
        viewModel.initializeGame(difficulty)
        viewModel.handleEvent(ConceptLinkerEvent.StartGame)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Concept Linker") },
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
                    when (uiState!!.conceptLinkerPhase) {
                        ConceptLinkerPhase.STUDY -> StudyPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        ConceptLinkerPhase.CONNECTING -> ConnectingPhaseScreen(
                            state = uiState!!,
                            onEvent = viewModel::handleEvent
                        )
                        ConceptLinkerPhase.REVIEW -> ReviewPhaseScreen(
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
    state: ConceptLinkerState,
    onEvent: (ConceptLinkerEvent) -> Unit
) {
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Text(
                "Estudia los conceptos y sus relaciones",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nodes list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.nodes) { node ->
                NodeCard(node = node, isSelected = false, onClick = {})
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onEvent(ConceptLinkerEvent.FinishStudy) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar a Conectar")
        }
    }
}

@Composable
private fun NodeCard(
    node: ConceptNode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(node.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = node.category.name.take(1),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = node.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (node.stakeholder != null || node.deadline != null || node.resources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (node.stakeholder != null) {
                            DetailChip("👤 ${node.stakeholder}")
                        }
                        if (node.deadline != null) {
                            DetailChip("📅 ${node.deadline}")
                        }
                        if (node.resources.isNotEmpty()) {
                            DetailChip("🔧 ${node.resources.size}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun ConnectingPhaseScreen(
    state: ConceptLinkerState,
    onEvent: (ConceptLinkerEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Ronda ${state.currentRound}/${state.totalRounds}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Text(
                "Selecciona dos nodos y define su relación",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Conexiones", state.userConnections.size.toString())
            StatCard("Requeridas", state.correctConnections.size.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current connections
        if (state.userConnections.isNotEmpty()) {
            Text(
                "Tus Conexiones:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.userConnections) { connection ->
                    ConnectionCard(
                        connection = connection,
                        nodes = state.nodes,
                        onDelete = { onEvent(ConceptLinkerEvent.DeleteConnection(connection.id)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Nodes
        Text(
            "Conceptos:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.nodes) { node ->
                NodeCard(
                    node = node,
                    isSelected = state.selectedNodeId == node.id,
                    onClick = { onEvent(ConceptLinkerEvent.SelectNode(node.id)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.selectedNodeId != null) {
                OutlinedButton(
                    onClick = { onEvent(ConceptLinkerEvent.CancelSelection) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancelar")
                }
            }

            Button(
                onClick = { onEvent(ConceptLinkerEvent.CompleteRound) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Finalizar Ronda")
            }
        }
    }

    // Relation type selector dialog
    if (state.showRelationTypeSelector && state.pendingConnection != null) {
        RelationTypeSelectorDialog(
            onDismiss = { onEvent(ConceptLinkerEvent.CancelSelection) },
            onSelect = { relationType ->
                onEvent(
                    ConceptLinkerEvent.CreateConnection(
                        state.pendingConnection.first,
                        state.pendingConnection.second,
                        relationType
                    )
                )
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ConnectionCard(
    connection: Connection,
    nodes: List<ConceptNode>,
    onDelete: () -> Unit
) {
    val sourceNode = nodes.find { it.id == connection.sourceNodeId }
    val targetNode = nodes.find { it.id == connection.targetNodeId }

    if (sourceNode != null && targetNode != null) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sourceNode.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = connection.relationType.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = targetNode.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RelationTypeSelectorDialog(
    onDismiss: () -> Unit,
    onSelect: (RelationType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipo de Relación") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RelationType.values()) { relationType ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(relationType) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = relationType.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = relationType.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ReviewPhaseScreen(
    state: ConceptLinkerState,
    onEvent: (ConceptLinkerEvent) -> Unit
) {
    // Calculate round stats
    val userConnectionsCount = state.userConnections.size
    val requiredConnectionsCount = state.correctConnections.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
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

                Text(
                    text = "${(state.completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Completitud",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Conexiones creadas", userConnectionsCount.toString())
                DetailRow("Conexiones requeridas", requiredConnectionsCount.toString())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = {
                if (state.currentRound < state.totalRounds) {
                    onEvent(ConceptLinkerEvent.NextRound)
                } else {
                    onEvent(ConceptLinkerEvent.FinishGame)
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
private fun ResultsScreen(
    result: ConceptLinkerResult,
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
                DetailRow("Completitud", "${(result.completionRate * 100).toInt()}%")
                DetailRow("Conexiones correctas", result.correctConnectionsCount.toString())
                DetailRow("Conexiones incorrectas", result.incorrectConnectionsCount.toString())
                DetailRow("Conexiones perdidas", result.missedConnectionsCount.toString())
                DetailRow("Rondas perfectas", "${result.perfectRounds}/${result.totalRounds}")
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
