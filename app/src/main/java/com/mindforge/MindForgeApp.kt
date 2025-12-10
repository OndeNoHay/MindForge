package com.mindforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for MindForge
 * Annotated with @HiltAndroidApp to enable dependency injection
 */
@HiltAndroidApp
class MindForgeApp : Application()
