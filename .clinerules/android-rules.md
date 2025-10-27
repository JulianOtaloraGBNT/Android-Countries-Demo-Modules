## Project Overview
- Kotlin (null-safety), Coroutines & Flow
- Jetpack Compose (Material 3), Navigation-Compose
- Hilt (DI with dispatcher qualifiers)
- Room (SQLite) as local cache
- Retrofit + OkHttp + kotlinx.serialization for networking
- Clean Architecture + SOLID, MVVM, Single-Activity
- Logging (Timber), Result/Either in `:core:common`
- Testing: JUnit, kotlinx-coroutines-test, Turbine, MockWebServer, Room in-memory

## Core Principles
1. **Clean Architecture** with KISS and SOLID
    - SRP, OCP, LSP, ISP, DIP applied to modules/classes.
2. **Composition over inheritance** (UI and module design).
3. **Framework-agnostic business logic** (pure Domain; no Android/Retrofit/Room imports).
4. **Strict call graph:**  
   MainActivity/MainNavHost → Screen (Compose) → ViewModel → UseCase → Repository → Data Sources (DAO/API).  
   *Never* skip layers or call across layers.
5. **Dependency Injection** via Hilt and constructor injection.
6. **Pure entities** (domain models without side effects).
7. **One ViewModel per screen** (destination). Leaf UI components are stateless (state hoisting).
8. **Lifecycle-aware Flow consumption** in screens using `collectAsStateWithLifecycle`.
9. **Consistent error handling** with a base error type and mappers per layer.
10. **Local-first + controlled sync** (UI backed by Room; remote refresh by policy).

## Core Layers
1. **Navigation / App (`:app`)**
    - **Responsibility:** Navigation orchestration & presentation glue.
    - **Details:**
        - Single-Activity with **MainActivity** hosting **MainNavHost()**.
        - `BaseDestination` (sealed) with routes/args (`Search`, `Details/{id}`).
        - **One ViewModel per destination** (`hiltViewModel()`), depends **only on UseCases**.
        - Map `AppError → UiError` (messages/Retry actions).
        - **API key** lives in `:app` (secrets/env) and is injected into `:core:network`.

2. **UI (`:core:ui`)**
    - **Responsibility:** Stateless Compose screens/components.
    - **Details:**
        - One **full screen** implemented here (e.g., `CountryDetailsScreen(state, onBack)`).
        - The other (Search) is composed in `:app` using exported components (`SearchBar`, `CountryCard`, `EmptyState`, `ErrorState`, `LoadingIndicator`).
        - No Data access, no navigation knowledge.

3. **Domain (`:core:domain`)**
    - **Responsibility:** Rules and contracts (pure Kotlin).
    - **Subfolders:**
        - **Entities:** `Country`, `CountrySummary`.
        - **Repositories (interfaces):** `CountriesRepository`.
        - **UseCases:** `RefreshAllCountries`, `ObserveCountries`, `SearchCountries`, `GetCountryDetails` (1 class/file).
    - **Details:** Return `Result<T>` (or `Either<AppError,T>`). No Android/Retrofit/Room imports.

4. **Data (`:core:data`)**
    - **Responsibility:** Repository implementations, mappers, Room orchestration.
    - **Details:** `CountryEntity`, `CountryDao`, `AppDatabase`, DTO↔Entity↔Domain mappers, `CountriesRepositoryImpl`.

5. **Network (`:core:network`)**
    - **Responsibility:** HTTP client setup and API contracts.
    - **Details:** Retrofit/OkHttp, interceptors (debug logging and **auth with API key** injected from `:app`), `RestCountriesApi`.

6. **Common (`:core:common`)**
    - **Responsibility:** Cross-cutting utilities.
    - **Details:** `Result/Either`, **`AppError` (sealed)** with `NetworkError`, `ServerError(code,body)`, `NotFound`, `SerializationError`, `Timeout`, `UnknownError`; qualifiers `@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`; helpers (search normalization).

## Infrastructure & Utilities
1. **Clients (`:core:network`)**
    - Retrofit + kotlinx.serialization converter; OkHttp with timeouts and interceptors.
    - **Base URL** defined in `:core:network`; **API key** injected from `:app`.
    - **Version lock:** Retrofit/OkHttp/kotlinx-serialization artifacts **must** use versions from `gradle/libs.versions.toml`; if a key is missing, propose it and wait for approval.

2. **Database (`:core:data`)**
    - Room DB; indices on `searchName`/`nameCommon`; DAO returns Flows; in-memory tests.

3. **Coroutines**
    - ViewModels receive **injected dispatchers** (Hilt). I/O via `withContext(IODispatcher)`.
    - State with `StateFlow` + `stateIn(SharingStarted.WhileSubscribed(5000))`.

## Configuration Management
1. **API Key & Base URL**
    - API key in `:app` (secrets/env) → injected into `:core:network` interceptor (Hilt).
    - Base URL configured in `:core:network` (BuildConfig/DI). No secrets in VCS.

2. **Build Types/Flavors**
    - Network/DB configs via DI; avoid cross-module BuildConfig coupling.

## Data Access & Sync Policy
1. **Local-first**
    - UI observes Room (Flow) and Compose consumes with `collectAsStateWithLifecycle`.

2. **App start**
    - DB empty → fetch `/v3.1/all` → upsert → Room emits.
    - DB has data → show immediately; refresh if **TTL** (~24h) is expired (background).

3. **Real-time search (≥ 2 chars)**
    - `< 2` → `flowAllOrdered()`; `≥ 2` → `flowSearch(queryNorm LIKE %q%)`.
    - **Remote fallback** `/v3.1/name/{q}` only if local emits empty and `q.length ≥ 2` → upsert.

4. **Upsert policy**
    - Upsert by `cca3`; map **only** fields required by each use case.

5. **Pull-to-refresh**
    - `RefreshAllCountries(force=true)`; UI remains local-first.

## Testing Strategy
1. **ViewModels**
    - `SearchViewModel`: debounce (~300ms), min length (≥2), success/empty/error, refresh.
    - `DetailsViewModel`: success/error (invalid id).
    - `runTest` + `StandardTestDispatcher`; assertions with Turbine.

2. **UseCases**
    - Rules and **AppError propagation** using repository fakes.

3. **Mappers**
    - DTO→Entity and Entity↔Domain (nullability/edge cases).

4. **Repositories (prefer integration)**
    - Room in-memory + MockWebServer (/all, /name/{q}); verify TTL & upsert behavior.

5. **Compose (optional)**
    - Minimal snapshot/interaction tests.

## Naming Conventions
- **Modules:** `:app`, `:core:ui`, `:core:domain`, `:core:data`, `:core:network`, `:core:common`
- **UseCases:** `XxxYyyUseCase` (1 class/file)
- **Repos:** `CountriesRepository` / `CountriesRepositoryImpl`
- **DAOs:** `CountryDao`
- **Entities/DTOs:** `CountryEntity` / `CountryDto`
- **Domain models:** `Country`, `CountrySummary`
- **ViewModels:** `SearchViewModel`, `DetailsViewModel`
- **Screens/Components:** `CountryDetailsScreen`, `SearchScreen`, `CountryCard`, `SearchBar`, `EmptyState`, `ErrorState`, `LoadingIndicator`
- **Errors:** `AppError` (data/domain), `UiError` (presentation)
- **Dispatchers:** `IODispatcher`, `DefaultDispatcher`, `MainDispatcher`

## Versioning Policy
1. **Single source of truth:** `gradle/libs.versions.toml` is the only source of versions. **Do not** upgrade/downgrade without approval.
2. **Use the existing catalog keys only.** If a dependency has no key:
    - Propose adding it (version from the existing catalog if available, or mark as `PENDING_VERSION`) and **wait for approval**.
3. **Compose:** Use **Compose BOM**; do not set versions on Compose artifacts.
4. **Tooling:** Do not modify AGP/Kotlin/Compose/JDK or local SDK config.
5. **Consistency:** All modules must depend on the catalog (no hardcoded versions).
6. **Verification:** After dependency changes, run `:app:assembleDebug` and `test`.
7. **TechContext Mirror:** `memory-bank/techContext.md` **must exactly mirror** catalog versions/artifacts (no invented values).

## Images (Library, SVG, caching)
1. **Library:** **Coil 2.7.0** (compatible with Compose BOM `2024.09.00`).
    - `io.coil-kt:coil-compose:2.7.0`
    - `io.coil-kt:coil-svg:2.7.0`
2. **SVG vs PNG:** prefer SVG for sharpness; fallback to PNG if decoding fails.
3. **ImageLoader & cache:** reuse the **OkHttpClient** from `:core:network` to share disk cache/timeouts/logging.
4. **UI API:** `AsyncImage` (stateless), proper `contentDescription`, loading/error placeholders aligned with `UiError`.
5. **Tests:** at least one case loading a real SVG flag and an error/fallback case.

## Compliance & Execution Rules
1. **Preview required (docs):** Any `memory-bank/*.md` or `README` changes must be presented as a **PREVIEW** for approval before writing to disk.
2. **No improvisation:** Do not invent endpoints, routes, module names, or dependencies. If something is missing, request clarification or propose a minimal delta and wait for approval.
3. **Architecture checks:**
    - Allowed dependency graph:
        - `:app` → `:core:ui`, `:core:domain`, `:core:common`
        - `:core:data` → `:core:domain`, `:core:network`, `:core:common`
        - `:core:ui` → `:core:common`
        - `:core:domain` → `:core:common`
        - `:core:network` → `:core:common`
    - **Forbidden:** `:app` → `:core:data`, and any `:core:ui ↔ :core:data`.
4. **Step-by-step execution:** Work in atomic Steps; after each Step, run `./gradlew :app:assembleDebug` and `./gradlew test`, show diffs, and update `memory-bank/activeContext.md` and `memory-bank/progress.md`.
5. **Secrets:** Never hardcode API keys; inject them from `:app` via DI. Never commit secrets to VCS.