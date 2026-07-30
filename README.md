# Percentify

A beautiful, Material 3-compliant interactive progress and percentage tracker for your Android home screen. With multiple widget layout themes, custom photo backgrounds, and an innovative clock-style rotary slider, **Percentify** elevates the visual design of personal level and progress tracking.

---

## 🎨 Core Features

- **Progress/Level Visualizers**: Choose from multiple styles including Classic Wheel, Corner Ring, Solid Accent, and Bar Progress.
- **Clock-Style Circular Dial**: Set your values instantly on the update screen using an innovative, highly intuitive rotary gesture progress wheel.
- **Custom Adaptive Imagery**: Personalize individual widgets with your own photos, complete with smooth rounded background clipping (`18.dp`) and a high-contrast protective translucent scrim.
- **Accented Themes**: Choose from vibrant system theme accents (Emerald, Amber, Violet, Rose, Sunset, Ocean, Space) to customize individual widgets.
- **Dynamic Previews**: View real-time interactive widget previews in-app before saving your configuration.

---

## 🚀 GitHub Actions CI & Auto-Release Setup

This project includes automated GitHub Actions workflows:

1. **Auto Build & GitHub Release** ([release.yml](file:///D:/Projects/Percentify/.github/workflows/release.yml)):
   - **Triggers**: Pushing a tag (`git tag v1.0.0 && git push origin v1.0.0`), publishing a release on GitHub, or running manually via `Actions -> Build APK & Create Release -> Run workflow`.
   - **Behavior**: Compiles the Android APKs (Release & Debug), saves them to Actions artifacts, and automatically creates/updates a GitHub Release with the `.apk` files attached!

2. **CI Build & Artifact Upload** ([build_apk.yml](file:///D:/Projects/Percentify/.github/workflows/build_apk.yml)):
   - **Triggers**: Every push or pull request to `main`/`master`.
   - **Behavior**: Verifies project compilation and attaches the generated debug `.apk` file to the GitHub Actions run summary.

---

## 🛠️ Requirements & Compilation

- **Android Studio / Gradle (Kotlin DSL)**
- **Java SE Development Kit 17**
- **Jetpack Compose / glance-appwidget** for dynamic Material 3 and Home Screen layouts.

Compile app directly:
```bash
./gradlew assembleDebug
```

---

## 👨‍💻 Author & Developer
This application was designed and developed by **Suhas Goravale Siddaramu**.
