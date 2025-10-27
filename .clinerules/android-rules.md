## Project Overview
- Kotlin (null-safety), Coroutines & Flow
- Jetpack Compose (Material 3), Navigation Compose
- Hilt (DI with dispatcher qualifiers)
- Room (SQLite) as local cache
- Retrofit + OkHttp + kotlinx.serialization for networking
- Clean Architecture + SOLID, MVVM, Single-Activity
- Logging (Timber), Result/Either in `:core:common`
- Testing: JUnit, kotlinx-coroutines-test, Turbine, MockWebServer, Room in-memory

## Core Principles
1. **Clean Architecture** with KISS & SOLID (SRP, OCP, LSP, ISP, DIP).
2. **Composition over inheritance** (UI & module design).
3. **Framework-agnostic Domain** (no Android/Retrofit/Room imports in Domain).
4. **Strict call graph:**  
   `MainActivity/MainNavHost → Screen (Compose) → ViewModel → UseCase → Repository → Data Source (DAO/API)`  
   *Never* skip layers or cross-call.
5. **Dependency Injection** via Hilt & constructor injection.
6. **Pure entities** (side-effect-free domain models).
7. **One ViewModel per destination**; leaf UI components are stateless (state hoisting).
8. **Lifecycle-aware Flow** consumption using `collectAsStateWithLifecycle`.
9. **Consistent error handling** (base error type + layer mappers).
10. **Local-first + controlled sync** (UI backed by Room; remote refresh by policy).

## Core Layers
1. **App / Navigation (`:app`)**
   - Single Activity (`MainActivity`) hosting `MainNavHost()`.
   - `BaseDestination` (sealed) with routes/args (`Search`, `Details/{id}`).
   - One ViewModel per destination (`hiltViewModel()`), depends **only on UseCases**.
   - Map `AppError → UiError` (messages/retry).
   - **API key** lives in `:app` (secrets/env) and is injected into `:core:network`.

2. **UI (`:core:ui`)**
   - Stateless Compose screens/components.
   - One **full screen** here (e.g., `CountryDetailsScreen(state, onBack)`).
   - The other (Search) is composed in `:app` using exported components (`SearchBar`, `CountryCard`, `EmptyState`, `ErrorState`, `LoadingIndicator`).
   - No Data access; no navigation knowledge.

3. **Domain (`:core:domain`)**
   - Rules & contracts (pure Kotlin).
   - Entities: `Country`, `CountrySummary`.
   - Repositories (interfaces): `CountriesRepository`.
   - UseCases: `RefreshAllCountries`, `ObserveCountries`, `SearchCountries`, `GetCountryDetails` (one class per file).
   - Return `Result<T>` / `Either<AppError, T>`.

4. **Data (`:core:data`)**
   - Repository implementations, mappers, Room.
   - `CountryEntity`, `CountryDao`, `AppDatabase`, DTO↔Entity↔Domain mappers, `CountriesRepositoryImpl`.

5. **Network (`:core:network`)**
   - HTTP client setup and API contracts.
   - Retrofit/OkHttp, interceptors (debug logging + **auth with API key** injected from `:app`), `RestCountriesApi`.

6. **Common (`:core:common`)**
   - Cross-cutting utilities.
   - `Result/Either`, **`AppError` (sealed)** with `NetworkError`, `ServerError(code,body)`, `NotFound`, `SerializationError`, `Timeout`, `UnknownError`; qualifiers `@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`; helpers (search normalization).

## Infrastructure & Utilities
- **Network clients (`:core:network`)**: Retrofit + kotlinx.serialization converter; OkHttp with timeouts/interceptors.  
  **Base URL** provided in `:core:network`; **API key** injected from `:app`.
- **Database (`:core:data`)**: Room DB; indices on `searchName`/`nameCommon`; DAOs return `Flow`; in-memory tests.
- **Coroutines**: ViewModels receive injected dispatchers (Hilt). I/O via `withContext(IODispatcher)`. State with `StateFlow` and `stateIn(SharingStarted.WhileSubscribed(5000))`.

## Configuration Management
- **API Key & Base URL**: API key in `:app` (secrets/env) → injected into `:core:network` interceptor (Hilt). Base URL configured in `:core:network` (BuildConfig/DI). No secrets in VCS.
- **Build Types/Flavors**: Network/DB configs via DI; avoid cross-module `BuildConfig` coupling.

## Data Access & Sync Policy
1. **Local-first**: UI observes Room; Composables consume with `collectAsStateWithLifecycle`.
2. **App start**: DB empty → fetch `/v3.1/all` → upsert → Room emits. If DB has data → show immediately; refresh if **TTL** (~24h) expired (background).
3. **Real-time search (≥ 2 chars)**:
   - `< 2` → `flowAllOrdered()`; `≥ 2` → `flowSearch(%queryNorm%)`.
   - **Remote fallback** `/v3.1/name/{q}` only if local emits empty and `q.length ≥ 2` → upsert.
4. **Upsert policy**: Upsert by `cca3`; map **only** fields required by use cases.
5. **Pull-to-refresh**: `RefreshAllCountries(force=true)`; UI remains local-first.

## Testing Strategy
- **ViewModels**: debounce (~300ms), min length (≥2), success/empty/error, refresh; `runTest` + `StandardTestDispatcher`; Turbine assertions.
- **UseCases**: rules + `AppError` propagation with repository fakes.
- **Mappers**: DTO→Entity and Entity↔Domain (nullability/edges).
- **Repositories (prefer integration)**: Room in-memory + MockWebServer (`/all`, `/name/{q}`); verify TTL & upsert.
- **Compose (optional)**: minimal snapshot/interaction tests.

## Naming Conventions
- **Modules:** `:app`, `:core:ui`, `:core:domain`, `:core:data`, `:core:network`, `:core:common`
- **UseCases:** `XxxYyyUseCase` (one class/file)
- **Repos:** `CountriesRepository` / `CountriesRepositoryImpl`
- **DAOs:** `CountryDao`
- **Entities/DTOs:** `CountryEntity` / `CountryDto`
- **Domain models:** `Country`, `CountrySummary`
- **ViewModels:** `SearchViewModel`, `DetailsViewModel`
- **Screens/Components:** `CountryDetailsScreen`, `SearchScreen`, `CountryCard`, `SearchBar`, `EmptyState`, `ErrorState`, `LoadingIndicator`
- **Errors:** `AppError` (data/domain), `UiError` (presentation)
- **Dispatchers:** `IODispatcher`, `DefaultDispatcher`, `MainDispatcher`

## Version Catalog Enforcement
1. **Single source of truth:** read versions **only** from `gradle/libs.versions.toml` (this exact path).
2. **Use existing catalog keys exactly as they appear** (e.g., if the key is `coil2`, use `coil2`; do **not** rename to `coil`).
3. **New libraries for features:**
   - First, validate compatibility and propose versions that **do not conflict** with current catalog.
   - If unavoidable, propose **minimal** version changes and list **all** code/build adjustments required.
   - Wait for explicit approval before modifying the catalog.
4. **Compose:** use Compose BOM; **no explicit versions** on Compose artifacts.
5. **Tooling:** do not modify AGP/Kotlin/Compose/JDK or local SDK config.
6. **Consistency:** all modules must use the catalog (no hardcoded versions).

## TechContext Rendering Rules
- `memory-bank/techContext.md` must render versions as a **bullet list** (no TOML, no code fences).
- The list must **mirror** the catalog keys/values **verbatim** (no renames, no guesses).
- **Do not compute hashes/digests** of the catalog (avoid extra tokens).
- If any required key is missing, list it under **“Missing Catalog Keys (PENDING_VERSION)”** and **STOP** for approval.

## Catalog Reading Protocol (Act Mode, low-token, macOS)
Use **one command per step** and return **only printed lines**. BSD `awk` is strict; avoid reserved words and escape brackets.

    awk '
      /^\[versions\]/ { in_block=1; next }
      /^\[/ && in_block { exit }
      in_block && /^[[:space:]]*[A-Za-z0-9_.-]+[[:space:]]*=/ {
        gsub(/"/,""); sub(/^[[:space:]]*/,""); print "- " $0
      }
    ' gradle/libs.versions.toml

## Images (library & caching)
- Use **Coil** in UI; prefer SVG with PNG fallback.
- Reuse the `OkHttpClient` from `:core:network` for shared cache/timeouts/logging.
- UI API: `AsyncImage` with `contentDescription`, loading/error placeholders aligned with `UiError`.

## Compliance & Execution Rules
1. **Preview required for docs:** any `memory-bank/*.md` changes must be **previewed** and approved before writing.
2. **No improvisation:** do not invent endpoints, routes, module names, or dependencies.
3. **Architecture checks (allowed graph):**
   - `:app` → `:core:ui`, `:core:domain`, `:core:common`
   - `:core:data` → `:core:domain`, `:core:network`, `:core:common`
   - `:core:ui` → `:core:common`
   - `:core:domain` → `:core:common`
   - `:core:network` → `:core:common`
   - **Forbidden:** `:app → :core:data`, and any `:core:ui ↔ :core:data`.
4. **Step-by-step:** work in atomic steps; after each approved step run `./gradlew :app:assembleDebug` and `./gradlew test`, show diffs, and update `memory-bank/activeContext.md` + `progress.md`.
5. **Secrets:** never hardcode API keys; inject from `:app` via DI; never commit secrets to VCS.

## Search Normalization Rules
- Normalize query: lowercase + diacritic folding.
- Persist normalized field (`searchName`) in Room and **index** it.
- DAO queries use `LIKE %queryNorm%` against `searchName`.
- Mappers keep only fields required by use cases.

## CI & Quality Gates
- After each approved step: build `:app:assembleDebug`, run tests, report module graph changes, and update the memory bank.

## Memory Bank Sync (systemPatterns.md)

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
- Log details (e.g., Timber) in Data layer; **never** expose raw exceptions/stacktraces to UI.

### Sync Rules (Plan → Act)
1. **Plan Mode (preview-only):** generate a **unified diff** that updates `memory-bank/systemPatterns.md` to contain exactly the sections **ViewModel Policy** and **Error Mapping Path** shown above (no extra text).
2. **Act Mode (write):** apply the previewed diff only after explicit approval.
3. **No improvisation:** do not introduce additional sections or alter the canonical bullets.
4. **Idempotency:** repeated runs must produce no changes if the file already matches the canonical content.
