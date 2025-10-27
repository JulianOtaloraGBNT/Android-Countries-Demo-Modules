## Project Overview
- Kotlin (null-safety), Coroutines & Flow
- Jetpack Compose (Material 3), Navigation-Compose
- Hilt (DI con qualifiers para dispatchers)
- Room (SQLite) como cache local
- Retrofit + OkHttp + kotlinx.serialization para red
- Clean Architecture + SOLID, MVVM, Single-Activity
- Logging (Timber), Result/Either en `:core:common`
- Testing: JUnit, kotlinx-coroutines-test, Turbine, MockWebServer, Room in-memory

## Core Principles
1. **Clean Architecture** con KISS y SOLID
   - SRP, OCP, LSP, ISP, DIP aplicados a módulos/clases.
2. **Composition over inheritance** (UI y diseño de módulos).
3. **Business logic framework-agnostic** (Domain puro, sin Android/Retrofit/Room).
4. **Strict call graph:**  
   MainActivity/MainNavHost → Screen (Compose) → ViewModel → UseCase → Repository → Data Sources (DAO/API).  
   *Nunca* saltar capas ni cruzar llamadas entre capas.
5. **Dependency Injection** mediante Hilt y constructores.
6. **Pure entities** (modelos de dominio sin efectos colaterales).
7. **Un ViewModel por pantalla** (destino). Componentes de UI son stateless (state hoisting).
8. **Flows lifecycle-aware** en pantallas con `collectAsStateWithLifecycle`.
9. **Error handling consistente** con tipo base y mapeos por capa.
10. **Local-first + sync controlado** (UI desde Room, refresh remoto por reglas).

## Core Layers
1. **Navigation / App (`:app`)**
   - **Responsabilidad:** Orquestación de navegación y presentación.
   - **Detalles:**
      - Single-Activity con **MainActivity** alojando **MainNavHost()**.
      - `BaseDestination` (sealed) con rutas/args (`Search`, `Details/{id}`).
      - **ViewModel por destino** (`hiltViewModel()`), depende **solo de UseCases**.
      - Mapeo `AppError → UiError` (mensajes/Retry).
      - **API key**: vive en `:app` (secrets/env) y se inyecta hacia `:core:network`.
2. **UI (`:core:ui`)**
   - **Responsabilidad:** Pantallas/componentes Compose **stateless**.
   - **Detalles:**
      - Una pantalla **completa** aquí (p.ej. `CountryDetailsScreen(state, onBack)`).
      - La otra (Search) se construye en `:app` usando componentes exportados (`SearchBar`, `CountryCard`, `EmptyState`, `ErrorState`, `LoadingIndicator`).
      - Sin acceso a Data ni conocimiento de navegación.
3. **Domain (`:core:domain`)**
   - **Responsabilidad:** Reglas y contratos (puro Kotlin).
   - **Subcarpetas:**
      - **Entities:** `Country`, `CountrySummary`.
      - **Repositories (interfaces):** `CountriesRepository`.
      - **UseCases:** `RefreshAllCountries`, `ObserveCountries`, `SearchCountries`, `GetCountryDetails` (1 clase/archivo).
   - **Detalles:** Retornan `Result<T>` (o `Either<AppError,T>`). Sin imports de Android/Retrofit/Room.
4. **Data (`:core:data`)**
   - **Responsabilidad:** Implementaciones de repos, mappers y Room.
   - **Detalles:** `CountryEntity`, `CountryDao`, `AppDatabase`, mapeos DTO↔Entity↔Domain, `CountriesRepositoryImpl`.
5. **Network (`:core:network`)**
   - **Responsabilidad:** Cliente HTTP y contratos de API.
   - **Detalles:** Retrofit/OkHttp, interceptores (logging debug y **auth con API key** inyectada desde `:app`), `RestCountriesApi`.
6. **Common (`:core:common`)**
   - **Responsabilidad:** Utilidades transversales.
   - **Detalles:** `Result/Either`, **`AppError` (sealed)** con `NetworkError`, `ServerError(code,body)`, `NotFound`, `SerializationError`, `Timeout`, `UnknownError`; qualifiers `@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`; helpers (normalización de búsqueda).

## Infrastructure & Utilities
1. **Clients (`:core:network`)**
   - Retrofit + converter kotlinx.serialization; OkHttp con timeouts e interceptores.
   - **Base URL** en `:core:network`; **API key** inyectada desde `:app`.
2. **Database (`:core:data`)**
   - Room DB; índices en `searchName`/`nameCommon`; Flows en DAO; tests in-memory.
3. **Coroutines**
   - ViewModels reciben **dispatchers inyectados** (Hilt). I/O con `withContext(IODispatcher)`.
   - Estado con `StateFlow` + `stateIn(SharingStarted.WhileSubscribed(5000))`.

## Configuration Management
1. **API Key & Base URL**
   - API key en `:app` (secrets/env) → interceptor de `:core:network` (Hilt).
   - Base URL configurada en `:core:network` (BuildConfig/DI). No secrets en VCS.
2. **Build Types/Flavors**
   - Configs de red/DB **por DI**; evitar acoplar BuildConfig entre módulos.

## Data Access & Sync Policy
1. **Local-first**
   - UI observa Room (Flow) y Compose consume con `collectAsStateWithLifecycle`.
2. **Arranque**
   - DB vacía → fetch `/v3.1/all` → upsert → Room emite.
   - DB con datos → mostrar de inmediato; refrescar si **TTL** (~24h) vencido (background).
3. **Búsqueda realtime (≥2 chars)**
   - `<2` → `flowAllOrdered()`; `≥2` → `flowSearch(queryNorm LIKE %q%)`.
   - **Fallback remoto** `/v3.1/name/{q}` solo si local emite vacío y `q.length ≥ 2` → upsert.
4. **Upsert policy**
   - Upsert por `cca3`; mapear **solo** campos requeridos por caso de uso.
5. **Pull-to-refresh**
   - `RefreshAllCountries(force=true)`; UI sigue local-first.

## Testing Strategy
1. **ViewModels**
   - `SearchViewModel`: debounce (~300ms), min-length (≥2), success/empty/error, refresh.
   - `DetailsViewModel`: success/error (id inválido).
   - `runTest` + `StandardTestDispatcher`, asserts con Turbine.
2. **UseCases**
   - Reglas y **propagación de AppError** con repos fakes.
3. **Mappers**
   - DTO→Entity y Entity↔Domain (nullables/bordes).
4. **Repositories (preferente integración)**
   - Room in-memory + MockWebServer (/all, /name/{q}); verificar TTL y upsert.
5. **Compose (opcional)**
   - UI snapshot/interaction mínimos.

## Naming Conventions
- **Módulos:** `:app`, `:core:ui`, `:core:domain`, `:core:data`, `:core:network`, `:core:common`
- **UseCases:** `XxxYyyUseCase` (1 clase/archivo)
- **Repos:** `CountriesRepository` / `CountriesRepositoryImpl`
- **DAOs:** `CountryDao`
- **Entities/DTOs:** `CountryEntity` / `CountryDto`
- **Domain models:** `Country`, `CountrySummary`
- **ViewModels:** `SearchViewModel`, `DetailsViewModel`
- **Screens/Components:** `CountryDetailsScreen`, `SearchScreen`, `CountryCard`, `SearchBar`, `EmptyState`, `ErrorState`, `LoadingIndicator`
- **Errores:** `AppError` (data/domain), `UiError` (presentación)
- **Dispatchers:** `IODispatcher`, `DefaultDispatcher`, `MainDispatcher`

## Versioning Policy
1. **Source of truth:** `gradle/libs.versions.toml`.
2. **Baseline:** usar las versiones **ya presentes** en el proyecto (sin subir/bajar).
3. **Compose:** usar **Compose BOM**; sin versiones explícitas en artefactos Compose.
4. **Consistencia:** todos los módulos referencian el **catálogo**.
5. **Herramientas:** no modificar AGP/Kotlin/Compose/JDK ni SDK local.
6. **Nuevas libs:** añadir al catálogo respetando baseline (sin “arrastrar” upgrades).
7. **Verificación:** tras cambios, correr `:app:assembleDebug` y `test`.

## Images (Library, SVG, caching)
1. **Librería:** **Coil 2.7.0** (compatible con tu Compose BOM `2024.09.00`).
   - `io.coil-kt:coil-compose:2.7.0`
   - `io.coil-kt:coil-svg:2.7.0`
2. **SVG vs PNG:** preferir SVG (nitidez), fallback a PNG si falla.
3. **ImageLoader & cache:** reutilizar **OkHttpClient** de `:core:network` para compartir cache/timeout/logging.
4. **UI API:** `AsyncImage` (stateless), `contentDescription`, placeholders de loading/error.
5. **Tests:** al menos un caso de carga SVG real y fallback de error.