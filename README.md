# MindForge 🧠

Sistema de Entrenamiento Cognitivo Gamificado para Asistentes Ejecutivos

## 📱 Estado del Proyecto

### ✅ Completado
- **Fase 1**: Setup completo del proyecto multi-módulo
- **Fase 2**: Framework de Juegos (game-core) completo

### 🚧 En Desarrollo
- **Fase 3**: Primer Juego - Memory Matrix (próximo)

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin 1.9.21
- **UI**: Jetpack Compose + Material 3
- **Arquitectura**: Clean Architecture + MVVM
- **DI**: Hilt (Dagger)
- **Database**: Room
- **Async**: Kotlin Coroutines + Flow
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## 🏗️ Arquitectura Multi-Módulo

```
mindforge/
├── app/                     # Aplicación principal
├── core/
│   ├── core-common/         # Utilidades compartidas
│   ├── core-ui/             # Tema Material 3 y componentes
│   ├── core-domain/         # Modelos de dominio y lógica
│   └── core-data/           # Persistencia con Room
├── games/
│   └── game-core/           # Framework base para juegos
├── features/                # Módulos de funcionalidades
└── navigation/              # Sistema de navegación
```

## 🚀 Cómo Ejecutar la App

### Prerrequisitos

1. **Android Studio** Hedgehog (2023.1.1) o superior
2. **JDK 17** o superior
3. **Android SDK** con:
   - Compile SDK: 34
   - Min SDK: 26

### Opción 1: Abrir en Android Studio

1. Clona el repositorio:
   ```bash
   git clone https://github.com/OndeNoHay/MindForge.git
   cd MindForge
   ```

2. Abre Android Studio

3. Selecciona: **File > Open**

4. Navega a la carpeta `MindForge` y ábrela

5. Espera a que Android Studio sincronice el proyecto (puede tardar unos minutos la primera vez)

6. Conecta un dispositivo Android o inicia un emulador

7. Haz clic en el botón **Run** ▶️ (o presiona Shift+F10)

### Opción 2: Compilar desde Terminal

```bash
# En la raíz del proyecto
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# O combinar ambos
./gradlew assembleDebug installDebug
```

### Opción 3: Emulador de Android Studio

1. En Android Studio, abre **Device Manager** (icono de teléfono)

2. Crea un nuevo dispositivo virtual:
   - Device: Pixel 6 (recomendado)
   - System Image: Android 14.0 (API 34) o superior
   - RAM: 4GB mínimo

3. Inicia el emulador

4. Ejecuta la app con **Run** ▶️

## 📦 Características Implementadas

### Framework de Juegos (game-core)

✅ **Motor de Juegos**
- GameEngine genérico con estados, eventos y resultados
- BaseGameViewModel para integración con Compose
- Sistema de fases: READY, PLAYING, PAUSED, COMPLETED, REVIEW

✅ **Sistema de Puntuación**
- Puntuación base con bonificaciones
- Time bonus (respuestas rápidas)
- Streak bonus (rachas)
- Difficulty bonus (10 niveles)
- Cálculo de XP de sesión

✅ **Dificultad Adaptativa**
- Ajuste automático basado en rendimiento
- Parámetros escalables por nivel
- Algoritmo inteligente de ajuste

✅ **Sistema de Gamificación**
- XP y Niveles (1000 XP/nivel)
- Rachas diarias con multiplicadores
- Daily goals tracking
- 6 logros predefinidos
- Sistema de achievements extensible

## 🧪 Tests

El proyecto incluye 38 tests unitarios con cobertura completa:

```bash
# Ejecutar tests
./gradlew test

# Ejecutar tests con reporte
./gradlew testDebugUnitTest --tests "*"
```

## 📱 Pantalla Actual

La app actualmente muestra:
- Pantalla de bienvenida con "Welcome to MindForge!"
- Tema Material 3 profesional (azul primario)
- Soporte para modo oscuro

**Nota**: Los juegos estarán disponibles en las próximas fases.

## 🎮 Próximas Funcionalidades

### Fase 3: Memory Matrix
- Juego de memoria visual y espacial
- Grid interactivo con Canvas/Compose
- Contextualización ejecutiva
- Integración con XP y logros

### Fases Futuras
- Task Prioritizer (Matriz de Eisenhower)
- Name-Face Associator
- Meeting Recall
- Concept Linker
- Spaced Review System

## 🐛 Solución de Problemas

### Error: "SDK location not found"
Crea un archivo `local.properties` en la raíz:
```properties
sdk.dir=/ruta/a/tu/Android/Sdk
```

### Error de compilación Gradle
```bash
# Limpia el proyecto
./gradlew clean

# Sincroniza y compila
./gradlew build
```

### Emulador lento
- Asegúrate de tener habilitada la aceleración de hardware (Intel HAXM o AMD Hypervisor)
- Aumenta la RAM asignada al emulador (mínimo 4GB)
- Usa una imagen del sistema x86_64

## 📚 Documentación

- [Especificación Completa](MINDFORGE_PROJECT_PROMPT.md)
- Commits detallados con descripción de cada fase

## 👥 Contribuir

Este proyecto sigue las fases de desarrollo especificadas en `MINDFORGE_PROJECT_PROMPT.md`.

## 📄 Licencia

[Por definir]

---

**Desarrollado con ❤️ usando Jetpack Compose y Material 3**
