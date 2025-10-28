# Project Brief

## Purpose
Clean Architecture Android demo using a multi-project Gradle setup. Two feature libraries are produced as AARs and consumed as binaries:
- `:features:countries-data-sdk` — self-contained SDK (Retrofit/OkHttp/Serialization/Room, TTL).
- `:features:countries-ui-artifact` — stateless Compose UI (includes Coil) with complete views and reusable components.

## Modules & Namespaces
- `:app` — **com.julianotalora.countriesdemo** (presentation, navigation, Hilt composition root).
- `:core:common` — **com.julianotalora.core.common** (Result/Either, AppError, dispatcher qualifiers, extensions).
- `:core:domain` — **com.julianotalora.core.domain** (entities, repository contracts, use cases as interfaces + `Impl`).
- `:core:data` — **com.julianotalora.core.data** (repository impls; wraps SDK; no Room).
- `:features:countries-data-sdk` — **com.julianotalora.features.countriesdatasdk** (SDK; owns Room + network; exposes `CountriesSdk.create(...)` and `CountriesClient`).
- `:features:countries-ui-artifact` — **com.julianotalora.features.countriesuiartifact** (stateless Compose UI + Coil; no data access, no Hilt).

## Dependency Graph (enforced)
Allowed:
- `:app → :core:domain, :core:common, :core:data`
- `:core:data → :core:domain, :core:common`
- `:core:domain → :core:common`

Forbidden:
- `:core:domain → :core:data` or `:app`
- any `:core:* → :app`
- App never calls the SDK directly; it goes through Data.

## AAR Workflow
- Root task `prepareFeatureAarsDebug` assembles both feature AARs and copies them to `libs/`.
- `:core:data` consumes `countries-data-sdk-debug.aar`.
- `:app` consumes `countries-ui-artifact-debug.aar` (artifact contains Coil so the app doesn't need a direct Coil dep for those views).
- Optional future: publish features to `mavenLocal()` if transitive metadata is needed.

## DI Strategy
- Hilt modules in `:app`: CommonModule (dispatchers), SdkModule (provides `CountriesClient` via `CountriesSdk.create(context, NetworkConfig(apiKey = BuildConfig.COUNTRIES_API_KEY))`), RepositoryModule (bind repository interfaces → impls in `:core:data`), UseCaseModule (bind each use-case interface → its `Impl` in `:core:domain`).
- `:core:data` uses `@Inject` constructors; no Hilt modules.
- `:core:common` is JVM only (qualifiers/utilities).

## Features
- Features: `countries`, `details`.
- Domain: Query/Command split; use-cases are interfaces with `XxxYyyUseCaseImpl`.
- Data: maps SDK DTO→Domain and `SdkError`→`AppError`; decides *when* to refresh; SDK decides *how/where* to store.
- SDK: persists full `CountryEntity` in Room; exposes `observe*` flows and `refresh*` with TTL.
