# MONTAI MATRIX — Android

A lightweight, offline cyber-terminal / digital-rain simulator for Android 6.0+ and Android 14.

## Features
- Animated Matrix-style digital rain (pausable from the command deck)
- MONTAI-themed terminal interface and custom launcher icon
- Local device snapshot: manufacturer/model, Android version/API and battery percentage
- Simulated commands: `help`, `scan`, `whoami`, `matrix`, `clear`, `about`
- No Internet permission, no root, no file access, no background service

## Build the APK
This workspace does not include the Android SDK/Gradle toolchain, so the APK is built in GitHub Actions.

1. Create a new GitHub repository named `MONTAI-MATRIX`.
2. Upload the contents of this folder to the repository (the `.github` folder must be included).
3. Open **Actions** → **Build MONTAI MATRIX APK** → **Run workflow**.
4. When it finishes, open the workflow run and download the artifact named `MONTAI-MATRIX-debug-apk`.
5. Extract the ZIP and install `app-debug.apk` on your phone. If Android asks, allow installation from that source.

The workflow builds a debug APK targeting Android 15 (API 35), compatible with Android 14. The debug APK is suitable for personal testing; it is not a Play Store release.

## Safety
All hacking language and terminal output are visual simulation only. Device information is limited to public OS/device properties and battery status. The app does not scan private data or communicate over the network.
