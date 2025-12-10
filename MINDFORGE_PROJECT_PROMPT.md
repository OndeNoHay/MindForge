# MindForge: Sistema de Entrenamiento Cognitivo Gamificado para Asistentes Ejecutivos

## Prompt de Proyecto para Claude Code

---

## 1. CONTEXTO Y ANÁLISIS DEL PROBLEMA

### 1.1 Perfil del Usuario
El usuario es un **asistente ejecutivo** que gestiona múltiples responsabilidades:
- Asistencia a reuniones con diferentes stakeholders
- Análisis de minutas y actas de reunión
- Extracción y registro de tareas asignadas en Microsoft Loop
- Transformación de tareas en acciones ejecutables
- Seguimiento y completado de acciones
- Reporting al directivo

### 1.2 Desafíos Cognitivos Identificados
| Función Cognitiva | Manifestación del Problema |
|-------------------|---------------------------|
| **Memoria de trabajo** | Olvido de tareas durante reuniones extensas |
| **Atención selectiva** | Dificultad para filtrar información relevante de minutas |
| **Función ejecutiva** | Problemas de priorización y secuenciación de tareas |
| **Retención a largo plazo** | Pérdida de contexto entre reuniones relacionadas |
| **Interconexión conceptual** | Dificultad para relacionar tareas de diferentes proyectos |

### 1.3 Base Científica
La aplicación se fundamenta en investigación validada:

- **Gamificación cognitiva**: Meta-análisis demuestran que la gamificación aumenta el engagement y puede mejorar la memoria de trabajo y funciones ejecutivas cuando se aplica correctamente (Vermeir et al., 2020 - JMIR Serious Games).

- **Repetición espaciada**: Técnica con evidencia robusta desde Ebbinghaus, que demuestra que revisar información a intervalos crecientes optimiza la retención a largo plazo (Kang, 2016 - Policy Insights from the Behavioral and Brain Sciences).

- **Entrenamiento de funciones ejecutivas**: Los estudios indican que el entrenamiento en memoria de trabajo y atención puede producir mejoras transferibles cuando se diseña adecuadamente (PMC Systematic Reviews, 2020).

---

## 2. ESPECIFICACIÓN DE LA APLICACIÓN: MINDFORGE

### 2.1 Visión del Producto
**MindForge** es una aplicación Android gamificada diseñada para fortalecer las capacidades cognitivas específicas requeridas en la gestión ejecutiva: memoria, priorización, retención de información e interconexión de ideas.

### 2.2 Principios de Diseño
1. **Modularidad**: Arquitectura basada en módulos independientes y desacoplados
2. **Escalabilidad**: Facilidad para añadir nuevos juegos/ejercicios sin modificar el núcleo
3. **Personalización**: Sistema adaptativo basado en rendimiento del usuario
4. **Gamificación científica**: Elementos de juego alineados con principios cognitivos validados
5. **Contextualización profesional**: Ejercicios con temática de entorno ejecutivo/empresarial

---

## 3. ARQUITECTURA TÉCNICA

### 3.1 Stack Tecnológico
```
Lenguaje:        Kotlin 1.9+
UI Framework:    Jetpack Compose (Material 3)
Arquitectura:    Clean Architecture + MVVM
DI:              Hilt (Dagger)
Base de datos:   Room
Async:           Kotlin Coroutines + Flow
Navegación:      Jetpack Navigation Compose
Testing:         JUnit5 + Mockk + Compose Testing
Build:           Gradle Kotlin DSL con Version Catalogs
Min SDK:         26 (Android 8.0)
Target SDK:      34 (Android 14)
```

### 3.2 Estructura Multi-Módulo
```
mindforge/
├── app/                          # Módulo de aplicación principal
│   ├── src/main/
│   │   ├── MindForgeApp.kt       # Application class + Hilt setup
│   │   └── MainActivity.kt       # Single Activity
│   └── build.gradle.kts
│
├── core/                         # Módulos core compartidos
│   ├── core-ui/                  # Componentes UI reutilizables
│   │   ├── theme/                # Material 3 Theme
│   │   ├── components/           # Buttons, Cards, etc.
│   │   └── animations/           # Animaciones compartidas
│   │
│   ├── core-domain/              # Lógica de negocio compartida
│   │   ├── model/                # Entidades de dominio
│   │   ├── repository/           # Interfaces de repositorio
│   │   └── usecase/              # Casos de uso base
│   │
│   ├── core-data/                # Implementación de datos
│   │   ├── local/                # Room DAOs y entidades
│   │   ├── repository/           # Implementaciones de repositorio
│   │   └── di/                   # Módulos Hilt de datos
│   │
│   └── core-common/              # Utilidades compartidas
│       ├── extensions/           # Extension functions
│       ├── utils/                # Helpers generales
│       └── constants/            # Constantes de la app
│
├── features/                     # Módulos de funcionalidades
│   ├── feature-home/             # Dashboard principal
│   ├── feature-profile/          # Perfil y estadísticas del usuario
│   ├── feature-progress/         # Seguimiento de progreso
│   └── feature-settings/         # Configuración
│
├── games/                        # MÓDULOS DE JUEGOS COGNITIVOS
│   ├── game-core/                # Framework base para juegos
│   │   ├── engine/               # Motor de juego base
│   │   ├── scoring/              # Sistema de puntuación
│   │   ├── difficulty/           # Sistema de dificultad adaptativa
│   │   └── gamification/         # XP, logros, rachas
│   │
│   ├── game-memory-matrix/       # Juego: Matriz de Memoria
│   ├── game-task-prioritizer/    # Juego: Priorizador de Tareas
│   ├── game-name-face/           # Juego: Nombres y Caras
│   ├── game-meeting-recall/      # Juego: Recuerdo de Reuniones
│   ├── game-concept-linker/      # Juego: Conexión de Conceptos
│   └── game-spaced-review/       # Sistema de Repetición Espaciada
│
├── navigation/                   # Módulo de navegación centralizada
│   └── NavigationGraph.kt        # Grafo de navegación
│
└── build-logic/                  # Plugins de Gradle compartidos
    └── convention/               # Convenciones de build
```

### 3.3 Diagrama de Dependencias
```
                    ┌─────────┐
                    │   app   │
                    └────┬────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
   ┌──────────┐   ┌──────────┐   ┌──────────┐
   │ features │   │  games   │   │navigation│
   └────┬─────┘   └────┬─────┘   └────┬─────┘
        │              │              │
        └──────────────┼──────────────┘
                       │
                       ▼
              ┌────────────────┐
              │   game-core    │
              └───────┬────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
   ┌─────────┐  ┌──────────┐  ┌─────────┐
   │ core-ui │  │core-domain│  │core-data│
   └────┬────┘  └─────┬────┘  └────┬────┘
        │             │            │
        └─────────────┼────────────┘
                      │
                      ▼
              ┌─────────────┐
              │ core-common │
              └─────────────┘
```

---

## 4. ESPECIFICACIÓN DE MÓDULOS DE JUEGOS

### 4.1 Framework Base de Juegos (game-core)

#### 4.1.1 Motor de Juego Base
```kotlin
// GameEngine.kt
interface GameEngine<State : GameState, Event : GameEvent, Result : GameResult> {
    val currentState: StateFlow<State>
    fun processEvent(event: Event)
    fun start()
    fun pause()
    fun resume()
    fun finish(): Result
}

// GameState.kt
interface GameState {
    val phase: GamePhase
    val score: Int
    val timeRemaining: Long?
    val difficulty: DifficultyLevel
}

enum class GamePhase {
    READY, PLAYING, PAUSED, COMPLETED, REVIEW
}
```

#### 4.1.2 Sistema de Dificultad Adaptativa
Basado en rendimiento del usuario, ajusta parámetros automáticamente:
```kotlin
// AdaptiveDifficultyManager.kt
class AdaptiveDifficultyManager {
    fun calculateNextDifficulty(
        currentLevel: DifficultyLevel,
        accuracy: Float,
        responseTime: Long,
        streak: Int
    ): DifficultyLevel
    
    // Algoritmo: Si accuracy > 80% y responseTime < threshold → subir
    //            Si accuracy < 60% o responseTime > 2x threshold → bajar
}
```

#### 4.1.3 Sistema de Gamificación
```kotlin
// GamificationSystem.kt
data class UserProgress(
    val totalXP: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val achievements: List<Achievement>,
    val dailyGoalProgress: Float
)

// Elementos de gamificación implementados:
// - Sistema de XP con niveles
// - Rachas diarias con multiplicadores
// - Logros desbloqueables
// - Progreso visual con feedback inmediato
// - Objetivos diarios personalizables
```

---

### 4.2 MÓDULO: Memory Matrix (game-memory-matrix)

**Objetivo cognitivo**: Memoria de trabajo visual y espacial

**Mecánica de juego**:
1. Se muestra una cuadrícula (ej: 4x4) con algunos cuadros iluminados
2. Los cuadros desaparecen tras N segundos
3. El usuario debe tocar los cuadros que estaban iluminados
4. Progresión: aumenta tamaño de matriz y cantidad de cuadros

**Contextualización ejecutiva**: 
- Los cuadros representan "asistentes a una reunión"
- El usuario debe recordar quién estaba presente

**Parámetros de dificultad**:
| Nivel | Tamaño Grid | Cuadros | Tiempo Visualización |
|-------|-------------|---------|---------------------|
| 1-3   | 3x3         | 3-4     | 3s                  |
| 4-6   | 4x4         | 4-6     | 2.5s                |
| 7-9   | 5x5         | 6-8     | 2s                  |
| 10+   | 6x6         | 8-10    | 1.5s                |

---

### 4.3 MÓDULO: Task Prioritizer (game-task-prioritizer)

**Objetivo cognitivo**: Funciones ejecutivas, toma de decisiones, priorización

**Mecánica de juego**:
1. Aparecen tarjetas con tareas ficticias (urgencia + importancia)
2. El usuario arrastra las tareas al cuadrante correcto (Matriz de Eisenhower)
3. Bonus por velocidad y precisión
4. Niveles avanzados: tareas con dependencias, deadlines conflictivos

**Contextualización ejecutiva**:
- Tareas reales de entorno empresarial
- Escenarios de priorización del día a día

**Estructura de tarea**:
```kotlin
data class TaskCard(
    val id: String,
    val title: String,           // Ej: "Preparar informe Q4"
    val description: String,
    val urgencyLevel: Int,       // 1-5
    val importanceLevel: Int,    // 1-5
    val deadline: String?,
    val dependencies: List<String>?,
    val correctQuadrant: EisenhowerQuadrant
)

enum class EisenhowerQuadrant {
    DO_FIRST,      // Urgente + Importante
    SCHEDULE,      // No urgente + Importante
    DELEGATE,      // Urgente + No importante
    ELIMINATE      // No urgente + No importante
}
```

---

### 4.4 MÓDULO: Name-Face Associator (game-name-face)

**Objetivo cognitivo**: Memoria asociativa, reconocimiento facial

**Mecánica de juego**:
1. Se presentan rostros generados/stock con nombres y roles
2. Fase de estudio: tiempo limitado para memorizar
3. Fase de test: se muestran rostros y se debe seleccionar nombre/rol correcto
4. Técnica mnemónica integrada: sugerencias de asociación visual

**Contextualización ejecutiva**:
- "Nuevos participantes en el comité de dirección"
- "Equipo del cliente X"

**Progresión**:
| Nivel | Personas | Datos por persona | Tiempo estudio |
|-------|----------|-------------------|----------------|
| 1-3   | 3        | Nombre            | 20s            |
| 4-6   | 5        | Nombre + Rol      | 25s            |
| 7-9   | 7        | Nombre + Rol + Dato| 30s           |
| 10+   | 10       | Completo          | 35s            |

---

### 4.5 MÓDULO: Meeting Recall (game-meeting-recall)

**Objetivo cognitivo**: Memoria episódica, retención de información compleja

**Mecánica de juego**:
1. Se presenta un "resumen de reunión" ficticio con:
   - Participantes
   - Temas tratados
   - Decisiones tomadas
   - Tareas asignadas (quién, qué, cuándo)
2. Fase de lectura con tiempo limitado
3. Preguntas sobre el contenido (múltiple opción o respuesta libre)
4. Bonus por recordar detalles secundarios

**Técnicas aplicadas**:
- Chunking: información agrupada lógicamente
- Elaborative encoding: conexiones entre elementos

**Ejemplo de contenido**:
```json
{
  "meetingTitle": "Revisión Sprint 23 - Proyecto Atlas",
  "date": "15/01/2025",
  "attendees": ["María García (PM)", "Juan López (Dev)", "Ana Ruiz (QA)"],
  "topics": [
    {
      "title": "Estado del desarrollo",
      "keyPoints": ["Completado módulo de autenticación", "Retraso en API de pagos"],
      "decisions": ["Priorizar API de pagos esta semana"]
    }
  ],
  "actionItems": [
    {"assignee": "Juan", "task": "Completar API de pagos", "deadline": "20/01"},
    {"assignee": "Ana", "task": "Preparar casos de prueba", "deadline": "18/01"}
  ]
}
```

---

### 4.6 MÓDULO: Concept Linker (game-concept-linker)

**Objetivo cognitivo**: Pensamiento relacional, conexión de ideas, visión sistémica

**Mecánica de juego**:
1. Se presentan nodos con conceptos/proyectos/stakeholders
2. El usuario debe establecer conexiones válidas entre ellos
3. Cada conexión requiere justificación (selección de tipo de relación)
4. Scoring basado en conexiones correctas y creatividad

**Tipos de relaciones**:
- Depende de
- Bloquea a
- Relacionado con
- Mismo stakeholder
- Mismo deadline
- Conflicto de recursos

**Representación visual**: Grafo interactivo con nodos arrastrables

---

### 4.7 MÓDULO: Spaced Review System (game-spaced-review)

**Objetivo cognitivo**: Retención a largo plazo mediante repetición espaciada

**Implementación**: Algoritmo basado en SM-2 (SuperMemo)

```kotlin
// SpacedRepetitionEngine.kt
data class ReviewItem(
    val id: String,
    val content: String,        // Pregunta o información
    val answer: String,
    val easinessFactor: Float,  // Inicial: 2.5
    val interval: Int,          // Días hasta próxima revisión
    val repetitions: Int,
    val nextReviewDate: LocalDate
)

fun calculateNextReview(
    item: ReviewItem,
    quality: Int  // 0-5, respuesta del usuario
): ReviewItem {
    // Implementación SM-2:
    // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
    // Si q >= 3: interval crece, sino: reset
}
```

**Integración con otros módulos**:
- Las tarjetas de revisión se generan automáticamente desde:
  - Errores en otros juegos
  - Información de Meeting Recall
  - Asociaciones Name-Face fallidas

---

## 5. INTERFAZ DE USUARIO

### 5.1 Pantallas Principales

#### Home Dashboard
```
┌────────────────────────────────────┐
│  MindForge                    ⚙️   │
├────────────────────────────────────┤
│  ┌─────────────────────────────┐   │
│  │ Racha: 🔥 7 días            │   │
│  │ Nivel: 12  ████████░░ 2450XP│   │
│  │ Objetivo diario: ████░░ 70% │   │
│  └─────────────────────────────┘   │
│                                    │
│  📚 Revisión pendiente: 12 items  │
│  [Comenzar Revisión]              │
│                                    │
│  🎮 Entrenamiento Diario          │
│  ┌──────┐ ┌──────┐ ┌──────┐      │
│  │Memory│ │Tasks │ │Names │      │
│  │Matrix│ │Prior.│ │Faces │      │
│  └──────┘ └──────┘ └──────┘      │
│  ┌──────┐ ┌──────┐               │
│  │Meet. │ │Concept│               │
│  │Recall│ │Linker │               │
│  └──────┘ └──────┘               │
│                                    │
│  📊 Ver estadísticas detalladas   │
└────────────────────────────────────┘
```

### 5.2 Diseño Visual
- **Tema**: Material 3 con Dynamic Color
- **Paleta base**: Azul profesional (#1565C0) con acentos (#4CAF50 para éxito)
- **Tipografía**: Google Sans / Roboto
- **Animaciones**: Lottie para feedback de logros
- **Modo oscuro**: Soporte completo

---

## 6. MODELO DE DATOS

### 6.1 Entidades de Room

```kotlin
// User.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val totalXP: Int,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long,
    val dailyGoal: Int,          // Minutos de entrenamiento
    val preferredDifficulty: String
)

// GameSession.kt
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val id: String,
    val gameType: String,        // memory_matrix, task_prioritizer, etc.
    val startTime: Long,
    val endTime: Long,
    val score: Int,
    val accuracy: Float,
    val difficultyLevel: Int,
    val xpEarned: Int
)

// Achievement.kt
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val unlockedAt: Long?,
    val progress: Float          // 0.0 - 1.0
)

// SpacedRepetitionItem.kt
@Entity(tableName = "spaced_items")
data class SpacedItemEntity(
    @PrimaryKey val id: String,
    val content: String,
    val answer: String,
    val sourceGame: String,
    val easinessFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewDate: Long,
    val createdAt: Long
)
```

---

## 7. INSTRUCCIONES PARA CLAUDE CODE

### 7.1 Fase 1: Setup del Proyecto (Semana 1)
```
1. Crear proyecto Android con estructura multi-módulo descrita
2. Configurar build-logic con convention plugins para:
   - android-library
   - android-application
   - compose
   - hilt
3. Implementar core-common y core-ui con:
   - Tema Material 3 completo
   - Componentes base (MfButton, MfCard, MfTopBar)
4. Configurar Room en core-data con entidades base
5. Setup de navegación con Navigation Compose
```

### 7.2 Fase 2: Framework de Juegos (Semana 2)
```
1. Implementar game-core completo:
   - GameEngine interface y BaseGameViewModel
   - Sistema de puntuación
   - Dificultad adaptativa
   - GamificationSystem con XP y logros
2. Crear tests unitarios para cada componente
```

### 7.3 Fase 3: Primer Juego - Memory Matrix (Semana 3)
```
1. Crear módulo game-memory-matrix
2. Implementar:
   - UI con Canvas/Compose para grid
   - Lógica de generación de patrones
   - Sistema de niveles con parámetros de tabla 4.2
   - Animaciones de feedback
3. Integrar con sistema de XP
4. Tests de UI con Compose Testing
```

### 7.4 Fase 4: Task Prioritizer (Semana 4)
```
1. Crear módulo game-task-prioritizer
2. Implementar:
   - Drag & drop con Compose
   - Cuadrantes de Eisenhower como drop zones
   - Generador de tareas realistas
   - Sistema de scoring con bonus por velocidad
3. Integrar errores con sistema de repetición espaciada
```

### 7.5 Fase 5: Name-Face Associator (Semana 5)
```
1. Crear módulo game-name-face
2. Implementar:
   - UI de tarjetas con imágenes
   - Fases de estudio y test
   - Generador de datos (nombres + roles + datos extra)
   - Sugerencias mnemónicas opcionales
3. Imágenes: usar randomuser.me API o recursos locales
```

### 7.6 Fase 6: Meeting Recall (Semana 6)
```
1. Crear módulo game-meeting-recall
2. Implementar:
   - Parser de contenido de reunión
   - UI de lectura con timer
   - Generador de preguntas variadas
   - Sistema de puntuación ponderado por dificultad
3. Crear banco de reuniones ficticias realistas
```

### 7.7 Fase 7: Concept Linker (Semana 7)
```
1. Crear módulo game-concept-linker
2. Implementar:
   - Visualización de grafo interactivo
   - Sistema de nodos y aristas arrastrables
   - Validación de conexiones
   - Generador de escenarios
```

### 7.8 Fase 8: Spaced Review System (Semana 8)
```
1. Crear módulo game-spaced-review
2. Implementar:
   - Algoritmo SM-2 completo
   - Integración con WorkManager para notificaciones
   - UI de tarjetas de repaso
   - Estadísticas de retención
3. Conectar con todos los módulos de juegos para alimentar items
```

### 7.9 Fase 9: Features Complementarias (Semanas 9-10)
```
1. feature-home: Dashboard con widgets de progreso
2. feature-profile: Estadísticas detalladas y gráficos
3. feature-progress: Historial y tendencias
4. feature-settings: Configuración de objetivos y notificaciones
```

### 7.10 Fase 10: Polish y Testing (Semanas 11-12)
```
1. Tests de integración end-to-end
2. Optimización de rendimiento
3. Accesibilidad (TalkBack, tamaños de texto)
4. Revisión de UX con feedback
5. Preparación para release (ProGuard, signing)
```

---

## 8. CRITERIOS DE ÉXITO

### 8.1 Métricas Técnicas
- [ ] Cobertura de tests > 70%
- [ ] Build time incremental < 30 segundos
- [ ] Crash-free rate > 99%
- [ ] Tiempo de inicio < 2 segundos

### 8.2 Métricas de Usuario
- [ ] Retención día 7 > 40%
- [ ] Sesiones promedio > 5 minutos
- [ ] Progresión de dificultad equilibrada

---

## 9. REFERENCIAS Y RECURSOS

### 9.1 Fundamentos Científicos
1. Vermeir, J.F. et al. (2020). "The Effects of Gamification on Computerized Cognitive Training: Systematic Review and Meta-Analysis". *JMIR Serious Games*. PMC7445616
2. Lumsden, J. et al. (2016). "Gamification of Cognitive Assessment and Cognitive Training: A Systematic Review of Applications and Efficacy". *JMIR Serious Games*. PMC4967181
3. Khaleghi, A. et al. (2021). "A Gamification Framework for Cognitive Assessment and Cognitive Training: Qualitative Study". *JMIR Serious Games*.
4. Kang, S.H.K. (2016). "Spaced Repetition Promotes Efficient and Effective Learning". *Policy Insights from the Behavioral and Brain Sciences*.
5. Smolen, P. et al. (2016). "The right time to learn: mechanisms and optimization of spaced learning". *Nature Reviews Neuroscience*. PMC5126970

### 9.2 Arquitectura Android
6. Android Developers. "Jetpack Compose architectural layering". developer.android.com
7. Android Developers. "Guide to app architecture". developer.android.com
8. Android Developers. "Compose UI Architecture". developer.android.com

### 9.3 Proyectos de Referencia
9. Now in Android (Google) - https://github.com/android/nowinandroid
10. JetRorty - Modular Clean Architecture - https://github.com/nicholasgot/rorty-android

---

## 10. NOTAS ADICIONALES PARA CLAUDE CODE

### Prioridades de Implementación
1. **Funcionalidad sobre estética**: Primero que funcione, luego que sea bonito
2. **Tests desde el inicio**: No acumular deuda técnica
3. **Commits atómicos**: Un cambio lógico por commit
4. **Documentación inline**: KDoc en clases e interfaces públicas

### Patrones a Seguir
- ViewModels exponen `StateFlow<UiState>` 
- Eventos de UI como `sealed class`
- Repositorios retornan `Flow` para datos reactivos
- Use cases para lógica de negocio compleja

### Evitar
- God classes o ViewModels con demasiada responsabilidad
- Dependencias circulares entre módulos
- Lógica de UI en ViewModels
- Hardcoded strings (usar resources)

---

**Fecha de creación**: Diciembre 2024  
**Versión del documento**: 1.0  
**Autor**: Generado con asistencia de Claude (Anthropic)
