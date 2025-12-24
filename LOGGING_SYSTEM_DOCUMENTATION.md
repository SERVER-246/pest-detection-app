# Intelli-PEST Application Logging System

## Overview

A comprehensive logging system has been implemented to track all user actions, system responses, and errors throughout the application. This enables precise debugging and understanding of app behavior.

## Log Format

Each log entry contains:
- **Timestamp**: When the event occurred
- **Type**: Category of the event (ACTION, RESPONSE, ERROR, etc.)
- **Screen**: Which screen/component the event occurred in
- **Action**: What action was performed
- **Details**: Additional context
- **Status**: Result of the action
- **Error Info**: (If applicable) Error message and stack trace

## Example Log Output

```
[2025-12-16 10:30:45.123] ACTION: Camera_Button_Clicked | Screen: MainScreen | Status: TRIGGERED
[2025-12-16 10:30:46.234] LIFECYCLE: Screen_Opened | Screen: CameraScreen | Status: OPENED
[2025-12-16 10:30:52.456] ACTION: Capture_Initiated | Screen: CameraScreen | Status: TRIGGERED
[2025-12-16 10:30:53.567] RESPONSE: Image_Saved | Screen: CameraScreen | Status: SUCCESS | Details: Photo saved to: /cache/capture_xxx.jpg
[2025-12-16 10:30:54.678] RESPONSE: Bitmap_Ready | Screen: CameraScreen | Status: SUCCESS | Details: Final bitmap: 1920x1080, Config: ARGB_8888
[2025-12-16 10:30:55.789] ACTION: Starting_Detection | Screen: CameraScreen | Status: TRIGGERED
[2025-12-16 10:30:56.890] MODEL: Initialize_Model | Screen: ModelManager | Status: STARTED | Details: Model: models/mobilenet_v2.tflite
[2025-12-16 10:30:58.901] MODEL: Initialize_Model | Screen: ModelManager | Status: SUCCESS | Details: Model initialized successfully
[2025-12-16 10:31:00.012] RESPONSE: Detection_Success | Screen: DetectionViewModel | Status: SUCCESS | Details: Pest: Armyworm, Confidence: 92.5%
```

## Log Types

| Type | Description | Example |
|------|-------------|---------|
| `ACTION` | User-initiated actions | Button clicks, selections |
| `RESPONSE` | System responses to actions | Success confirmations |
| `ERROR` | Error conditions | Crashes, failures |
| `WARNING` | Non-fatal issues | Quality warnings |
| `INFO` | General information | Status updates |
| `DEBUG` | Technical details | Internal state |
| `LIFECYCLE` | Screen/component lifecycle | Screen opened/closed |
| `MODEL` | ML model operations | Load, inference |
| `IMAGE` | Image processing | Capture, validation |
| `NETWORK` | Network operations | Downloads |

## Files with Logging

### Core Components

1. **AppLogger.kt** - Central logging utility
   - Log to Logcat AND file
   - In-memory queue for recent logs
   - Export functionality

2. **IntelliPestApplication.kt**
   - App startup/shutdown
   - DI initialization

3. **MainActivity.kt**
   - Activity lifecycle
   - Navigation events
   - All button clicks

### Presentation Layer

4. **CameraScreen.kt**
   - Camera permission
   - Capture initiation
   - Image processing steps
   - Success/failure callbacks

5. **GalleryPicker.kt**
   - Picker launch
   - Image selection
   - All 4 bitmap loading methods
   - Bitmap conversion

6. **DetectionViewModel.kt**
   - Model selection
   - Detection start/end
   - Results/errors

### Data Layer

7. **PestDetectionRepositoryImpl.kt**
   - Model path resolution
   - Model loading status
   - Detection pipeline

8. **TFLiteModelWrapper.kt**
   - Model initialization
   - Buffer loading
   - Inference execution

## How to View Logs

### Method 1: Android Studio Logcat
Filter by tag: `AppLogger`

```bash
adb logcat -s AppLogger:*
```

### Method 2: Export from App
Logs are saved to: `{app_files_dir}/logs/intelli_pest_log.txt`

```bash
adb pull /data/data/com.example.intelli_pest/files/logs/intelli_pest_log.txt
```

### Method 3: In-App (Future)
A debug screen can be added to view logs directly in the app.

## Tracked User Journey

### Camera Flow
```
1. [ACTION] Camera_Button_Clicked (MainScreen)
2. [LIFECYCLE] Screen_Opened (CameraScreen)
3. [ACTION] Capture_Initiated (CameraScreen)
4. [DEBUG] Capture_File_Created
5. [INFO] Taking_Picture
6. [RESPONSE] Image_Saved
7. [INFO] Processing_Image
8. [DEBUG] Decoding_Bitmap
9. [DEBUG] Correcting_Rotation
10. [DEBUG] Converting_Software_Bitmap
11. [RESPONSE] Bitmap_Ready
12. [RESPONSE] Image_Processing_Complete
13. [ACTION] Starting_Detection (MainScreen)
14. [MODEL] Initialize_Model - STARTED
15. [MODEL] Initialize_Model - SUCCESS
16. [RESPONSE] Detection_Success / [ERROR] Detection_Failed
```

### Gallery Flow
```
1. [ACTION] Gallery_Button_Clicked (MainScreen)
2. [LIFECYCLE] Screen_Opened (GalleryPicker)
3. [ACTION] Launching_Picker
4. [ACTION] Image_Selection_Result (URI)
5. [INFO] Loading_Bitmap
6. [DEBUG] Method_1/2/3/4 attempts
7. [RESPONSE] Method_X_Success
8. [DEBUG] Ensure_Software
9. [INFO/DEBUG] Converting_Bitmap (if needed)
10. [RESPONSE] Bitmap_Loaded
11. [ACTION] Starting_Detection
... (same detection flow)
```

## Debugging Common Issues

### Issue: "Failed to load model"
Look for logs with:
- `[MODEL] Initialize_Model - FAILED`
- Check error message and stack trace
- Verify model file exists in assets

### Issue: Camera crash
Look for logs with:
- `[ERROR] Camera_Error`
- `[ERROR] Processing_Error`
- Check bitmap config and size

### Issue: Gallery crash
Look for logs with:
- `[WARNING] Method_X_Failed` - which methods failed
- `[ERROR] All_Methods_Failed` - no method worked
- `[ERROR] Load_Exception`

## Usage in Code

```kotlin
// Simple action log
AppLogger.logAction("ScreenName", "Button_Clicked", "Additional details")

// Response with details
AppLogger.logResponse("ScreenName", "Operation_Complete", "Result: $result")

// Error with exception
AppLogger.logError("ScreenName", "Operation_Failed", exception, "Context info")

// Model operation
AppLogger.logModelOperation("Load_Model", modelId, "SUCCESS", "Loaded in 500ms")

// Screen lifecycle
AppLogger.logScreenOpened("CameraScreen")
AppLogger.logScreenClosed("CameraScreen")
```

## Log File Management

- **Max entries in memory**: 1000
- **Max file size**: 5MB (auto-rotated)
- **Backup file**: `intelli_pest_log_backup.txt`

## Date Created
December 16, 2025

