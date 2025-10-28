# Implementation Plan

## Phase 0 — Housekeeping (Git hygiene & reproducible AARs)
- Enforce .gitignore rules for build outputs and generated AAR/JARs.
- Ensure libs/.gitkeep exists; never commit generated binaries.
- Provide root tasks:
  - `prepareFeatureAarsDebug` to generate/copy AARs.
  - `cleanGeneratedBinaries` to purge artifacts.
- Make `:app` and `:core:data` `preBuild` depend on `:prepareFeatureAarsDebug`.

## Phase 1 — Build Foundations
- Add all 6 modules to `settings.gradle.kts`.
- Ensure plugins via version catalog: android-application, android-library, kotlin-android, kotlin-jvm, kotlin-kapt, kotlin-compose, hilt.
- Create root Gradle task `prepareFeatureAarsDebug`:
  - Depends on `:features:countries-data-sdk:assembleDebug` and `:features:countries-ui-artifact:assembleDebug`.
  - Copies `*debug*.aar` into `${rootDir}/libs/`.
- In `:app` and `:core:data`: make `preBuild` depend on `:prepareFeatureAarsDebug`.

## Phase 2 — Scaffolding & DI
- `:app` (`com.julianotalora.countriesdemo`)
  - `di/` → CommonModule, SdkModule, RepositoryModule, UseCaseModule
  - `navigation/` → BaseDestination, MainNavHost
  - `ui/countries` & `ui/details` → navigation / view / viewmodel
- `:core:common` — Result/Either, AppError, qualifiers, extensions
- `:core:domain` — entities, repository interfaces, use-cases (interfaces + Impl)
- `:core:data` — repository impl, mappers, error mapping

## Phase 3 — SDK & UI Artifacts
- `:features:countries-data-sdk`
  - Public API: CountriesSdk, CountriesClient, NetworkConfig, CountryDto, SdkError
  - Internal: Room entities/dao/converters, Retrofit/OkHttp, TTL policy, mapping
  - `defaultConfig` with `buildConfigField("String", "SDK_BASE_URL", "...")`
- `:features:countries-ui-artifact`
  - Stateless views + components; includes Coil; no navigation/data/Hilt

## Phase 4 — Wiring & Testing
- Provide `CountriesClient` in `SdkModule` using `CountriesSdk.create(context, NetworkConfig(apiKey = BuildConfig.COUNTRIES_API_KEY))`
- Bind repositories & use-cases in :app DI
- VMs depend only on use-cases
- Tests:
  - Domain: use-cases with fake repos
  - Data: repo impl with fake/mocked CountriesClient
  - SDK: MockWebServer + Room in-memory; TTL & error mapping
  - (Optional) UI snapshots/interactions

## Phase 5 — Build & Verify
- `./gradlew :prepareFeatureAarsDebug`
- `./gradlew :app:assembleDebug`
- `./gradlew test`
- Iterate and update memory bank.
