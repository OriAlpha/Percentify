# Percentify

A beautiful, Material 3-compliant interactive progress and percentage tracker for your Android home screen. With multiple widget layout themes, custom photo backgrounds, and an innovative clock-style rotary slider, **Percentify** elevates the visual design of personal level and progress tracking.

---

## 🎨 Core Features

- **Progress/Level Visualizers**: Choose from multiple styles including Classic Circle, Ambient Glow, Corner Ring, Thin Hollow, Solid Accent, Bar Progress, and Minimal Text.
- **Clock-Style Circular Dial**: Set your values instantly on the update screen using an innovative, highly intuitive rotary gesture progress wheel.
- **Custom Adaptive Imagery**: Personalize individual widgets with your own photos, complete with smooth rounded background clipping (`18.dp`) and a high-contrast protective translucent scrim.
- **Accented Themes**: Choose from vibrant system theme accents (Emerald, Amber, Violet, Rose, Sunset, Ocean, Space) to customize individual widgets.
- **Dynamic Previews**: View real-time interactive widget previews in-app before saving your configuration.

---

## 🚀 GitHub Actions CI Setup Included

This project contains a fully configured GitHub Actions workflow (`.github/workflows/build_apk.yml`) that automatically:
1. Triggers on pushes or pull requests to the main branches.
2. Formats and caches project gradle configurations.
3. Automatically compiles the application and signs a debug release installer package.
4. Uploads the generated `.apk` file directly to your GitHub Action artifacts for single-click downloads.

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
