package com.mindforge.game.taskprioritizer.engine

import com.mindforge.game.taskprioritizer.model.EisenhowerQuadrant
import com.mindforge.game.taskprioritizer.model.TaskCard
import com.mindforge.game.taskprioritizer.model.TaskCategory
import kotlin.random.Random

/**
 * Generates realistic business tasks for the Task Prioritizer game
 */
class TaskGenerator {

    private val usedTaskIds = mutableSetOf<String>()

    /**
     * Generate a set of tasks ensuring variety across all quadrants
     */
    fun generateTasks(count: Int): List<TaskCard> {
        val tasks = mutableListOf<TaskCard>()
        val quadrantsNeeded = EisenhowerQuadrant.values().toList().shuffled()

        // Ensure at least one task per quadrant if count >= 4
        if (count >= 4) {
            quadrantsNeeded.forEach { quadrant ->
                tasks.add(generateTaskForQuadrant(quadrant))
            }
        }

        // Fill remaining slots with random tasks
        while (tasks.size < count) {
            val randomQuadrant = EisenhowerQuadrant.values().random()
            tasks.add(generateTaskForQuadrant(randomQuadrant))
        }

        return tasks.shuffled()
    }

    private fun generateTaskForQuadrant(quadrant: EisenhowerQuadrant): TaskCard {
        val (urgency, importance) = when (quadrant) {
            EisenhowerQuadrant.DO_FIRST -> Pair(Random.nextInt(4, 6), Random.nextInt(4, 6))
            EisenhowerQuadrant.SCHEDULE -> Pair(Random.nextInt(1, 3), Random.nextInt(4, 6))
            EisenhowerQuadrant.DELEGATE -> Pair(Random.nextInt(4, 6), Random.nextInt(1, 3))
            EisenhowerQuadrant.ELIMINATE -> Pair(Random.nextInt(1, 3), Random.nextInt(1, 3))
        }

        val template = taskTemplates[quadrant]?.random()
            ?: throw IllegalStateException("No templates for quadrant $quadrant")

        val id = generateUniqueId()

        return TaskCard(
            id = id,
            title = template.title,
            description = template.description,
            urgencyLevel = urgency,
            importanceLevel = importance,
            deadline = template.deadline,
            estimatedTime = template.estimatedTime,
            category = template.category
        )
    }

    private fun generateUniqueId(): String {
        var id: String
        do {
            id = "task_${Random.nextInt(10000, 99999)}"
        } while (usedTaskIds.contains(id))
        usedTaskIds.add(id)
        return id
    }

    fun reset() {
        usedTaskIds.clear()
    }

    // Task templates organized by quadrant
    private val taskTemplates = mapOf(
        EisenhowerQuadrant.DO_FIRST to listOf(
            TaskTemplate(
                "Preparar presentación para junta directiva",
                "La junta es mañana y necesita aprobación del presupuesto anual",
                "Mañana 9:00 AM",
                "4 horas",
                TaskCategory.PLANNING
            ),
            TaskTemplate(
                "Resolver incidencia crítica del sistema",
                "El sistema de pagos está caído afectando a 500 usuarios",
                "Hoy - URGENTE",
                "2 horas",
                TaskCategory.GENERAL
            ),
            TaskTemplate(
                "Completar informe trimestral para auditoría",
                "La auditoría externa comienza en 2 días",
                "Pasado mañana",
                "6 horas",
                TaskCategory.REPORT
            ),
            TaskTemplate(
                "Responder a queja formal de cliente VIP",
                "Cliente amenaza con cancelar contrato de $50K",
                "Hoy",
                "1 hora",
                TaskCategory.EMAIL
            ),
            TaskTemplate(
                "Revisar y firmar contrato urgente",
                "El proveedor necesita firma hoy para mantener condiciones",
                "Hoy 6:00 PM",
                "30 minutos",
                TaskCategory.REVIEW
            )
        ),

        EisenhowerQuadrant.SCHEDULE to listOf(
            TaskTemplate(
                "Desarrollar plan estratégico Q2",
                "Definir objetivos y KPIs para el próximo trimestre",
                "En 2 semanas",
                "8 horas",
                TaskCategory.PLANNING
            ),
            TaskTemplate(
                "Capacitación del equipo en nueva metodología",
                "Mejorar eficiencia del equipo a largo plazo",
                "Próximo mes",
                "4 horas",
                TaskCategory.MEETING
            ),
            TaskTemplate(
                "Análisis de mercado para nuevo producto",
                "Investigación para decisión de inversión importante",
                "En 3 semanas",
                "10 horas",
                TaskCategory.REPORT
            ),
            TaskTemplate(
                "Crear documentación técnica del sistema",
                "Necesaria para mantenimiento futuro y nuevos desarrolladores",
                "Próximas 2 semanas",
                "6 horas",
                TaskCategory.GENERAL
            ),
            TaskTemplate(
                "Planificar presupuesto departamental 2025",
                "Fundamental para recursos del próximo año",
                "Mes próximo",
                "5 horas",
                TaskCategory.PLANNING
            )
        ),

        EisenhowerQuadrant.DELEGATE to listOf(
            TaskTemplate(
                "Actualizar lista de contactos del CRM",
                "Mantenimiento rutinario pero vence hoy",
                "Hoy",
                "1 hora",
                TaskCategory.GENERAL
            ),
            TaskTemplate(
                "Responder correos de consultas generales",
                "20 emails acumulados de esta semana",
                "Hoy",
                "2 horas",
                TaskCategory.EMAIL
            ),
            TaskTemplate(
                "Confirmar asistencia a evento de networking",
                "El organizador necesita confirmación urgente",
                "Hoy",
                "15 minutos",
                TaskCategory.EMAIL
            ),
            TaskTemplate(
                "Revisar y aprobar solicitudes de vacaciones",
                "El equipo espera respuesta para planificar",
                "Esta semana",
                "30 minutos",
                TaskCategory.REVIEW
            ),
            TaskTemplate(
                "Actualizar calendario compartido del equipo",
                "Varios eventos pendientes de agregar",
                "Hoy",
                "20 minutos",
                TaskCategory.GENERAL
            )
        ),

        EisenhowerQuadrant.ELIMINATE to listOf(
            TaskTemplate(
                "Revisar feed de redes sociales corporativas",
                "Chequeo rutinario sin valor inmediato",
                null,
                "30 minutos",
                TaskCategory.GENERAL
            ),
            TaskTemplate(
                "Leer newsletter semanal de la industria",
                "Información general no urgente ni crítica",
                null,
                "15 minutos",
                TaskCategory.EMAIL
            ),
            TaskTemplate(
                "Organizar archivos personales en drive",
                "Limpieza opcional de documentos antiguos",
                null,
                "1 hora",
                TaskCategory.GENERAL
            ),
            TaskTemplate(
                "Actualizar firma de correo electrónico",
                "Cambio estético menor",
                null,
                "10 minutos",
                TaskCategory.EMAIL
            ),
            TaskTemplate(
                "Explorar nuevas apps de productividad",
                "Curiosidad sobre herramientas alternativas",
                null,
                "45 minutos",
                TaskCategory.GENERAL
            )
        )
    )

    private data class TaskTemplate(
        val title: String,
        val description: String,
        val deadline: String?,
        val estimatedTime: String,
        val category: TaskCategory
    )
}
