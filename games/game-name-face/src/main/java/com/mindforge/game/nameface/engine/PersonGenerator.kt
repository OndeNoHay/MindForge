package com.mindforge.game.nameface.engine

import com.mindforge.game.nameface.model.PersonCard
import kotlin.random.Random

/**
 * Generates realistic person data for the Name-Face game
 */
class PersonGenerator {

    private val usedCombinations = mutableSetOf<String>()
    private val avatarColors = listOf(
        0xFFE91E63, // Pink
        0xFF9C27B0, // Purple
        0xFF673AB7, // Deep Purple
        0xFF3F51B5, // Indigo
        0xFF2196F3, // Blue
        0xFF00BCD4, // Cyan
        0xFF009688, // Teal
        0xFF4CAF50, // Green
        0xFFFF9800, // Orange
        0xFFFF5722, // Deep Orange
        0xFF795548, // Brown
        0xFF607D8B  // Blue Grey
    )

    fun generatePeople(count: Int, includeRole: Boolean, includeCompany: Boolean, includeDepartment: Boolean): List<PersonCard> {
        usedCombinations.clear()
        val people = mutableListOf<PersonCard>()

        val shuffledFirstNames = firstNames.shuffled()
        val shuffledLastNames = lastNames.shuffled()
        val shuffledRoles = roles.shuffled()
        val shuffledCompanies = companies.shuffled()
        val shuffledDepartments = departments.shuffled()
        val shuffledColors = avatarColors.shuffled()

        for (i in 0 until count) {
            val firstName = shuffledFirstNames[i % shuffledFirstNames.size]
            val lastName = shuffledLastNames[i % shuffledLastNames.size]
            val key = "$firstName-$lastName"

            // Ensure unique combinations
            var attempts = 0
            var uniqueKey = key
            while (usedCombinations.contains(uniqueKey) && attempts < 100) {
                val altLastName = shuffledLastNames[(i + attempts + 1) % shuffledLastNames.size]
                uniqueKey = "$firstName-$altLastName"
                attempts++
            }

            usedCombinations.add(uniqueKey)
            val parts = uniqueKey.split("-")

            people.add(
                PersonCard(
                    id = "person_${Random.nextInt(10000, 99999)}",
                    firstName = parts[0],
                    lastName = parts[1],
                    role = if (includeRole) shuffledRoles[i % shuffledRoles.size] else null,
                    company = if (includeCompany) shuffledCompanies[i % shuffledCompanies.size] else null,
                    department = if (includeDepartment) shuffledDepartments[i % shuffledDepartments.size] else null,
                    avatarColor = shuffledColors[i % shuffledColors.size]
                )
            )
        }

        return people
    }

    fun reset() {
        usedCombinations.clear()
    }

    // Spanish first names
    private val firstNames = listOf(
        "Carlos", "María", "José", "Ana", "Luis", "Carmen", "Antonio", "Isabel",
        "Francisco", "Dolores", "Manuel", "Pilar", "David", "Teresa", "Javier",
        "Rosa", "Daniel", "Francisca", "Rafael", "Antonia", "Miguel", "Josefa",
        "Pedro", "Lucía", "Alejandro", "Mercedes", "Fernando", "Elena", "Sergio",
        "Cristina", "Pablo", "Eva", "Jorge", "Marta", "Andrés", "Laura", "Alberto",
        "Patricia", "Juan", "Beatriz", "Diego", "Silvia", "Ángel", "Natalia",
        "Víctor", "Raquel", "Roberto", "Alicia", "Raúl", "Sandra"
    )

    // Spanish last names
    private val lastNames = listOf(
        "García", "Rodríguez", "González", "Fernández", "López", "Martínez",
        "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández",
        "Díaz", "Moreno", "Álvarez", "Muñoz", "Romero", "Alonso", "Gutiérrez",
        "Navarro", "Torres", "Domínguez", "Vázquez", "Ramos", "Gil", "Ramírez",
        "Serrano", "Blanco", "Suárez", "Molina", "Castro", "Ortega", "Rubio",
        "Marín", "Sanz", "Iglesias", "Núñez", "Medina", "Garrido"
    )

    // Business roles
    private val roles = listOf(
        "Director General", "CFO", "CTO", "Director de RRHH", "Gerente de Proyectos",
        "Analista Senior", "Coordinador", "Jefe de Equipo", "Especialista",
        "Consultor", "Director Comercial", "Gerente de Ventas", "Product Manager",
        "Scrum Master", "Arquitecto de Software", "Director de Marketing",
        "Gerente de Operaciones", "Controller Financiero", "Director de TI",
        "Gerente de Calidad", "Responsable de Compras", "Director de Innovación",
        "Gerente de Logística", "Director de Producción", "Analista de Datos",
        "Director de Estrategia", "Gerente de Clientes", "Director Jurídico",
        "Gerente de Comunicación", "Director de Seguridad"
    )

    // Company names
    private val companies = listOf(
        "TechCorp", "InnovaGroup", "GlobalSolutions", "DataSystems", "FutureVision",
        "SmartBusiness", "ProActive", "Excellence Inc", "Momentum", "Synergy Partners",
        "NextGen Solutions", "PrimeTech", "AlphaConsulting", "Omega Industries",
        "Vertex Group", "Horizon Corp", "Catalyst Solutions", "Pinnacle Systems",
        "Summit Enterprises", "Nexus Technologies"
    )

    // Departments
    private val departments = listOf(
        "Tecnología", "Finanzas", "Recursos Humanos", "Marketing", "Ventas",
        "Operaciones", "Producción", "Calidad", "Logística", "Legal",
        "Compras", "I+D", "Estrategia", "Comunicación", "Atención al Cliente",
        "Administración", "Seguridad", "Desarrollo", "Proyectos", "Consultoría"
    )
}
