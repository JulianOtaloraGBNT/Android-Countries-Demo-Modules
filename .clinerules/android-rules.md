# .clinerules/android-rules.md

## 1. Project Overview & Core Principles
- **Stack**: Kotlin (null-safety), Coroutines & Flow, Jetpack Compose (Material 3), Navigation Compose, Hilt (DI), Retrofit, kotlinx.serialization, Room (if required outside the SDK).
- **Architecture**: A multi-project Gradle build implementing Clean Architecture, where some modules are consumed as compiled AARs by others.
- **Core Principles**:
    - **Clean Architecture**: Strict adherence to layers (App → Domain ← Data) with KISS & SOLID (SRP, OCP, LSP, ISP, DIP).
    - **Composition over Inheritance**: Especially in UI and module design.
    - **Framework-agnostic Domain**: The `:core:domain` module must not contain any Android, Retrofit, or Room imports.
    - **Strict Call Graph**: `UI (Screen) → ViewModel → UseCase → Repository → DataSource`. Never skip layers.
    - **Dependency Injection**: Via Hilt and constructor injection.
    - **Lifecycle-aware Flow Consumption**: Use `collectAsStateWithLifecycle` in the UI.
    - **Consistent Error Handling**: Base error type + mappers per layer.
    - **Local-first**: UI is backed by a local source of truth (Room), with a controlled remote sync policy.

## 2. Project Module Structure & Build Process
The project consists of **six modules in total**, organized into two categories. The key is how they depend on each other.

### Category 1: Application Framework Modules
These four modules form the core of the application.

- **`:app`**:
    - **Plugin**: `com.android.application`.
    - **Responsibility**: Contains the `MainActivity`, navigation, and presentation layer.
    - **Dependencies**:
        - `implementation(project(":core:domain"))`
        - `implementation(project(":core:common"))`
        - `implementation(files("build/outputs/aar/countries-ui-artifact.aar"))`

- **`:core:data`**:
    - **Plugin**: `com.android.library`.
    - **Responsibility**: Implements repository interfaces by consuming the data SDK's AAR.
    - **Dependencies**:
        - `implementation(project(":core:domain"))`
        - `implementation(project(":core:common"))`
        - `implementation(files("build/outputs/aar/countries-data-sdk.aar"))`

- **`:core:domain`**:
    - **Plugin**: `org.jetbrains.kotlin.jvm` (or `java-library`). **Must not be an Android module.**
    - **Responsibility**: Pure business logic (entities, use cases, repository interfaces).
    - **Dependencies**: `implementation(project(":core:common"))`

- **`:core:common`**:
    - **Plugin**: `org.jetbrains.kotlin.jvm` (or `java-library`). **Must not be an Android module.**
    - **Responsibility**: Shared utilities (`Result`, `AppError`, Dispatcher Qualifiers).
    - **Dependencies**: None.

### Category 2: Feature Library Modules (AAR Producers)
These two modules are also part of the project but are consumed differently.

- **`:features:countries-data-sdk`**:
    - **Plugin**: `com.android.library`.
    - **Responsibility**: A self-contained data access library.
    - **Output**: Produces `countries-data-sdk.aar`. This module is **not a direct dependency** of `:core:data`.

- **`:features:countries-ui-artifact`**:
    - **Plugin**: `com.android.library`.
    - **Responsibility**: A self-contained library of stateless UI components.
    - **Output**: Produces `countries-ui-artifact.aar`. This module is **not a direct dependency** of `:app`.

## 3. AAR-Based Dependency Workflow
This project employs a specific build workflow to simulate a real-world library consumption model.

> **Note:** The `:features` modules **are part of this project**, but the `:app` and `:core:data` modules consume their compiled **AAR outputs**, not the modules directly.

### 3.1. Build & Consumption Logic
- A Gradle build (e.g., `./gradlew assembleDebug`) will automatically trigger the compilation of the `:features` modules first.
- A custom build logic will be required to copy the resulting AAR files from their default build locations (e.g., `features/countries-data-sdk/build/outputs/aar/`) to a centralized directory (e.g., a root `build/outputs/aar/` directory).
- The `:app` and `:core:data` modules will then consume these AARs as file dependencies. Gradle's task dependency graph will ensure this happens in the correct order.

### 3.2. Public APIs & Responsibilities
- **`countries-data-sdk`**:
    - **Public API (Hilt-free):** Provides a `CountriesClient` via a `CountriesSdk.create(...)` factory.
    - **Responsibility:** `:core:data` must **wrap** this client, normalize its errors to `AppError`, and map DTOs to Domain models.
- **`countries-ui-artifact`**:
    - **Public API (stateless, Hilt-free):** Provides `@Composable` functions like `CountriesSearchScreen`.
    - **Constraints:** No navigation, no repository access. Must accept an injected `ImageLoader` for images.

### Acceptance Criteria
- The project must have six modules in `settings.gradle.kts`.
- A single Gradle command must successfully build the feature AARs and then build the main app that consumes them.
- The dependency graph must be enforced as described.

*(The remaining sections: Core Layers, Version Catalog, Catalog Reading Protocol, Compliance Rules, and Memory Bank Sync are preserved exactly as in your provided file, as they are correct and valuable.)*

## 4. Core Layers (behavioral details)
- **App / Navigation (`:app`)**: Hosts `MainActivity` and `MainNavHost`. A ViewModel per destination (`hiltViewModel()`) depends **only on use cases**. It maps `AppError → UiError` (message + retry action). Secrets live in `:app` and are passed to the SDK via `NetworkConfig`.
- **Domain (`:core:domain`)**: Contains `Country`, `CountrySummary` entities, `CountriesRepository` contracts, and single-purpose Use Cases (`RefreshAllCountries`, `ObserveCountries`, etc.). Returns `Result<T, AppError>`.
- **Data (`:core:data`)**: Implements repositories. **Wraps `CountriesClient`** from the SDK. Applies domain-facing policies (refresh cadence, TTL, DTO→Domain mapping). If a DB is needed outside the SDK, it defines the `CountryEntity`, `CountryDao`, and `AppDatabase`.
- **Common (`:core:common`)**: Provides `Result/Either`, the `AppError` sealed class (`NetworkError`, `ServerError(code,body)`, `NotFound`, `SerializationError`, `Timeout`, `UnknownError`), Hilt Qualifiers (`@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`), and shared utilities.

## 5. Version Catalog Enforcement
1.  **Single Source of Truth**: All dependency versions MUST be read from `gradle/libs.versions.toml`. No hardcoded versions.
2.  **Key Usage**: Use the catalog keys exactly as they are defined.
3.  **New Libraries**: Propose and validate any new libraries to ensure compatibility. Get explicit approval before modifying the catalog.
4.  **Compose BOM**: Use the Compose BOM (`compose-bom`) to manage Compose library versions.
5.  **Tooling**: Do not modify AGP/Kotlin/Compose/JDK versions.
6.  **Consistency**: All modules must use the version catalog.

## 6. Catalog Reading Protocol (Act Mode)
Run a single external command and return **only** its stdout lines (no extra text).

**Command:** `awk`
**Arguments (2 total, in order):**
1. `/^\[versions\]/{b=1;next} /^\[/{if(b)exit} b && /^[[:space:]]*[A-Za-z0-9_.-]+[[:space:]]*=/ {gsub(/"/,""); sub(/^[[:space:]]*/,""); print "- "$0}`
2. `gradle/libs.versions.toml`

**Behavior contract:**
- Treat argument #1 as the entire awk program.
- Treat argument #2 as the path to the version catalog file.
- Print exactly the transformed key/value lines from the `[versions]` block.

## 7. Compliance & Execution Rules
1.  **Preview Docs**: Any change to `memory-bank/*.md` must be previewed (diff) and approved before writing.
2.  **No Improvisation**: Do not invent endpoints, routes, module names, or dependencies.
3.  **Architecture Review**: Strictly adhere to the allowed dependency graph.
4.  **Step-by-step**: Work in atomic steps. After each approved step, run `./gradlew :app:assembleDebug` and `./gradlew test`, show diffs, and update `memory-bank/activeContext.md` + `progress.md`.
5.  **Secrets**: Never hardcode API keys. Inject them from `:app` into the SDK. Never commit secrets to VCS.

## 8. Memory Bank Sync (systemPatterns.md)
**Source of truth:** this file (`.clinerules/android-rules.md`).
**Target file:** `memory-bank/systemPatterns.md`.

### Canonical Content for System Patterns

#### ViewModel Policy
- One ViewModel per navigation destination.
- ViewModels depend only on **UseCases** (no direct Data/Network).
- ViewModels receive coroutine **dispatchers via Hilt** (`@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`). **Do not** use `Dispatchers.IO` directly.
- Expose immutable `StateFlow<UiState>`; keep `MutableStateFlow` internal.
- Collect in UI with `collectAsStateWithLifecycle`; when adapting flows in VM, prefer `stateIn(SharingStarted.WhileSubscribed(5000))`.

#### Error Mapping Path
- Data/Network layer converts Retrofit/OkHttp/serialization/IO failures to **AppError** (sealed).
- Domain layer returns `Result`/`Either<AppError, T>` without re-wrapping.
- App layer maps **AppError → UiError** (user-facing message + optional **retry** action).
- Log details (e.g., with Timber) in the Data layer; **never** expose raw exceptions/stacktraces to the UI.

### Sync Rules (Plan → Act)
1. **Plan Mode (preview-only):** Generate a **unified diff** that updates `memory-bank/systemPatterns.md` to contain **exactly** the **ViewModel Policy** and **Error Mapping Path** sections.
2. **Act Mode (write):** Apply the previewed diff only after explicit approval.
3. **Idempotency:** Repeated executions should not produce changes if the file already matches the canonical content.
