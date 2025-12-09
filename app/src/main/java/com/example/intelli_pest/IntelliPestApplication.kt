package com.example.intelli_pest

import android.app.Application
import com.example.intelli_pest.di.AppContainer

/**
 * Application class for Intelli-PEST
 */
class IntelliPestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize dependency injection
        AppContainer.initialize(applicationContext)
    }
}

