# Build & Install (Markdown Preview Feature)

## Requirements

| Tool | Version |
|------|---------|
| Java | 17+ |
| Android SDK | `platforms;35` + `build-tools;37.0.0` |
| NDK | `28.1.13356709` |
| Gradle | 9.3.1 (bundled wrapper) |

## One-time Setup (any machine)

### Option A: Android Studio (easiest)

1. Install [Android Studio](https://developer.android.com/studio)
2. Open SDK Manager → Install:
   - SDK Platform 35
   - Android SDK Build-Tools 37
   - NDK 28.1.13356709
3. Open this project, let it sync
4. Run → select `app`

### Option B: Command-line (macOS/Linux)

```bash
# 1. Install Java 17+
sudo apt install openjdk-17-jdk          # Debian/Ubuntu
brew install openjdk@17                  # macOS

# 2. Download Android SDK
# Set these in ~/.bashrc or ~/.zshrc:
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

# 3. Install SDK tools
curl -o cmdline-tools.zip \
  https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"

# 4. Accept licenses & install SDK/NDK
yes | sdkmanager --licenses
sdkmanager "platforms;android-35" "build-tools;37.0.0" "ndk;28.1.13356709"
```

## Build APK

```bash
# Clone (if not already)
git clone git@github.com:zerwiz/MaterialFiles.git
cd MaterialFiles

# Debug build (unsigned, auto-signed with debug keystore)
./gradlew assembleDebug

# APK location:
#   app/build/outputs/apk/debug/app-debug.apk
```

> **Note:** `signing.gradle` is only used for release builds. Debug builds work without it.

## Install on Phone

| Method | Steps |
|--------|-------|
| **ADB** (USB debug) | `adb install app/build/outputs/apk/debug/app-debug.apk` |
| **Side-load** | Copy APK to phone → open file → "Install anyway" |
| **Cloud** | `scp app-debug.apk user@host:~/` then download on phone |

Allow "Install from unknown apps" in Settings → Security.

## Run Tests

```bash
# Unit tests (PreviewableFileDetector + 10 more)
./gradlew test --tests "*PreviewableFileDetector*"
```

## Uninstall

Standard Android uninstall, or:
```bash
adb uninstall me.zhanghai.android.files
```
