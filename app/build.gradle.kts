plugins {
    id("mindforge.android.application")
    id("mindforge.android.compose")
    id("mindforge.android.hilt")
}

android {
    namespace = "com.mindforge"

    defaultConfig {
        applicationId = "com.mindforge"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core modules
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-common"))
    implementation(project(":navigation"))

    // Feature modules
    implementation(project(":features:feature-home"))

    // Game modules
    implementation(project(":games:game-core"))
    implementation(project(":games:game-memory-matrix"))
    implementation(project(":games:game-task-prioritizer"))
    implementation(project(":games:game-name-face"))
    implementation(project(":games:game-meeting-recall"))
    implementation(project(":games:game-concept-linker"))
    implementation(project(":games:game-spaced-review"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    testRuntimeOnly(libs.junit.jupiter.engine)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(platform(libs.compose.bom))
    debugImplementation(libs.compose.ui.test.manifest)
}
