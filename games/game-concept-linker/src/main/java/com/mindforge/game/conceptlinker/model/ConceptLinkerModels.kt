package com.mindforge.game.conceptlinker.model

import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.game.core.engine.GameEvent
import com.mindforge.game.core.engine.GamePhase
import com.mindforge.game.core.engine.GameResult
import com.mindforge.game.core.engine.GameState

/**
 * Represents a concept node in the graph
 */
data class ConceptNode(
    val id: String,
    val title: String,
    val description: String,
    val category: NodeCategory,
    val stakeholder: String? = null,
    val deadline: String? = null,
    val resources: List<String> = emptyList(),
    val color: Long
) {
    enum class NodeCategory {
        PROJECT,
        TASK,
        STAKEHOLDER,
        RESOURCE,
        MILESTONE
    }
}

/**
 * Type of relationship between nodes
 */
enum class RelationType(val displayName: String, val description: String) {
    DEPENDS_ON("Depende de", "Este elemento necesita que el otro se complete primero"),
    BLOCKS("Bloquea a", "Este elemento impide el progreso del otro"),
    RELATED_TO("Relacionado con", "Ambos elementos están conectados temáticamente"),
    SAME_STAKEHOLDER("Mismo responsable", "Ambos elementos tienen el mismo stakeholder"),
    SAME_DEADLINE("Mismo plazo", "Ambos elementos comparten el mismo deadline"),
    RESOURCE_CONFLICT("Conflicto de recursos", "Ambos elementos requieren los mismos recursos limitados")
}

/**
 * A connection between two nodes
 */
data class Connection(
    val id: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val relationType: RelationType
)

/**
 * A predefined correct connection in the scenario
 */
data class CorrectConnection(
    val sourceNodeId: String,
    val targetNodeId: String,
    val validRelationTypes: List<RelationType>  // Multiple types may be valid
)

/**
 * Game phase specific to Concept Linker
 */
enum class ConceptLinkerPhase {
    STUDY,       // Study the nodes
    CONNECTING,  // Make connections
    REVIEW       // Review results
}

/**
 * State of the Concept Linker game
 */
data class ConceptLinkerState(
    override val phase: GamePhase = GamePhase.READY,
    override val score: Int = 0,
    override val timeRemaining: Long? = null,
    override val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    // Concept Linker specific state
    val conceptLinkerPhase: ConceptLinkerPhase = ConceptLinkerPhase.STUDY,
    val currentRound: Int = 0,
    val totalRounds: Int = 3,
    val nodes: List<ConceptNode> = emptyList(),
    val correctConnections: List<CorrectConnection> = emptyList(),
    val userConnections: List<Connection> = emptyList(),
    val studyTimePerRound: Long = 30000,  // milliseconds

    // Connection creation state
    val selectedNodeId: String? = null,  // First node selected for connection
    val showRelationTypeSelector: Boolean = false,
    val pendingConnection: Pair<String, String>? = null,  // (sourceId, targetId)

    // Scoring
    val correctConnectionsCount: Int = 0,
    val incorrectConnectionsCount: Int = 0,
    val missedConnectionsCount: Int = 0,

    // Timing
    val studyStartTime: Long = 0,
    val connectingStartTime: Long = 0
) : GameState {
    val accuracy: Float
        get() {
            val total = correctConnectionsCount + incorrectConnectionsCount
            return if (total > 0) correctConnectionsCount.toFloat() / total.toFloat() else 0f
        }

    val completionRate: Float
        get() {
            val total = correctConnections.size
            return if (total > 0) correctConnectionsCount.toFloat() / total.toFloat() else 0f
        }
}

/**
 * Events in the Concept Linker game
 */
sealed class ConceptLinkerEvent : GameEvent {
    object StartGame : ConceptLinkerEvent()
    object StartRound : ConceptLinkerEvent()
    object FinishStudy : ConceptLinkerEvent()
    object StartConnecting : ConceptLinkerEvent()
    data class SelectNode(val nodeId: String) : ConceptLinkerEvent()
    object CancelSelection : ConceptLinkerEvent()
    data class CreateConnection(val sourceNodeId: String, val targetNodeId: String, val relationType: RelationType) : ConceptLinkerEvent()
    data class DeleteConnection(val connectionId: String) : ConceptLinkerEvent()
    object CompleteRound : ConceptLinkerEvent()
    object NextRound : ConceptLinkerEvent()
    object PauseGame : ConceptLinkerEvent()
    object ResumeGame : ConceptLinkerEvent()
    object FinishGame : ConceptLinkerEvent()
}

/**
 * Result of a Concept Linker game session
 */
data class ConceptLinkerResult(
    override val score: Int,
    override val accuracy: Float,
    override val timeTaken: Long,
    override val difficulty: DifficultyLevel,
    override val xpEarned: Int,
    override val passed: Boolean,

    // Concept Linker specific results
    val totalRounds: Int,
    val totalPossibleConnections: Int,
    val correctConnectionsCount: Int,
    val incorrectConnectionsCount: Int,
    val missedConnectionsCount: Int,
    val completionRate: Float,
    val perfectRounds: Int
) : GameResult

/**
 * Parameters for different difficulty levels
 */
data class ConceptLinkerParameters(
    val roundsCount: Int,
    val nodesPerRound: Int,
    val requiredConnectionsPerRound: Int,
    val studyTimePerRound: Long,  // milliseconds
    val includeStakeholders: Boolean,
    val includeDeadlines: Boolean,
    val includeResources: Boolean,
    val allowMultipleRelationTypes: Boolean
) {
    companion object {
        fun forDifficulty(level: DifficultyLevel): ConceptLinkerParameters {
            return when (level) {
                DifficultyLevel.BEGINNER -> ConceptLinkerParameters(
                    roundsCount = 2,
                    nodesPerRound = 4,
                    requiredConnectionsPerRound = 3,
                    studyTimePerRound = 30000,  // 30s
                    includeStakeholders = false,
                    includeDeadlines = false,
                    includeResources = false,
                    allowMultipleRelationTypes = false
                )
                DifficultyLevel.EASY -> ConceptLinkerParameters(
                    roundsCount = 2,
                    nodesPerRound = 5,
                    requiredConnectionsPerRound = 4,
                    studyTimePerRound = 35000,  // 35s
                    includeStakeholders = true,
                    includeDeadlines = false,
                    includeResources = false,
                    allowMultipleRelationTypes = false
                )
                DifficultyLevel.MEDIUM -> ConceptLinkerParameters(
                    roundsCount = 3,
                    nodesPerRound = 6,
                    requiredConnectionsPerRound = 5,
                    studyTimePerRound = 40000,  // 40s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = false,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.HARD -> ConceptLinkerParameters(
                    roundsCount = 3,
                    nodesPerRound = 7,
                    requiredConnectionsPerRound = 6,
                    studyTimePerRound = 45000,  // 45s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.EXPERT -> ConceptLinkerParameters(
                    roundsCount = 4,
                    nodesPerRound = 8,
                    requiredConnectionsPerRound = 7,
                    studyTimePerRound = 50000,  // 50s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.MASTER -> ConceptLinkerParameters(
                    roundsCount = 4,
                    nodesPerRound = 9,
                    requiredConnectionsPerRound = 8,
                    studyTimePerRound = 55000,  // 55s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.GRANDMASTER -> ConceptLinkerParameters(
                    roundsCount = 5,
                    nodesPerRound = 10,
                    requiredConnectionsPerRound = 9,
                    studyTimePerRound = 60000,  // 60s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.LEGENDARY -> ConceptLinkerParameters(
                    roundsCount = 5,
                    nodesPerRound = 11,
                    requiredConnectionsPerRound = 10,
                    studyTimePerRound = 65000,  // 65s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.MYTHIC -> ConceptLinkerParameters(
                    roundsCount = 6,
                    nodesPerRound = 12,
                    requiredConnectionsPerRound = 11,
                    studyTimePerRound = 70000,  // 70s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
                DifficultyLevel.DIVINE -> ConceptLinkerParameters(
                    roundsCount = 7,
                    nodesPerRound = 13,
                    requiredConnectionsPerRound = 12,
                    studyTimePerRound = 75000,  // 75s
                    includeStakeholders = true,
                    includeDeadlines = true,
                    includeResources = true,
                    allowMultipleRelationTypes = true
                )
            }
        }
    }
}

/**
 * Scenario template for generating concept networks
 */
data class ScenarioTemplate(
    val name: String,
    val description: String,
    val nodes: List<NodeTemplate>,
    val connections: List<ConnectionTemplate>
)

data class NodeTemplate(
    val title: String,
    val description: String,
    val category: ConceptNode.NodeCategory,
    val stakeholder: String? = null,
    val deadline: String? = null,
    val resources: List<String> = emptyList()
)

data class ConnectionTemplate(
    val sourceIndex: Int,
    val targetIndex: Int,
    val validRelationTypes: List<RelationType>
)
