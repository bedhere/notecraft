# Notecraft 笔记工坊

A cross-platform note-taking application built with Kotlin Multiplatform + Compose Multiplatform, inspired by [Floral Notepaper](https://github.com/Achilng/floral-notepaper).

## Features

- Create, edit, and delete notes with Markdown support
- Auto-save with debounce (800ms) and serialized save queue
- Markdown real-time preview (Edit / Split / Preview modes)
- Search and sort notes
- Dark / Light / System theme
- Import and export Markdown files
- Desktop: system tray, global shortcuts, tile mode, window state persistence
- Web: browser localStorage persistence
- Android: mobile-optimized navigation (list → detail), state restoration

## Architecture

`
notecraft/
├── shared/                  # KMP shared module
│   ├── commonMain/          # Cross-platform code
│   │   ├── domain/model/    # Note, NoteMetadata, AppConfig
│   │   ├── domain/repository/  # Repository interfaces
│   │   ├── data/repository/    # Repository implementations
│   │   ├── data/storage/       # Storage interfaces
│   │   ├── presentation/       # ViewModels + UI state
│   │   └── ui/                 # Composable screens, Markdown renderer, Theme
│   ├── jvmMain/             # Desktop (JVM) platform
│   ├── androidMain/         # Android platform
│   ├── jsMain/              # Web (JS) platform
│   └── wasmJsMain/          # Web (Wasm) platform
├── androidApp/              # Android entry point
├── desktopApp/              # Desktop entry point (tray, shortcuts, tiles)
└── webApp/                  # Web entry point
`

## Build & Run

### Desktop

`ash
./gradlew :desktopApp:run
`

### Web

`ash
# Development (JS)
./gradlew :webApp:jsBrowserDevelopmentRun

# Production build
./gradlew :webApp:jsProductionExecutableCompileSync
`

### Android

`ash
./gradlew :androidApp:assembleDebug
`

## Tests

`ash
# Shared module tests (JVM)
./gradlew :shared:jvmTest

# All tests
./gradlew :shared:allTests
`

## Technical Stack

- **Language**: Kotlin 2.4.10
- **UI Framework**: Compose Multiplatform 1.11.1
- **Minimum SDK**: Android 24
- **Architecture**: Repository pattern + ViewModel + StateFlow
- **Serialization**: kotlinx-serialization-json
- **Persistence**: JSON files (Desktop/Android), localStorage (Web)
- **Desktop features**: Compose Desktop Window API + tray/shortcut plugins

## License

MIT License. See [LICENSE](LICENSE) for details.

Based on [Floral Notepaper](https://github.com/Achilng/floral-notepaper) (MIT).
