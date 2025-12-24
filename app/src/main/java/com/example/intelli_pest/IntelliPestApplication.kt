package com.example.intelli_pest

import android.app.Application
import com.example.intelli_pest.di.AppContainer
import com.example.intelli_pest.util.AppLogger

/**
 * Application class for Intelli-PEST
 */
class IntelliPestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the application logger first
        AppLogger.init(applicationContext)
        AppLogger.logInfo("Application", "App_Started", "Intelli-PEST application starting...")

        // Initialize dependency injection
        AppContainer.initialize(applicationContext)
        AppLogger.logResponse("Application", "DI_Initialized", "Dependency injection container initialized")
    }

    override fun onTerminate() {
        AppLogger.logSessionSummary()
        AppLogger.logInfo("Application", "App_Terminated", "Application shutting down")
        super.onTerminate()
    }
}
