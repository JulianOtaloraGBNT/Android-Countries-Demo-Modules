# .clinerules/android-rules.md

## 1. Project Overview & Core Principles
- **Stack**: Kotlin (null-safety), Coroutines & Flow, Jetpack Compose (Material 3), Navigation Compose, Hilt (DI), Retrofit, OkHttp, kotlinx.serialization, **Room (inside the SDK)**.
- **Architecture**: Multi-project Gradle with Clean Architecture. Part of the codebase is **consumed as AARs** (data SDK and UI artifact).
- **Core Principles**:
  - **Clean Architecture**: App → Domain ← Data. KISS & SOLID (SRP, OCP, LSP, ISP, DIP).
  - **Composition over Inheritance**: especially for UI and modular design.
  - **Framework-agnostic Domain**: `:core:domain` has **no** Android/Retrofit/Room imports.
  - **Strict Call Graph**: `UI(Screen) → ViewModel → UseCase → Repository → DataSource(SDK)`. Never skip layers.
  - **Dependency Injection**: Hilt + constructor injection; the **App** is the *composition root*.
  - **Lifecycle-aware Flow**: use `collectAsStateWithLifecycle` in UI.
  - **Consistent Error Handling**: network/IO errors → `AppError` → `UiError`.
  - **Local-first**: Room lives in the **SDK** (source of truth) with TTL-based refresh policy.

---

## 2. Modules, Namespaces & Dependency Graph

### 2.1 Modules & Namespaces
- `:app`
  - **namespace / applicationId**: `com.julianotalora.countriesdemo`
  - **Role**: presentation and navigation; Hilt composition root; wiring of repositories/use cases; consumes the UI artifact (AAR).
- `:core:common`
  - **namespace**: `com.julianotalora.core.common`
  - **Role**: pure utilities; `Result/Either`, `AppError` (sealed), dispatcher qualifiers, extensions.
- `:core:domain`
  - **namespace**: `com.julianotalora.core.domain`
  - **Role**: entities, **repository contracts**, **use cases (interfaces + Impl)**; no Android.
- `:core:data`
  - **namespace**: `com.julianotalora.core.data`
  - **Role**: **repository implementations**; wraps the SDK (`CountriesClient`), maps DTO→Domain, normalizes errors to `AppError`. **No Room** here (Room is inside the SDK).
- `:features:countries-data-sdk` (AAR producer)
  - **namespace**: `com.julianotalora.features.countriesdatasdk`
  - **Role**: self-contained SDK (Retrofit/OkHttp/Serialization/Room); defines `SDK_BASE_URL` internally, TTL and DTO↔Entity mapping; exposes a minimal API.
- `:features:countries-ui-artifact` (AAR producer)
  - **namespace**: `com.julianotalora.features.countriesuiartifact`
  - **Role**: **stateless** Compose library (components + full views). **Includes Coil**; no data access, no Hilt.

### 2.2 Allowed / Forbidden Dependencies
- **Allowed**
  - `:app → :core:domain, :core:common, :core:data`
  - `:core:data → :core:domain, :core:common`
  - `:core:domain → :core:common`
- **Forbidden**
  - `:core:domain → :core:data` or `:core:domain → :app`
  - any `:core:* → :app`
  - The **App does not touch the SDK** directly (it goes through `:core:data`).

---

## 3. AAR Workflow (Debug AARs for local consumption)

### 3.1 AAR outputs (feature producers)
- `:features:countries-data-sdk` produces `countries-data-sdk-debug.aar`
- `:features:countries-ui-artifact` produces `countries-ui-artifact-debug.aar`
- They are consumed **as binaries** (not as project modules) by `:core:data` (SDK) and `:app` (UI).

### 3.2 Root task to assemble & copy AARs
Create a **root** task that:
1) Assembles both `:features` debug AARs
2) Copies the `.aar` files to `${rootDir}/libs/`

(Example in root `build.gradle.kts`, expressed as pseudo-code with explicit paths)

    tasks.register<Copy>("prepareFeatureAarsDebug") {
      dependsOn(
        ":features:countries-data-sdk:assembleDebug",
        ":features:countries-ui-artifact:assembleDebug"
      )
      from(layout.projectDirectory.dir("features/countries-data-sdk/build/outputs/aar")) {
        include("*debug*.aar")
        rename { "countries-data-sdk-debug.aar" }
      }
      from(layout.projectDirectory.dir("features/countries-ui-artifact/build/outputs/aar")) {
        include("*debug*.aar")
        rename { "countries-ui-artifact-debug.aar" }
      }
      into(layout.projectDirectory.dir("libs"))
    }

In `:app/build.gradle.kts` and `:core:data/build.gradle.kts`:

    tasks.named("preBuild") {
      dependsOn(":prepareFeatureAarsDebug")
    }

### 3.3 Consumption in dependents
- `:app`:

  dependencies {
  implementation(files("$rootDir/libs/countries-ui-artifact-debug.aar"))
  implementation(project(":core:domain"))
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  // If consuming the UI artifact via files(.aar), also add Coil for classpath:
  // (UI artifact uses Coil internally; the app doesn't call Coil APIs)
  implementation(libs.coil2)
  // Compose BOM, Hilt, Navigation, testing from the catalog...
  }

- `:core:data`:

  dependencies {
  implementation(files("$rootDir/libs/countries-data-sdk-debug.aar"))
  implementation(project(":core:domain"))
  implementation(project(":core:common"))
  // kapt(libs.hilt.compiler) if you annotate implementations with @Inject
  }

Note: if you switch to `mavenLocal()` publishing for the feature AARs, transitive metadata will pull Coil automatically for the UI artifact, and you can remove the explicit Coil line from `:app`.

---

## 4. DI Plan (Hilt in :app; repositories in Data; SDK provided from App)
- `:app/di/CommonModule.kt`
  - Provide coroutine `Dispatchers` with qualifiers (`@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`).
- `:app/di/SdkModule.kt`
  - Provide `CountriesClient` from the SDK:
    - Receives `@ApplicationContext context`
    - Reads the API key from `BuildConfig.COUNTRIES_API_KEY`
    - Calls `CountriesSdk.create(context, NetworkConfig(apiKey = BuildConfig.COUNTRIES_API_KEY))`
- `:app/di/RepositoryModule.kt`
  - `@Binds` from `CountriesRepository` → `CountriesRepositoryImpl` (impl in `:core:data`).
- `:app/di/UseCaseModule.kt`
  - `@Binds` for **each** use-case interface → its `Impl` (both classes live in `:core:domain`).
- `:core:data`:
  - Keep classes `@Inject constructor` (no Hilt modules here).
- `:core:common`:
  - Only qualifiers/utilities (no Hilt modules, JVM-only).

---

## 5. Domain Design (Query/Command; interfaces + Impl)

### 5.1 Features
- **Features**: `countries`, `details`

### 5.2 Structure (per feature)
- `:core:domain/countries/`
  - `model/` → `Country.kt`, `CountrySummary.kt`
  - `repository/` → `CountriesRepository.kt` (interface)
  - `usecase/`
    - `query/`
      - `ObserveCountriesUseCase.kt` + `ObserveCountriesUseCaseImpl.kt`
      - `SearchCountriesUseCase.kt` + `SearchCountriesUseCaseImpl.kt`
      - `GetCountryDetailsUseCase.kt` + `GetCountryDetailsUseCaseImpl.kt`
    - `command/`
      - `RefreshAllCountriesUseCase.kt` + `RefreshAllCountriesUseCaseImpl.kt`

### 5.3 Conventions
- **Always** interface + `Impl` with suffix `XxxYyyUseCaseImpl`.
- Use cases return `Result<T>` / `Either<AppError, T>` (types in `:core:common`).
- **OCP/CQRS**: adding new use cases does not break existing ones.

---

## 6. Data Design (SDK wrapper, mapping, error normalization, TTL)
- `:core:data/countries/repository/CountriesRepositoryImpl.kt`
  - Receives `CountriesClient` (injected via `:app/di/SdkModule`).
  - Implements `CountriesRepository`.
  - **Maps DTO→Domain** (put mappers under `:core:data/.../mapper`).
  - **Normalizes errors**: `SdkError` → `AppError` (under `:core:data/.../error`).
  - **Policies**: Data decides **when** to call `refresh*` on the SDK (SDK enforces TTL internally).

- **Search normalization**:
  - Helper in `:core:common/extensions/StringExt.kt`: `toSearchNorm()` (lowercase + diacritic folding).

---

## 7. SDK Design (`:features:countries-data-sdk`) — Room + Network

### 7.1 Public API (only what’s needed)
- `data class NetworkConfig(apiKey: String /* + optional: timeoutsMs, extra headers */)`
- `sealed interface SdkError { ... }`  (public so Data can map to `AppError`)
- `data class CountryDto( ... )` (public so Data can map to Domain)
- `interface CountriesClient`
  - `fun observeAll(): Flow<List<CountryDto>>`
  - `fun observeSearch(query: String): Flow<List<CountryDto>>`   // normalize internally
  - `suspend fun refreshAll(force: Boolean = false)`
  - `suspend fun refreshSearch(query: String, force: Boolean = false)`
  - `suspend fun getById(cca3: String): CountryDto?`
- `object CountriesSdk`
  - `fun create(appContext: Context, config: NetworkConfig): CountriesClient`

> Everything else in the SDK is **internal**.

### 7.2 Base URL & API key
- **Base URL**: `BuildConfig.SDK_BASE_URL` defined in the SDK module’s `defaultConfig` (e.g., `"https://restcountries.com/"`).
- **API key**: the app provides it via `NetworkConfig(apiKey = BuildConfig.COUNTRIES_API_KEY)`.

### 7.3 Room inside the SDK
- **AppDatabase (internal)** with:
  - `CountryEntity` (persist **all** relevant fields from Rest Countries v3.1).
  - `RefreshStateEntity(key: String, lastUpdatedMillis: Long)` for TTL bookkeeping.
- **TypeConverters (internal)** using kotlinx.serialization for lists/maps/nested objects.
- **DAO (internal)**:
  - `observeAll()`, `observeSearch(q)`, `getById(id)`, and mass upsert operations.

### 7.4 `CountryEntity` fields (main set)
- Keys: `cca2`, `cca3` (PK), `ccn3`, `cioc`
- `nameCommon`, `nameOfficial`, `nativeName` (map)
- `region`, `subregion`, `continents` (list), `languages` (map)
- `capital` (list), `population`, `area`, `latlng` (list)
- `timezones` (list), `borders` (list), `tld` (list)
- `currencies` (map), `idd` (obj), `maps` (obj), `flags` (obj), `coatOfArms` (obj)
- `altSpellings` (list), `startOfWeek`, etc.
- `searchName` (normalized and **indexed**)
- `updatedAtMillis`

### 7.5 Local/Remote flow (TTL)
- `observe*` read **Room** only.
- `refreshAll(force)`:
  - If `force` or TTL expired → network (`/v3.1/all`) → DTO→Entity → upsert → update `RefreshStateEntity`.
- `refreshSearch(query)`:
  - Normalize `query`; if `observeSearch` is empty and `query.length ≥ 2` → `/v3.1/name/{q}` → upsert.

### 7.6 Error mapping (SDK)
- Map OkHttp/Retrofit/Serialization/IO → `SdkError` (`Network`, `Timeout`, `Http(code,body)`, `Serialization`, `Unknown`).
- **Never** expose raw stacktraces.

---

## 8. App Module Structure (UI + VM + Nav + DI)

Recommended layout under `:app/src/main/java/com/julianotalora.countriesdemo/`:

- `di/`
  - `CommonModule.kt` (dispatchers)
  - `SdkModule.kt` (CountriesClient + apiKey)
  - `RepositoryModule.kt` (bind repositories)
  - `UseCaseModule.kt` (bind use cases)
- `navigation/`
  - `MainNavHost.kt`, `BaseDestination.kt`
- `ui/`
  - `countries/`
    - `navigation/` → `CountriesDestination.kt`
    - `view/` → `CountriesSearchScreen.kt` (orchestrates the stateless artifact)
    - `viewmodel/` → `CountriesSearchViewModel.kt`
  - `details/`
    - `navigation/` → `DetailsDestination.kt`
    - `view/` → `DetailsScreen.kt`
    - `viewmodel/` → `DetailsViewModel.kt`
- `MainActivity.kt`

**ViewModel policy**:
- One VM per destination; depends **only** on UseCases.
- Expose immutable `StateFlow<UiState>`; keep `MutableStateFlow` internal.
- Adapt `Flow` with `stateIn(SharingStarted.WhileSubscribed(5000))`.

**Using the UI artifact**:
- The app composes the artifact’s view (`CountriesSearchView` / `CountryDetailsView`) passing `state` and `onEvent`.
- Navigation is decided by the app (the artifact **does not** know routes).

---

## 9. Common Module
- `Result/Either`, `AppError` (sealed: `NetworkError`, `ServerError(code, body)`, `NotFound`, `SerializationError`, `Timeout`, `UnknownError`).
- Qualifiers: `@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`.
- `extensions/` (pure shared functions, e.g., `String.toSearchNorm()`).
- JVM-only: no Android, no Hilt modules, no Room.

---

## 10. Build Configs & Secrets
- `:features:countries-data-sdk`:
  - `defaultConfig`:
    - `buildConfigField("String", "SDK_BASE_URL", "\"https://restcountries.com/\"")`
- `:app`:
  - `defaultConfig`:
    - `buildConfigField("String", "COUNTRIES_API_KEY", "\"${project.findProperty("COUNTRIES_API_KEY") ?: ""}\"")`
  - Define `COUNTRIES_API_KEY` in `~/.gradle/gradle.properties` or `local.properties` (never commit to VCS).

---

## 11. Version Catalog & Required Keys (libs.versions.toml)
- **Plugins** (`[plugins]`):
  - `android-application`, `android-library`, `kotlin-android`, `kotlin-jvm`, `kotlin-kapt`, `kotlin-compose`, `hilt`
- **Versions** (`[versions]`):
  - `agp`, `kotlin`, `composeBom`, `coreKtx`, `lifecycleRuntimeKtx`, `lifecycleCompose`, `activityCompose`, `navigationCompose`,
    `retrofit`, `okhttp`, `serialization`, `room`, `hilt`, `coroutines`, `coil2`, `turbine`, `mockwebserver`, `junit`, `junitVersion`, `espressoCore`
- **Libraries** (`[libraries]`):
  - `androidx-core-ktx`, `androidx-lifecycle-runtime-ktx`, `androidx-lifecycle-runtime-compose`,
    `androidx-activity-compose`, `androidx-navigation-compose`,
    `retrofit`, `retrofit-kotlinx-serialization-converter`, `okhttp`, `okhttp-logging-interceptor`,
    `kotlinx-serialization-json`, `room-runtime`, `room-ktx`, `room-compiler`,
    `hilt-android`, `hilt-compiler`,
    `kotlinx-coroutines-core`, `kotlinx-coroutines-android`,
    `coil2`, `junit`, `androidx-junit`, `androidx-espresso-core`, `turbine`, `mockwebserver`

Rule: every module uses the catalog (no hardcoded versions).

---

## 12. Catalog Reading Protocol (Act Mode)
Run **one** external command and return **only** its stdout (no extra text).

- **Command**: `awk`
- **Arg #1 (awk program)**:
  `/^\[versions\]/{b=1;next} /^\[/{if(b)exit} b && /^[[:space:]]*[A-Za-z0-9_.-]+[[:space:]]*=/ {gsub(/"/,""); sub(/^[[:space:]]*/,""); print "- "$0}`
- **Arg #2 (file path)**:
  `gradle/libs.versions.toml`

**Behavior contract**:
- Treat arg #1 as the complete awk program.
- Treat arg #2 as the catalog path.
- Print exactly the `key = value` lines from the `[versions]` block as bullet lines.

---

## 13. Images (artifact & app)
- **UI artifact**: **includes Coil** and exposes components like `FlagImage`, `CountryCard`, etc.
- **App**: does **not** use Coil APIs; it composes the artifact’s components.  
  If consuming the artifact via flat `.aar`, add `implementation(libs.coil2)` in `:app` to satisfy classpath; with `mavenLocal()` publishing, transitive metadata will handle it.

---

## 14. Compliance & Execution Rules
1) **No improvisation**: do not invent endpoints, routes, module names, or dependencies.
2) **Graph**: respect Allowed/Forbidden dependencies.
3) **Step-by-step**: after each approved change, run `./gradlew :app:assembleDebug` and `./gradlew test`, show diffs, and update the memory bank.
4) **Secrets**: never in VCS (use `gradle.properties`/`local.properties`).
5) **AARs**: use `prepareFeatureAarsDebug` + consume from `libs/`.
6) **internal in SDK**: everything not part of the public API must be `internal`.

---

## 15. Memory Bank Sync (systemPatterns.md)
- **Source of truth**: this file (`.clinerules/android-rules.md`)
- **Target**: `memory-bank/systemPatterns.md`

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
- App layer maps **AppError → UiError** (user-facing message with optional **retry** action).
- Log details (e.g., with Timber) in the Data layer; **never** expose raw exceptions/stacktraces to the UI.

### Sync Rules (Plan → Act)
1) **Plan Mode (preview-only)**: generate a unified diff so `systemPatterns.md` contains exactly the sections above.
2) **Act Mode (write)**: apply the diff after approval.
3) **Idempotency**: if it already matches, no changes should be produced.

---

## 16. Directory layouts (summary per module)

- `:app (com.julianotalora.countriesdemo)`
  - `di/` → `CommonModule.kt`, `SdkModule.kt`, `RepositoryModule.kt`, `UseCaseModule.kt`
  - `navigation/` → `MainNavHost.kt`, `BaseDestination.kt`
  - `ui/`
    - `countries/`
      - `navigation/` → `CountriesDestination.kt`
      - `view/` → `CountriesSearchScreen.kt`
      - `viewmodel/` → `CountriesSearchViewModel.kt`
    - `details/`
      - `navigation/` → `DetailsDestination.kt`
      - `view/` → `DetailsScreen.kt`
      - `viewmodel/` → `DetailsViewModel.kt`
  - `MainActivity.kt`

- `:core:common (com.julianotalora.core.common)`
  - `result/` → `Result.kt`, `Either.kt`
  - `error/` → `AppError.kt`
  - `coroutine/` → `DispatchersQualifiers.kt` (`@IODispatcher`, etc.)
  - `extensions/` → `StringExt.kt`, `FlowExt.kt`

- `:core:domain (com.julianotalora.core.domain)`
  - `countries/`
    - `model/` → `Country.kt`, `CountrySummary.kt`
    - `repository/` → `CountriesRepository.kt`
    - `usecase/`
      - `query/` → `ObserveCountriesUseCase(.kt)`, `ObserveCountriesUseCaseImpl(.kt)`, `SearchCountriesUseCase(.kt)`, `SearchCountriesUseCaseImpl(.kt)`, `GetCountryDetailsUseCase(.kt)`, `GetCountryDetailsUseCaseImpl(.kt)`
      - `command/` → `RefreshAllCountriesUseCase(.kt)`, `RefreshAllCountriesUseCaseImpl(.kt)`

- `:core:data (com.julianotalora.core.data)`
  - `countries/`
    - `repository/` → `CountriesRepositoryImpl.kt`
    - `mapper/` → `CountryMappers.kt`
    - `error/` → `SdkErrorMapping.kt`
  - `di/` (optional if you later encapsulate Data bindings)

- `:features:countries-data-sdk (com.julianotalora.features.countriesdatasdk)`
  - `api/` → `CountriesSdk.kt`, `CountriesClient.kt`, `NetworkConfig.kt`, `CountryDto.kt`, `SdkError.kt`
  - `internal/db/` → `AppDatabase.kt`, `CountryEntity.kt`, `CountryDao.kt`, `Converters.kt`, `RefreshStateEntity.kt`
  - `internal/net/` → `RestCountriesApi.kt`, `OkHttpFactory.kt`, `RetrofitFactory.kt`, `ErrorMapper.kt`
  - `internal/mapper/` → `DtoEntityMappers.kt`
  - `internal/util/` → `SearchNormalizer.kt`, `TtlPolicy.kt`

- `:features:countries-ui-artifact (com.julianotalora.features.countriesuiartifact)`
  - `ui/components/` → `CountryCard.kt`, `FlagImage.kt` (Coil), `SearchBar.kt`, `EmptyState.kt`, `ErrorState.kt`, `LoadingIndicator.kt`
  - `ui/views/` → `CountriesSearchView.kt`, `CountryDetailsView.kt`
  - `model/` → `CountriesState.kt`, `CountriesEvent.kt`

---

## 17. Testing Strategy (summary)
- **Domain**: test `*UseCaseImpl` with fake repositories.
- **Data**: test `CountriesRepositoryImpl` with a fake/mocked `CountriesClient` (SDK contract).
- **SDK**: test with `MockWebServer` (API key header, TTL, error mapping) + Room in-memory.
- **UI**: minimal snapshot/interaction (optional).
- **Coroutines**: `kotlinx-coroutines-test`, `Turbine`.