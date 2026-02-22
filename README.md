# ShelfCount

ShelfCount is an Android inventory app for tracking household stock (grocery, bathroom, laundry, spices, cleaning, pantry, etc.) with fast quantity updates, low-stock visibility, and category-based organization.

## Features

- Inventory list with compact item cards
- One-tap `+/-` quantity controls
- Hard delete for items
- Add/Edit item flow with:
  - category picker
  - unit picker
  - low-stock threshold
  - optional notes
- Category management:
  - default seeded categories
  - custom category creation
  - `Other` category kept at the end of display ordering
- Search, category filter, low-stock filter, and sort options
- Low-stock badge/tagging on list items
- Offline-first local persistence with Room

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM + StateFlow
- Room (SQLite)
- Hilt (DI)
- Coroutines
- Gradle Kotlin DSL + Version Catalog
- Static analysis:
  - ktlint
  - detekt

## Architecture

```text
UI (Compose Screens)
  -> ViewModel (state + user intents + validation)
    -> Domain Repositories (interfaces)
      -> Data Repositories (implementations)
        -> Room DAOs / Database
```

Key folders:

- `app/src/main/java/com/shelfcount/app/presentation` - screens, UI state, ViewModel
- `app/src/main/java/com/shelfcount/app/domain` - domain models + repository interfaces
- `app/src/main/java/com/shelfcount/app/data` - Room entities/dao, mappers, repository impl
- `app/src/main/java/com/shelfcount/app/di` - Hilt modules
- `app/src/main/java/com/shelfcount/app/ui/theme` - design tokens and app theme

## Requirements

- Android Studio (latest stable recommended)
- JDK 17
- Android SDK installed for:
  - `compileSdk = 35`
  - `targetSdk = 35`
- Minimum device SDK: `24`

## Setup

1. Clone repository:
   ```bash
   git clone https://github.com/pratyusv/shelf-count.git
   cd shelf-count
   ```
2. Open in Android Studio.
3. Let Gradle sync complete.
4. Run app on emulator/device.

## Build and Quality Commands

From project root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:ktlintMainSourceSetCheck
./gradlew :app:detekt
```

Auto-format Kotlin style:

```bash
./gradlew :app:ktlintFormat
```

## Database Behavior

- Room database is used as the primary source of truth.
- In debug builds, database reset behavior has been enabled in application startup for clean reruns during development.

## Sync Extensibility (Future)

The codebase already includes abstraction points for future cloud sync:

- `RemoteDataSource`
- `SyncOrchestrator`
- no-op implementations currently active

This allows migration to a remote backend (e.g., Google Sheets via API layer) without rewriting UI contracts.

## Known Build Notes

1. You may see warnings like:
   - `Unable to strip ... libandroidx.graphics.path.so`
   These are non-blocking packaging warnings in this setup.

2. Current `gradle.properties` includes compatibility flags needed for this environment/toolchain combination.

## Git Conventions

- Main branch: `main`
- Keep `main` stable; prefer feature branches for changes.
- `docs/` is intentionally ignored from version control.

## License

MIT License. See `LICENSE`.
