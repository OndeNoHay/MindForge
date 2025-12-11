package com.mindforge.game.spacedreview.engine

import com.mindforge.game.spacedreview.model.ReviewCategory
import com.mindforge.game.spacedreview.model.ReviewItem
import com.mindforge.game.spacedreview.model.ReviewItemTemplate
import java.time.LocalDate
import kotlin.random.Random

/**
 * Generates review items for spaced repetition
 */
class ReviewItemGenerator {

    private var itemIdCounter = 0

    /**
     * Generate initial review items
     */
    fun generateInitialItems(count: Int): List<ReviewItem> {
        val templates = sampleTemplates.shuffled().take(count)
        val today = LocalDate.now()

        return templates.map { template ->
            ReviewItem(
                id = "review_${itemIdCounter++}",
                question = template.question,
                answer = template.answer,
                category = template.category,
                easinessFactor = 2.5f,
                interval = 0,
                repetitions = 0,
                nextReviewDate = today,  // Due today
                lastReviewDate = null,
                createdDate = today
            )
        }
    }

    /**
     * Generate items with varied due dates for testing
     */
    fun generateVariedItems(count: Int): List<ReviewItem> {
        val templates = sampleTemplates.shuffled().take(count)
        val today = LocalDate.now()

        return templates.mapIndexed { index, template ->
            // Create items with different review states
            val daysOffset = when (index % 5) {
                0 -> 0      // Due today
                1 -> -1     // Overdue by 1 day
                2 -> 1      // Due tomorrow
                3 -> -3     // Overdue by 3 days
                else -> 2   // Due in 2 days
            }

            val repetitions = Random.nextInt(0, 4)
            val easinessFactor = 2.5f + Random.nextFloat() * 0.5f - 0.25f  // 2.25-2.75

            ReviewItem(
                id = "review_${itemIdCounter++}",
                question = template.question,
                answer = template.answer,
                category = template.category,
                easinessFactor = easinessFactor,
                interval = if (repetitions == 0) 0 else Random.nextInt(1, 10),
                repetitions = repetitions,
                nextReviewDate = today.plusDays(daysOffset.toLong()),
                lastReviewDate = if (repetitions > 0) today.minusDays(Random.nextLong(1, 10)) else null,
                createdDate = today.minusDays(Random.nextLong(5, 30))
            )
        }
    }

    fun reset() {
        itemIdCounter = 0
    }

    // Sample review items from different game types
    private val sampleTemplates = listOf(
        // Memory items
        ReviewItemTemplate(
            "¿Cuál es la secuencia de 4 números que memorizaste en nivel 3?",
            "7-4-2-9",
            ReviewCategory.MEMORY
        ),
        ReviewItemTemplate(
            "¿Qué patrón de colores apareció en la esquina superior izquierda?",
            "Azul-Rojo-Verde",
            ReviewCategory.MEMORY
        ),

        // Task prioritization
        ReviewItemTemplate(
            "¿En qué cuadrante de Eisenhower va 'Preparar presentación para junta directiva'?",
            "Hacer Primero (Urgente e Importante)",
            ReviewCategory.TASKS
        ),
        ReviewItemTemplate(
            "¿Cómo se clasifica 'Revisar feed de redes sociales'?",
            "Eliminar (Ni Urgente ni Importante)",
            ReviewCategory.TASKS
        ),
        ReviewItemTemplate(
            "¿Qué tarea debe 'Programarse' según la matriz de Eisenhower?",
            "Planificación estratégica (Importante pero no urgente)",
            ReviewCategory.TASKS
        ),

        // Name-Face associations
        ReviewItemTemplate(
            "¿Quién es el Director de TI que conociste en la reunión?",
            "Carlos López",
            ReviewCategory.PEOPLE
        ),
        ReviewItemTemplate(
            "¿Cuál es el rol de María García en el proyecto?",
            "Product Manager",
            ReviewCategory.PEOPLE
        ),
        ReviewItemTemplate(
            "¿Qué departamento lidera Ana Martínez?",
            "Departamento de Marketing",
            ReviewCategory.PEOPLE
        ),
        ReviewItemTemplate(
            "¿Quién es el Scrum Master del equipo Atlas?",
            "Juan Rodríguez",
            ReviewCategory.PEOPLE
        ),

        // Meeting recall
        ReviewItemTemplate(
            "¿Cuál fue la decisión principal en la Revisión Sprint 23?",
            "Priorizar API de pagos esta semana",
            ReviewCategory.MEETINGS
        ),
        ReviewItemTemplate(
            "¿Qué tarea fue asignada a Juan en la última reunión?",
            "Completar API de pagos para el 20/01",
            ReviewCategory.MEETINGS
        ),
        ReviewItemTemplate(
            "¿Cuál fue el principal bloqueo reportado en el proyecto?",
            "Dependencia externa con proveedor de SMS",
            ReviewCategory.MEETINGS
        ),
        ReviewItemTemplate(
            "¿Qué KPI mejoró en Q4 según el Comité de Dirección?",
            "Ingresos 15% por encima del objetivo",
            ReviewCategory.MEETINGS
        ),

        // Concept linking
        ReviewItemTemplate(
            "¿Qué relación existe entre 'API de Productos' y 'Desarrollo Frontend'?",
            "Bloquea (API debe completarse antes del Frontend)",
            ReviewCategory.CONCEPTS
        ),
        ReviewItemTemplate(
            "¿Cómo se relacionan 'Diseño UI/UX' y 'Desarrollo Frontend'?",
            "Depende de (Frontend depende del diseño)",
            ReviewCategory.CONCEPTS
        ),
        ReviewItemTemplate(
            "¿Qué conflicto existe entre 'API de Pagos' y 'API de Productos'?",
            "Conflicto de recursos (Mismo equipo Backend y servidor)",
            ReviewCategory.CONCEPTS
        ),
        ReviewItemTemplate(
            "¿Cuál es la relación entre 'Campaña en Redes' y 'Email Marketing'?",
            "Mismo deadline y presupuesto compartido",
            ReviewCategory.CONCEPTS
        ),

        // General business knowledge
        ReviewItemTemplate(
            "¿Qué significa MVP en desarrollo de producto?",
            "Minimum Viable Product (Producto Mínimo Viable)",
            ReviewCategory.GENERAL
        ),
        ReviewItemTemplate(
            "¿Qué es un Sprint en metodología Agile?",
            "Iteración de desarrollo de 1-4 semanas con objetivos específicos",
            ReviewCategory.GENERAL
        ),
        ReviewItemTemplate(
            "¿Qué representa el cuadrante 'Programar' en la matriz de Eisenhower?",
            "Tareas importantes pero no urgentes que requieren planificación",
            ReviewCategory.GENERAL
        ),
        ReviewItemTemplate(
            "¿Qué es un stakeholder en gestión de proyectos?",
            "Persona o grupo con interés en el resultado del proyecto",
            ReviewCategory.GENERAL
        ),
        ReviewItemTemplate(
            "¿Qué significa 'Depende de' en relaciones entre tareas?",
            "Una tarea necesita que otra se complete primero",
            ReviewCategory.GENERAL
        ),
        ReviewItemTemplate(
            "¿Cuál es el propósito de la repetición espaciada?",
            "Optimizar la retención a largo plazo revisando en intervalos crecientes",
            ReviewCategory.GENERAL
        )
    )
}
