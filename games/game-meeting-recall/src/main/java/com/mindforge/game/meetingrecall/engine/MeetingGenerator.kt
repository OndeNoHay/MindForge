package com.mindforge.game.meetingrecall.engine

import com.mindforge.game.meetingrecall.model.*
import kotlin.random.Random

/**
 * Generates realistic meeting summaries for the Meeting Recall game
 */
class MeetingGenerator {

    private val usedMeetingIds = mutableSetOf<String>()

    fun generateMeeting(
        attendeesCount: Int,
        topicsCount: Int,
        actionItemsCount: Int,
        includeLocation: Boolean,
        includeDuration: Boolean
    ): Meeting {
        val id = generateUniqueId()

        // Select random meeting template
        val template = meetingTemplates.random()

        // Generate attendees
        val selectedNames = names.shuffled().take(attendeesCount)
        val selectedRoles = roles.shuffled().take(attendeesCount)
        val attendees = selectedNames.zip(selectedRoles).map { (name, role) ->
            Attendee(name, role)
        }

        // Generate topics
        val selectedTopics = template.possibleTopics.shuffled().take(topicsCount)
        val topics = selectedTopics.map { topicTemplate ->
            Topic(
                title = topicTemplate.title,
                keyPoints = topicTemplate.keyPoints.shuffled().take(Random.nextInt(2, 4)),
                decisions = topicTemplate.decisions.shuffled().take(Random.nextInt(1, 3))
            )
        }

        // Generate action items
        val actionItems = generateActionItems(attendees, actionItemsCount)

        // Generate date
        val date = generateDate()

        return Meeting(
            id = id,
            title = template.title,
            date = date,
            attendees = attendees,
            topics = topics,
            actionItems = actionItems,
            duration = if (includeDuration) durations.random() else null,
            location = if (includeLocation) locations.random() else null
        )
    }

    private fun generateActionItems(attendees: List<Attendee>, count: Int): List<ActionItem> {
        val items = mutableListOf<ActionItem>()
        val shuffledAttendees = attendees.shuffled()
        val shuffledTasks = tasks.shuffled()

        for (i in 0 until minOf(count, shuffledTasks.size)) {
            val assignee = shuffledAttendees[i % shuffledAttendees.size].name
            val task = shuffledTasks[i]
            val deadline = deadlines.random()
            val priority = ActionItem.Priority.values().random()

            items.add(
                ActionItem(
                    assignee = assignee,
                    task = task,
                    deadline = deadline,
                    priority = priority
                )
            )
        }

        return items
    }

    private fun generateUniqueId(): String {
        var id: String
        do {
            id = "meeting_${Random.nextInt(10000, 99999)}"
        } while (usedMeetingIds.contains(id))
        usedMeetingIds.add(id)
        return id
    }

    private fun generateDate(): String {
        val day = Random.nextInt(1, 29)
        val month = Random.nextInt(1, 13)
        val year = 2025
        return "%02d/%02d/%d".format(day, month, year)
    }

    fun reset() {
        usedMeetingIds.clear()
    }

    // Data repositories
    private val names = listOf(
        "María García", "Carlos López", "Ana Martínez", "Juan Rodríguez",
        "Laura Sánchez", "Pedro González", "Carmen Fernández", "Miguel Pérez",
        "Isabel Gómez", "Antonio Martín", "Elena Jiménez", "Francisco Ruiz",
        "Rosa Hernández", "José Díaz", "Lucía Moreno", "David Álvarez",
        "Patricia Muñoz", "Javier Romero", "Sara Alonso", "Manuel Gutiérrez"
    )

    private val roles = listOf(
        "Director General", "CFO", "CTO", "Director de RRHH", "Gerente de Proyectos",
        "Analista Senior", "Coordinador", "Jefe de Equipo", "Especialista",
        "Product Manager", "Scrum Master", "Arquitecto de Software", "Director de Marketing",
        "Gerente de Operaciones", "Controller Financiero", "Director de TI",
        "Gerente de Calidad", "Director de Innovación", "Gerente de Ventas", "Director Comercial"
    )

    private val locations = listOf(
        "Sala de Juntas - Planta 3",
        "Sala Ejecutiva",
        "Sala de Conferencias A",
        "Sala Virtual - Teams",
        "Oficina Principal",
        "Sala de Reuniones B",
        "Centro de Innovación",
        "Auditorio"
    )

    private val durations = listOf(
        "30 minutos",
        "45 minutos",
        "1 hora",
        "1.5 horas",
        "2 horas"
    )

    private val deadlines = listOf(
        "Esta semana",
        "Próxima semana",
        "15 días",
        "Fin de mes",
        "Próximo mes",
        "En 2 semanas",
        "En 3 semanas"
    )

    private val tasks = listOf(
        "Completar análisis de requisitos",
        "Preparar presentación para el cliente",
        "Revisar presupuesto del proyecto",
        "Actualizar documentación técnica",
        "Coordinar con el equipo de desarrollo",
        "Enviar informe de progreso",
        "Programar reunión de seguimiento",
        "Implementar mejoras sugeridas",
        "Realizar pruebas de calidad",
        "Contactar con proveedores",
        "Revisar propuesta comercial",
        "Actualizar cronograma del proyecto",
        "Preparar casos de prueba",
        "Validar entregables con stakeholders",
        "Documentar lecciones aprendidas",
        "Configurar entorno de producción",
        "Realizar capacitación al equipo",
        "Analizar métricas de rendimiento",
        "Revisar contratos pendientes",
        "Organizar workshop técnico"
    )

    private val meetingTemplates = listOf(
        MeetingTemplate(
            title = "Revisión Sprint 23 - Proyecto Atlas",
            possibleTopics = listOf(
                TopicTemplate(
                    title = "Estado del Desarrollo",
                    keyPoints = listOf(
                        "Completado módulo de autenticación",
                        "Retraso en API de pagos",
                        "Testing en progreso para módulo de reportes",
                        "Integración con sistema legacy completada"
                    ),
                    decisions = listOf(
                        "Priorizar API de pagos esta semana",
                        "Asignar recursos adicionales al testing",
                        "Posponer feature de notificaciones al próximo sprint"
                    )
                ),
                TopicTemplate(
                    title = "Bloqueos y Riesgos",
                    keyPoints = listOf(
                        "Dependencia externa con proveedor de SMS",
                        "Ambiente de staging inestable",
                        "Falta claridad en requisitos de seguridad",
                        "Recursos limitados en equipo de QA"
                    ),
                    decisions = listOf(
                        "Escalar tema de proveedor de SMS con gerencia",
                        "Programar sesión de clarificación de requisitos",
                        "Solicitar apoyo temporal al equipo de QA del proyecto Beta"
                    )
                ),
                TopicTemplate(
                    title = "Planificación Próximo Sprint",
                    keyPoints = listOf(
                        "Prioridades del cliente para Q1",
                        "Capacidad del equipo reducida por vacaciones",
                        "Necesidad de refactorización en módulo principal"
                    ),
                    decisions = listOf(
                        "Incluir 3 historias de usuario prioritarias",
                        "Asignar 2 días para refactorización técnica",
                        "Ajustar estimaciones por disponibilidad reducida"
                    )
                )
            )
        ),
        MeetingTemplate(
            title = "Comité de Dirección - Enero 2025",
            possibleTopics = listOf(
                TopicTemplate(
                    title = "Resultados Financieros Q4 2024",
                    keyPoints = listOf(
                        "Ingresos 15% por encima del objetivo",
                        "Reducción de costos operativos en 8%",
                        "Margen de beneficio mejoró 3 puntos porcentuales",
                        "Inversión en I+D aumentó según lo planificado"
                    ),
                    decisions = listOf(
                        "Aprobar bonus trimestral para el equipo",
                        "Reinvertir 30% de beneficios en nuevos proyectos",
                        "Mantener estrategia de reducción de costos"
                    )
                ),
                TopicTemplate(
                    title = "Estrategia 2025",
                    keyPoints = listOf(
                        "Expansión a 2 nuevos mercados",
                        "Lanzamiento de 3 productos principales",
                        "Contratación de 25 personas clave",
                        "Transformación digital de procesos internos"
                    ),
                    decisions = listOf(
                        "Aprobar presupuesto de expansión",
                        "Iniciar proceso de contratación en febrero",
                        "Crear comité de transformación digital"
                    )
                ),
                TopicTemplate(
                    title = "Recursos Humanos",
                    keyPoints = listOf(
                        "Tasa de retención mejoró al 92%",
                        "Plan de capacitación ejecutado al 85%",
                        "Nueva política de trabajo híbrido bien recibida",
                        "Necesidad de fortalecer liderazgo intermedio"
                    ),
                    decisions = listOf(
                        "Implementar programa de desarrollo de líderes",
                        "Aumentar presupuesto de capacitación en 20%",
                        "Evaluar política de trabajo híbrido en marzo"
                    )
                )
            )
        ),
        MeetingTemplate(
            title = "Reunión de Cliente - Proyecto Phoenix",
            possibleTopics = listOf(
                TopicTemplate(
                    title = "Demo de Funcionalidades",
                    keyPoints = listOf(
                        "Presentación del dashboard ejecutivo",
                        "Módulo de reportes personalizables implementado",
                        "Integración con CRM funcionando correctamente",
                        "Mejoras en UX según feedback anterior"
                    ),
                    decisions = listOf(
                        "Cliente aprueba funcionalidades presentadas",
                        "Solicita ajustes menores en colores del dashboard",
                        "Pide agregar exportación a PDF en reportes"
                    )
                ),
                TopicTemplate(
                    title = "Planificación Fase 2",
                    keyPoints = listOf(
                        "Módulo de analytics avanzados",
                        "App móvil nativa para ejecutivos",
                        "Integración con sistema de facturación",
                        "Timeline de 4 meses para completar fase 2"
                    ),
                    decisions = listOf(
                        "Aprobar presupuesto adicional para app móvil",
                        "Priorizar integración con facturación",
                        "Programar reuniones quincenales de seguimiento"
                    )
                ),
                TopicTemplate(
                    title = "Soporte y Mantenimiento",
                    keyPoints = listOf(
                        "Acuerdo de nivel de servicio (SLA) propuesto",
                        "Horarios de soporte: 8am-8pm",
                        "Tiempo de respuesta: 4 horas para críticos",
                        "Sistema de tickets para gestión de incidencias"
                    ),
                    decisions = listOf(
                        "Cliente acepta SLA propuesto",
                        "Solicita incluir soporte en inglés",
                        "Pide training para equipo interno"
                    )
                )
            )
        ),
        MeetingTemplate(
            title = "Revisión de Arquitectura - Sistema Core",
            possibleTopics = listOf(
                TopicTemplate(
                    title = "Evaluación de Performance",
                    keyPoints = listOf(
                        "Tiempos de respuesta aumentaron 40% último mes",
                        "Base de datos mostrando signos de saturación",
                        "Caché actual no está optimizado",
                        "Crecimiento de usuarios superó proyecciones"
                    ),
                    decisions = listOf(
                        "Implementar sharding en base de datos",
                        "Migrar a Redis para caché distribuido",
                        "Realizar load testing exhaustivo"
                    )
                ),
                TopicTemplate(
                    title = "Deuda Técnica",
                    keyPoints = listOf(
                        "Módulos legacy sin documentación",
                        "Dependencias desactualizadas en 3 servicios",
                        "Testing coverage bajo 60% en componentes críticos",
                        "Necesidad de refactorización en capa de datos"
                    ),
                    decisions = listOf(
                        "Asignar 20% del tiempo a reducir deuda técnica",
                        "Actualizar dependencias de forma incremental",
                        "Objetivo de 80% coverage para fin de trimestre"
                    )
                ),
                TopicTemplate(
                    title = "Seguridad",
                    keyPoints = listOf(
                        "Auditoría reveló 3 vulnerabilidades menores",
                        "Certificados SSL próximos a vencer",
                        "Implementar autenticación de dos factores",
                        "Logs de seguridad necesitan mejor monitoreo"
                    ),
                    decisions = listOf(
                        "Corregir vulnerabilidades esta semana",
                        "Automatizar renovación de certificados",
                        "Implementar 2FA para usuarios admin en febrero"
                    )
                )
            )
        )
    )

    private data class MeetingTemplate(
        val title: String,
        val possibleTopics: List<TopicTemplate>
    )

    private data class TopicTemplate(
        val title: String,
        val keyPoints: List<String>,
        val decisions: List<String>
    )
}
