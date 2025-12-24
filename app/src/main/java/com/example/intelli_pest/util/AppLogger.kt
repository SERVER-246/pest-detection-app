package com.example.intelli_pest.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Centralized Application Logger
 * Tracks all user actions, system responses, and errors throughout the app
 */
object AppLogger {

    private const val TAG = "AppLogger"
    private const val LOG_FILE_NAME = "intelli_pest_log.txt"
    private const val MAX_LOG_ENTRIES = 1000
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 // 5MB max log file

    private var context: Context? = null
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    // Tracking mode - when disabled, only errors are logged
    private var trackingModeEnabled: Boolean = true // Default to enabled for debugging

    // Log entry types
    enum class LogType {
        ACTION,      // User clicked something
        RESPONSE,    // System responded to an action
        ERROR,       // Error occurred
        WARNING,     // Warning (non-fatal)
        INFO,        // General information
        DEBUG,       // Debug information
        LIFECYCLE,   // Screen/component lifecycle
        MODEL,       // Model operations
        IMAGE,       // Image processing operations
        NETWORK      // Network operations
    }

    // Log entry data class
    data class LogEntry(
        val timestamp: String,
        val type: LogType,
        val screen: String,
        val action: String,
        val details: String,
        val status: String,
        val errorMessage: String? = null,
        val stackTrace: String? = null
    ) {
        override fun toString(): String {
            val builder = StringBuilder()
            builder.append("[$timestamp] ")
            builder.append("${type.name}: ")
            builder.append("$action | ")
            builder.append("Screen: $screen | ")
            builder.append("Status: $status")
            if (details.isNotEmpty()) {
                builder.append(" | Details: $details")
            }
            if (!errorMessage.isNullOrEmpty()) {
                builder.append(" | Error: $errorMessage")
            }
            return builder.toString()
        }

        fun toDetailedString(): String {
            val builder = StringBuilder()
            builder.appendLine("═══════════════════════════════════════")
            builder.appendLine("Timestamp: $timestamp")
            builder.appendLine("Type: ${type.name}")
            builder.appendLine("Screen: $screen")
            builder.appendLine("Action: $action")
            builder.appendLine("Status: $status")
            if (details.isNotEmpty()) {
                builder.appendLine("Details: $details")
            }
            if (!errorMessage.isNullOrEmpty()) {
                builder.appendLine("Error: $errorMessage")
            }
            if (!stackTrace.isNullOrEmpty()) {
                builder.appendLine("Stack Trace:")
                builder.appendLine(stackTrace)
            }
            builder.appendLine("═══════════════════════════════════════")
            return builder.toString()
        }
    }

    /**
     * Initialize the logger with application context
     */
    fun init(appContext: Context) {
        context = appContext.applicationContext
        logDeviceInfo()
        logInfo("AppLogger", "Logger_Initialized", "AppLogger initialized successfully")
    }

    /**
     * Set tracking mode enabled/disabled
     */
    fun setTrackingMode(enabled: Boolean) {
        trackingModeEnabled = enabled
        Log.d(TAG, "Tracking mode set to: $enabled")
    }

    /**
     * Check if tracking mode is enabled
     */
    fun isTrackingModeEnabled(): Boolean = trackingModeEnabled

    /**
     * Log device information at startup
     */
    private fun logDeviceInfo() {
        val deviceInfo = buildString {
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("App Version: ${getAppVersion()}")
        }
        log(
            type = LogType.INFO,
            screen = "System",
            action = "Device_Info",
            details = deviceInfo,
            status = "INFO"
        )
    }

    private fun getAppVersion(): String {
        return try {
            context?.packageManager?.getPackageInfo(context?.packageName ?: "", 0)?.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Main logging function
     */
    @Synchronized
    fun log(
        type: LogType,
        screen: String,
        action: String,
        details: String = "",
        status: String = "OK",
        error: Throwable? = null
    ) {
        // If tracking mode is disabled, only log errors
        if (!trackingModeEnabled && type != LogType.ERROR) {
            // Still log to Logcat for debugging, but don't store
            val timestamp = dateFormat.format(Date())
            Log.d(TAG, "[$timestamp] ${type.name}: $action | Screen: $screen | Status: $status")
            return
        }

        val timestamp = dateFormat.format(Date())
        val errorMessage = error?.message
        val stackTrace = error?.let { getStackTraceString(it) }

        val entry = LogEntry(
            timestamp = timestamp,
            type = type,
            screen = screen,
            action = action,
            details = details,
            status = status,
            errorMessage = errorMessage,
            stackTrace = stackTrace
        )

        // Add to in-memory queue
        logQueue.add(entry)

        // Trim queue if too large
        while (logQueue.size > MAX_LOG_ENTRIES) {
            logQueue.poll()
        }

        // Log to Android Logcat
        val logMessage = entry.toString()
        when (type) {
            LogType.ERROR -> Log.e(TAG, logMessage, error)
            LogType.WARNING -> Log.w(TAG, logMessage)
            LogType.DEBUG -> Log.d(TAG, logMessage)
            else -> Log.i(TAG, logMessage)
        }

        // Write to file asynchronously
        writeToFileAsync(entry)
    }

    /**
     * Helper functions for common log types
     */

    // Log user action (button click, selection, etc.)
    fun logAction(screen: String, action: String, details: String = "") {
        log(LogType.ACTION, screen, action, details, "TRIGGERED")
    }

    // Log successful response
    fun logResponse(screen: String, action: String, details: String = "") {
        log(LogType.RESPONSE, screen, action, details, "SUCCESS")
    }

    // Log error
    fun logError(screen: String, action: String, error: Throwable?, details: String = "") {
        log(LogType.ERROR, screen, action, details, "FAILED", error)
    }

    // Log error with message only
    fun logError(screen: String, action: String, errorMessage: String, details: String = "") {
        log(
            type = LogType.ERROR,
            screen = screen,
            action = action,
            details = details,
            status = "FAILED",
            error = Exception(errorMessage)
        )
    }

    // Log warning
    fun logWarning(screen: String, action: String, details: String = "") {
        log(LogType.WARNING, screen, action, details, "WARNING")
    }

    // Log info
    fun logInfo(screen: String, action: String, details: String = "") {
        log(LogType.INFO, screen, action, details, "INFO")
    }

    // Log debug
    fun logDebug(screen: String, action: String, details: String = "") {
        log(LogType.DEBUG, screen, action, details, "DEBUG")
    }

    // Log screen lifecycle
    fun logScreenOpened(screen: String) {
        log(LogType.LIFECYCLE, screen, "Screen_Opened", "", "OPENED")
    }

    fun logScreenClosed(screen: String) {
        log(LogType.LIFECYCLE, screen, "Screen_Closed", "", "CLOSED")
    }

    // Log model operations
    fun logModelOperation(action: String, modelId: String, status: String, details: String = "", error: Throwable? = null) {
        log(LogType.MODEL, "ModelManager", action, "Model: $modelId | $details", status, error)
    }

    // Log image operations
    fun logImageOperation(screen: String, action: String, details: String, status: String, error: Throwable? = null) {
        log(LogType.IMAGE, screen, action, details, status, error)
    }

    /**
     * Get all logs as a list
     */
    fun getLogs(): List<LogEntry> {
        return logQueue.toList()
    }

    /**
     * Get logs filtered by type
     */
    fun getLogsByType(type: LogType): List<LogEntry> {
        return logQueue.filter { it.type == type }
    }

    /**
     * Get recent logs (last N entries)
     */
    fun getRecentLogs(count: Int = 50): List<LogEntry> {
        return logQueue.toList().takeLast(count)
    }

    /**
     * Get error logs only
     */
    fun getErrorLogs(): List<LogEntry> {
        return logQueue.filter { it.type == LogType.ERROR }
    }

    /**
     * Get formatted log string for export
     */
    fun getFormattedLogs(): String {
        val builder = StringBuilder()
        builder.appendLine("╔══════════════════════════════════════════════════════════════╗")
        builder.appendLine("║           INTELLI-PEST APPLICATION LOGS                      ║")
        builder.appendLine("║           Generated: ${dateFormat.format(Date())}            ║")
        builder.appendLine("╚══════════════════════════════════════════════════════════════╝")
        builder.appendLine()

        logQueue.forEach { entry ->
            builder.appendLine(entry.toString())
        }

        return builder.toString()
    }

    /**
     * Export logs to file and return the file path
     */
    fun exportLogs(): String? {
        return try {
            val logFile = getLogFile()
            logFile?.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export logs", e)
            null
        }
    }

    /**
     * Clear all logs
     */
    fun clearLogs() {
        logQueue.clear()
        try {
            getLogFile()?.writeText("")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log file", e)
        }
    }

    /**
     * Get the log file
     */
    private fun getLogFile(): File? {
        val ctx = context ?: return null
        val logDir = File(ctx.filesDir, "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        return File(logDir, LOG_FILE_NAME)
    }

    /**
     * Write log entry to file asynchronously
     */
    private fun writeToFileAsync(entry: LogEntry) {
        Thread {
            try {
                val logFile = getLogFile() ?: return@Thread

                // Check file size and rotate if needed
                if (logFile.exists() && logFile.length() > MAX_FILE_SIZE_BYTES) {
                    rotateLogFile(logFile)
                }

                FileWriter(logFile, true).use { writer ->
                    writer.appendLine(entry.toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write log to file", e)
            }
        }.start()
    }

    /**
     * Rotate log file when it gets too large
     */
    private fun rotateLogFile(logFile: File) {
        try {
            val backupFile = File(logFile.parent, "intelli_pest_log_backup.txt")
            if (backupFile.exists()) {
                backupFile.delete()
            }
            logFile.renameTo(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate log file", e)
        }
    }

    /**
     * Get stack trace as string
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = java.io.StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    /**
     * Log session summary (call this when app is closing)
     */
    fun logSessionSummary() {
        val totalLogs = logQueue.size
        val errorCount = logQueue.count { it.type == LogType.ERROR }
        val warningCount = logQueue.count { it.type == LogType.WARNING }
        val actionCount = logQueue.count { it.type == LogType.ACTION }

        val summary = buildString {
            appendLine("Session Summary:")
            appendLine("  Total Events: $totalLogs")
            appendLine("  User Actions: $actionCount")
            appendLine("  Errors: $errorCount")
            appendLine("  Warnings: $warningCount")
        }

        log(LogType.INFO, "System", "Session_End", summary, "COMPLETED")
    }

    /**
     * Get a shareable log file in external cache directory
     * This file can be shared via Intent
     */
    fun getShareableLogFile(): File? {
        val ctx = context ?: return null
        try {
            // Use external cache dir which is accessible for sharing
            val shareDir = File(ctx.externalCacheDir ?: ctx.cacheDir, "shared_logs")
            if (!shareDir.exists()) {
                shareDir.mkdirs()
            }

            val shareFile = File(shareDir, "intelli_pest_logs_${System.currentTimeMillis()}.txt")

            // Write all logs to the shareable file
            FileWriter(shareFile).use { writer ->
                writer.appendLine("╔══════════════════════════════════════════════════════════════╗")
                writer.appendLine("║           INTELLI-PEST APPLICATION LOGS                      ║")
                writer.appendLine("║           Generated: ${dateFormat.format(Date())}            ║")
                writer.appendLine("║           Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                writer.appendLine("║           Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                writer.appendLine("╚══════════════════════════════════════════════════════════════╝")
                writer.appendLine()

                // Write in-memory logs
                writer.appendLine("=== IN-MEMORY LOGS (${logQueue.size} entries) ===")
                writer.appendLine()
                logQueue.forEach { entry ->
                    writer.appendLine(entry.toString())
                }

                // Also append file logs if exists
                val fileLog = getLogFile()
                if (fileLog?.exists() == true) {
                    writer.appendLine()
                    writer.appendLine("=== FILE LOGS ===")
                    writer.appendLine()
                    writer.append(fileLog.readText())
                }
            }

            Log.d(TAG, "Shareable log file created: ${shareFile.absolutePath}")
            return shareFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create shareable log file", e)
            return null
        }
    }

    /**
     * Get logs as plain text string for clipboard/sharing
     */
    fun getLogsAsText(): String {
        val builder = StringBuilder()
        builder.appendLine("INTELLI-PEST LOGS - ${dateFormat.format(Date())}")
        builder.appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        builder.appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        builder.appendLine("Total entries: ${logQueue.size}")
        builder.appendLine("─".repeat(60))
        builder.appendLine()

        logQueue.forEach { entry ->
            builder.appendLine(entry.toString())
        }

        return builder.toString()
    }

    /**
     * Download logs to local storage (Downloads folder)
     * Returns the file path if successful, null otherwise
     */
    fun downloadLogsToStorage(): String? {
        val ctx = context ?: return null
        try {
            // Get the Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            // Create log file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val logFileName = "intelli_pest_logs_$timestamp.txt"
            val logFile = File(downloadsDir, logFileName)

            // Write logs to file
            FileWriter(logFile).use { writer ->
                writer.appendLine("╔══════════════════════════════════════════════════════════════╗")
                writer.appendLine("║           INTELLI-PEST APPLICATION LOGS                      ║")
                writer.appendLine("║           Generated: ${dateFormat.format(Date())}            ║")
                writer.appendLine("║           Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                writer.appendLine("║           Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                writer.appendLine("║           Tracking Mode: ${if (trackingModeEnabled) "ENABLED" else "DISABLED"}")
                writer.appendLine("╚══════════════════════════════════════════════════════════════╝")
                writer.appendLine()

                // Write in-memory logs
                writer.appendLine("=== APPLICATION LOGS (${logQueue.size} entries) ===")
                writer.appendLine()
                logQueue.forEach { entry ->
                    writer.appendLine(entry.toString())
                }

                // Also append file logs if exists
                val fileLog = getLogFile()
                if (fileLog?.exists() == true) {
                    writer.appendLine()
                    writer.appendLine("=== PERSISTENT FILE LOGS ===")
                    writer.appendLine()
                    writer.append(fileLog.readText())
                }
            }

            Log.d(TAG, "Logs downloaded to: ${logFile.absolutePath}")
            return logFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download logs to storage", e)
            return null
        }
    }

    /**
     * Download logs to app's local storage (no permissions required)
     * Returns the file path if successful, null otherwise
     */
    fun downloadLogsToAppStorage(): String? {
        val ctx = context ?: return null
        try {
            // Use app's external files directory (accessible via file manager)
            val logsDir = File(ctx.getExternalFilesDir(null), "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }

            // Create log file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val logFileName = "intelli_pest_logs_$timestamp.txt"
            val logFile = File(logsDir, logFileName)

            // Write logs to file
            FileWriter(logFile).use { writer ->
                writer.appendLine("╔══════════════════════════════════════════════════════════════╗")
                writer.appendLine("║           INTELLI-PEST APPLICATION LOGS                      ║")
                writer.appendLine("║           Generated: ${dateFormat.format(Date())}            ║")
                writer.appendLine("║           Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                writer.appendLine("║           Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                writer.appendLine("║           Tracking Mode: ${if (trackingModeEnabled) "ENABLED" else "DISABLED"}")
                writer.appendLine("╚══════════════════════════════════════════════════════════════╝")
                writer.appendLine()

                // Write in-memory logs
                writer.appendLine("=== APPLICATION LOGS (${logQueue.size} entries) ===")
                writer.appendLine()
                logQueue.forEach { entry ->
                    writer.appendLine(entry.toString())
                }

                // Also append file logs if exists
                val fileLog = getLogFile()
                if (fileLog?.exists() == true) {
                    writer.appendLine()
                    writer.appendLine("=== PERSISTENT FILE LOGS ===")
                    writer.appendLine()
                    writer.append(fileLog.readText())
                }
            }

            Log.d(TAG, "Logs downloaded to app storage: ${logFile.absolutePath}")
            return logFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download logs to app storage", e)
            return null
        }
    }
}

