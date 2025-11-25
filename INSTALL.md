# 📱 Installation Instructions

## Download APK

Download the appropriate APK for your device:

- **app-universal-debug.apk** (75 MB) - **RECOMMENDED** - Works on all devices
- app-arm64-v8a-debug.apk (31 MB) - For modern ARM devices (most phones)
- app-armeabi-v7a-debug.apk (27 MB) - For older ARM devices
- app-x86-debug.apk (33 MB) - For Intel-based Android devices
- app-x86_64-debug.apk (33 MB) - For Intel 64-bit devices

**Not sure which one?** Download **app-universal-debug.apk** - it works on all devices!

## Installation Steps

### Step 1: Enable Unknown Sources

1. Open **Settings** on your Android device
2. Go to **Security** or **Privacy**
3. Enable **Install apps from unknown sources** or **Install from unknown sources**
4. Select your browser or file manager and enable it

*On Android 8.0+: Go to Settings > Apps > Special app access > Install unknown apps > Choose your browser/file manager*

### Step 2: Download APK

1. Download **app-universal-debug.apk** from GitHub Releases
2. Wait for download to complete
3. Open notification or go to Downloads folder

### Step 3: Install

1. Tap the downloaded APK file
2. Tap **Install**
3. Wait for installation to complete
4. Tap **Open** or find the app in your app drawer

### Step 4: Grant Permissions

When you first open the app:

1. **Camera Permission** - Required to capture pest images
2. **Storage/Photos Permission** - Required to select images from gallery

Tap **Allow** for both permissions.

## Troubleshooting

### "App not installed" or "Package appears invalid"

**Solution:** You downloaded the unsigned release APK by mistake. Please download the **debug APK** instead:
- Use **app-universal-debug.apk** (not app-universal-release-unsigned.apk)

### "There was a problem parsing the package"

**Causes:**
- Download was interrupted or corrupted
- Not enough storage space
- Incompatible Android version

**Solutions:**
1. Re-download the APK
2. Free up at least 200 MB storage
3. Ensure Android 7.0 (Nougat) or higher

### "App keeps stopping"

**Solutions:**
1. Grant all required permissions (Settings > Apps > Pest Detection > Permissions)
2. Clear app cache (Settings > Apps > Pest Detection > Storage > Clear Cache)
3. Reinstall the app

### Cannot enable "Unknown Sources"

Some devices have this disabled by policy:
- Check if device is managed by organization (MDM/EMM)
- Contact your IT administrator
- Try using a different device

## Verification

After installation:

1. Open the app - Should show camera and gallery buttons
2. Tap camera/gallery - Should ask for permissions
3. Select the **MobileNet V2** model from dropdown
4. Take or select a test image
5. Tap **"Analyze Pest"** - Should show results

## Need Help?

- Check [README.md](README.md) for usage guide
- Report issues at [GitHub Issues](https://github.com/SERVER-246/pest-detection-app/issues)
- Ensure you're using the correct APK (app-universal-debug.apk)

---

**Important:** This is a debug build for testing. For production use, a properly signed release APK should be used.

