package com.mindforge.game.conceptlinker.engine

import com.mindforge.game.conceptlinker.model.*
import kotlin.random.Random

/**
 * Generates realistic concept scenarios for the Concept Linker game
 */
class ConceptGenerator {

    private val usedScenarios = mutableSetOf<Int>()
    private val colors = listOf(
        0xFF2196F3,  // Blue
        0xFF4CAF50,  // Green
        0xFFFF9800,  // Orange
        0xFF9C27B0,  // Purple
        0xFFE91E63,  // Pink
        0xFF00BCD4,  // Cyan
        0xFFFF5722,  // Deep Orange
        0xFF3F51B5,  // Indigo
        0xFF009688,  // Teal
        0xFFFFC107,  // Amber
        0xFF673AB7,  // Deep Purple
        0xFFCDDC39   // Lime
    )

    fun generateScenario(
        nodesCount: Int,
        connectionsCount: Int,
        includeStakeholders: Boolean,
        includeDeadlines: Boolean,
        includeResources: Boolean
    ): Pair<List<ConceptNode>, List<CorrectConnection>> {
        // Select a scenario template
        val template = selectUnusedScenario()

        // Generate nodes from template
        val selectedNodeTemplates = template.nodes.shuffled().take(nodesCount)
        val shuffledColors = colors.shuffled()

        val nodes = selectedNodeTemplates.mapIndexed { index, nodeTemplate ->
            ConceptNode(
                id = "node_$index",
                title = nodeTemplate.title,
                description = nodeTemplate.description,
                category = nodeTemplate.category,
                stakeholder = if (includeStakeholders) nodeTemplate.stakeholder else null,
                deadline = if (includeDeadlines) nodeTemplate.deadline else null,
                resources = if (includeResources) nodeTemplate.resources else emptyList(),
                color = shuffledColors[index % shuffledColors.size]
            )
        }

        // Generate correct connections based on template
        val correctConnections = mutableListOf<CorrectConnection>()
        val nodeIndicesMap = selectedNodeTemplates.withIndex().associate { it.value to it.index }

        template.connections.forEach { connectionTemplate ->
            val sourceTemplate = template.nodes.getOrNull(connectionTemplate.sourceIndex)
            val targetTemplate = template.nodes.getOrNull(connectionTemplate.targetIndex)

            if (sourceTemplate != null && targetTemplate != null) {
                val sourceIndex = nodeIndicesMap[sourceTemplate]
                val targetIndex = nodeIndicesMap[targetTemplate]

                if (sourceIndex != null && targetIndex != null &&
                    sourceIndex < nodes.size && targetIndex < nodes.size) {
                    correctConnections.add(
                        CorrectConnection(
                            sourceNodeId = nodes[sourceIndex].id,
                            targetNodeId = nodes[targetIndex].id,
                            validRelationTypes = connectionTemplate.validRelationTypes
                        )
                    )
                }
            }
        }

        return Pair(nodes, correctConnections.take(connectionsCount))
    }

    private fun selectUnusedScenario(): ScenarioTemplate {
        val availableScenarios = scenarioTemplates.indices.filter { !usedScenarios.contains(it) }

        val selectedIndex = if (availableScenarios.isNotEmpty()) {
            availableScenarios.random()
        } else {
            // Reset if all used
            usedScenarios.clear()
            scenarioTemplates.indices.random()
        }

        usedScenarios.add(selectedIndex)
        return scenarioTemplates[selectedIndex]
    }

    fun reset() {
        usedScenarios.clear()
    }

    private val scenarioTemplates = listOf(
        // Scenario 1: Software Development Project
        ScenarioTemplate(
            name = "Desarrollo de Plataforma E-commerce",
            description = "Proyecto de desarrollo de plataforma de comercio electrónico",
            nodes = listOf(
                NodeTemplate(
                    "Diseño de UI/UX",
                    "Crear wireframes y diseños visuales",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Equipo de Diseño",
                    deadline = "Semana 2",
                    resources = listOf("Diseñador Senior", "Figma")
                ),
                NodeTemplate(
                    "Desarrollo Frontend",
                    "Implementar interfaz en React",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Equipo de Frontend",
                    deadline = "Semana 5",
                    resources = listOf("Desarrollador React", "Figma")
                ),
                NodeTemplate(
                    "API de Productos",
                    "Desarrollar endpoints de gestión de productos",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Equipo de Backend",
                    deadline = "Semana 4",
                    resources = listOf("Desarrollador Backend", "Servidor Dev")
                ),
                NodeTemplate(
                    "Sistema de Pagos",
                    "Integrar pasarela de pagos",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Equipo de Backend",
                    deadline = "Semana 6",
                    resources = listOf("Desarrollador Backend", "Servidor Dev")
                ),
                NodeTemplate(
                    "Testing QA",
                    "Pruebas de funcionalidad y seguridad",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Equipo de QA",
                    deadline = "Semana 7",
                    resources = listOf("QA Tester", "Servidor Test")
                ),
                NodeTemplate(
                    "Deploy a Producción",
                    "Despliegue final en producción",
                    ConceptNode.NodeCategory.MILESTONE,
                    stakeholder = "DevOps",
                    deadline = "Semana 8",
                    resources = listOf("DevOps Engineer", "Servidor Prod")
                ),
                NodeTemplate(
                    "Servidor Dev",
                    "Servidor compartido para desarrollo",
                    ConceptNode.NodeCategory.RESOURCE,
                    resources = listOf("Servidor Dev")
                )
            ),
            connections = listOf(
                ConnectionTemplate(0, 1, listOf(RelationType.DEPENDS_ON)),  // UI depends on Design
                ConnectionTemplate(2, 1, listOf(RelationType.BLOCKS)),  // API blocks Frontend
                ConnectionTemplate(3, 2, listOf(RelationType.RELATED_TO)),  // Payments related to API
                ConnectionTemplate(1, 4, listOf(RelationType.DEPENDS_ON)),  // Testing depends on Frontend
                ConnectionTemplate(4, 5, listOf(RelationType.DEPENDS_ON)),  // Deploy depends on Testing
                ConnectionTemplate(2, 3, listOf(RelationType.SAME_STAKEHOLDER, RelationType.RESOURCE_CONFLICT)),  // Backend team
                ConnectionTemplate(2, 6, listOf(RelationType.RESOURCE_CONFLICT))  // API and Payments both need server
            )
        ),

        // Scenario 2: Marketing Campaign
        ScenarioTemplate(
            name = "Campaña de Lanzamiento de Producto",
            description = "Campaña integral de marketing para nuevo producto",
            nodes = listOf(
                NodeTemplate(
                    "Investigación de Mercado",
                    "Análisis de público objetivo y competencia",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Marketing",
                    deadline = "Semana 1",
                    resources = listOf("Analista de Marketing")
                ),
                NodeTemplate(
                    "Estrategia de Contenido",
                    "Definir mensajes clave y canales",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Marketing",
                    deadline = "Semana 2",
                    resources = listOf("Content Manager")
                ),
                NodeTemplate(
                    "Diseño Creativo",
                    "Crear assets visuales y videos",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Diseño",
                    deadline = "Semana 3",
                    resources = listOf("Diseñador Gráfico", "Estudio de Grabación")
                ),
                NodeTemplate(
                    "Campaña en Redes",
                    "Gestión de redes sociales y ads",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Marketing",
                    deadline = "Semana 4",
                    resources = listOf("Social Media Manager", "Presupuesto Ads")
                ),
                NodeTemplate(
                    "Email Marketing",
                    "Campaña de emails segmentada",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Marketing",
                    deadline = "Semana 4",
                    resources = listOf("Email Specialist", "Presupuesto Ads")
                ),
                NodeTemplate(
                    "Evento de Lanzamiento",
                    "Evento presencial de presentación",
                    ConceptNode.NodeCategory.MILESTONE,
                    stakeholder = "Eventos",
                    deadline = "Semana 5",
                    resources = listOf("Event Manager", "Presupuesto Evento")
                ),
                NodeTemplate(
                    "Medición de Resultados",
                    "Análisis de KPIs y ROI",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Marketing",
                    deadline = "Semana 6",
                    resources = listOf("Analista de Marketing")
                )
            ),
            connections = listOf(
                ConnectionTemplate(0, 1, listOf(RelationType.DEPENDS_ON)),  // Strategy depends on Research
                ConnectionTemplate(1, 2, listOf(RelationType.DEPENDS_ON)),  // Creative depends on Strategy
                ConnectionTemplate(2, 3, listOf(RelationType.DEPENDS_ON)),  // Social depends on Creative
                ConnectionTemplate(2, 4, listOf(RelationType.DEPENDS_ON)),  // Email depends on Creative
                ConnectionTemplate(3, 4, listOf(RelationType.SAME_DEADLINE, RelationType.RESOURCE_CONFLICT)),  // Same deadline and budget
                ConnectionTemplate(3, 5, listOf(RelationType.RELATED_TO)),  // Social related to Event
                ConnectionTemplate(5, 6, listOf(RelationType.DEPENDS_ON)),  // Measurement depends on Event
                ConnectionTemplate(0, 6, listOf(RelationType.SAME_STAKEHOLDER))  // Same stakeholder Marketing
            )
        ),

        // Scenario 3: Company Restructuring
        ScenarioTemplate(
            name = "Reestructuración Organizacional",
            description = "Proceso de reorganización departamental",
            nodes = listOf(
                NodeTemplate(
                    "Análisis Organizacional",
                    "Evaluar estructura actual y problemas",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "RRHH",
                    deadline = "Mes 1",
                    resources = listOf("Consultor Organizacional")
                ),
                NodeTemplate(
                    "Definir Nueva Estructura",
                    "Diseñar organigrama y roles",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Dirección",
                    deadline = "Mes 2",
                    resources = listOf("Consultor Organizacional")
                ),
                NodeTemplate(
                    "Comunicación Interna",
                    "Anunciar cambios al personal",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "RRHH",
                    deadline = "Mes 2",
                    resources = listOf("Comunicación Interna")
                ),
                NodeTemplate(
                    "Reasignación de Personal",
                    "Mover empleados a nuevos roles",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "RRHH",
                    deadline = "Mes 3",
                    resources = listOf("RRHH Team")
                ),
                NodeTemplate(
                    "Capacitación",
                    "Training en nuevos procesos",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "RRHH",
                    deadline = "Mes 3",
                    resources = listOf("Capacitador")
                ),
                NodeTemplate(
                    "Actualización de Sistemas",
                    "Modificar sistemas y permisos",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "IT",
                    deadline = "Mes 3",
                    resources = listOf("IT Team")
                ),
                NodeTemplate(
                    "Evaluación Post-cambio",
                    "Medir impacto y ajustar",
                    ConceptNode.NodeCategory.MILESTONE,
                    stakeholder = "Dirección",
                    deadline = "Mes 4",
                    resources = listOf("Consultor Organizacional")
                )
            ),
            connections = listOf(
                ConnectionTemplate(0, 1, listOf(RelationType.DEPENDS_ON)),  // Structure depends on Analysis
                ConnectionTemplate(1, 2, listOf(RelationType.DEPENDS_ON)),  // Communication depends on Structure
                ConnectionTemplate(2, 3, listOf(RelationType.DEPENDS_ON)),  // Reassignment depends on Communication
                ConnectionTemplate(3, 4, listOf(RelationType.SAME_DEADLINE, RelationType.RELATED_TO)),  // Training with reassignment
                ConnectionTemplate(3, 5, listOf(RelationType.SAME_DEADLINE, RelationType.RELATED_TO)),  // Systems with reassignment
                ConnectionTemplate(0, 2, listOf(RelationType.SAME_STAKEHOLDER)),  // RRHH stakeholder
                ConnectionTemplate(4, 6, listOf(RelationType.DEPENDS_ON))  // Evaluation depends on Training
            )
        ),

        // Scenario 4: Product Launch
        ScenarioTemplate(
            name = "Lanzamiento de Nuevo Servicio SaaS",
            description = "Preparación y lanzamiento de servicio cloud",
            nodes = listOf(
                NodeTemplate(
                    "Desarrollo MVP",
                    "Versión mínima viable del producto",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Desarrollo",
                    deadline = "Q1",
                    resources = listOf("Dev Team", "Cloud Credits")
                ),
                NodeTemplate(
                    "Beta Testing",
                    "Pruebas con usuarios seleccionados",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Product",
                    deadline = "Q2",
                    resources = listOf("Beta Testers")
                ),
                NodeTemplate(
                    "Estrategia de Precios",
                    "Definir modelos y tiers",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Product",
                    deadline = "Q2",
                    resources = listOf("Product Manager")
                ),
                NodeTemplate(
                    "Infraestructura Cloud",
                    "Configurar escalabilidad",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "DevOps",
                    deadline = "Q2",
                    resources = listOf("DevOps", "Cloud Credits")
                ),
                NodeTemplate(
                    "Documentación",
                    "Crear docs y tutoriales",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Product",
                    deadline = "Q2",
                    resources = listOf("Technical Writer")
                ),
                NodeTemplate(
                    "Plan de Marketing",
                    "Estrategia de go-to-market",
                    ConceptNode.NodeCategory.TASK,
                    stakeholder = "Marketing",
                    deadline = "Q2",
                    resources = listOf("Marketing Team")
                ),
                NodeTemplate(
                    "Lanzamiento Público",
                    "Release oficial del servicio",
                    ConceptNode.NodeCategory.MILESTONE,
                    stakeholder = "Dirección",
                    deadline = "Q3",
                    resources = listOf("Todos los equipos")
                )
            ),
            connections = listOf(
                ConnectionTemplate(0, 1, listOf(RelationType.DEPENDS_ON)),  // Beta depends on MVP
                ConnectionTemplate(1, 2, listOf(RelationType.RELATED_TO)),  // Pricing related to Beta feedback
                ConnectionTemplate(0, 3, listOf(RelationType.RESOURCE_CONFLICT)),  // Both need cloud credits
                ConnectionTemplate(1, 4, listOf(RelationType.RELATED_TO)),  // Docs from beta learning
                ConnectionTemplate(2, 3, listOf(RelationType.SAME_DEADLINE)),  // Same Q2 deadline
                ConnectionTemplate(4, 5, listOf(RelationType.RELATED_TO)),  // Marketing uses docs
                ConnectionTemplate(3, 6, listOf(RelationType.BLOCKS)),  // Infrastructure blocks launch
                ConnectionTemplate(5, 6, listOf(RelationType.BLOCKS))  // Marketing blocks launch
            )
        )
    )
}
