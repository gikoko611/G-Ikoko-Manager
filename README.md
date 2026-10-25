# I KO KO Manager

Original rootless, offline-first Android file and APK utility suite. No MT Manager code, resources, icons, or branding are copied.

## Planned modules
- File manager and SAF-based storage access
- Text/JSON/XML/Hex editor
- ZIP and archive tools
- APK/APKS/XAPK/APKM inspection and extraction
- Split APK install helper using Android PackageInstaller
- APK signing and validation
- DEX/Smali and Android resource analysis (incremental implementation)

## Target
- Android 10+
- ARMv7 (armeabi-v7a) compatibility
- Kotlin + Jetpack Compose
- Local processing; no network permission in the core app

## Build
GitHub Actions builds a debug APK on pushes and manual dispatch. Advanced binary-format features will be added and validated incrementally; feature-list entries are roadmap until implemented and tested.
