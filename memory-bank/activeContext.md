# Active Context

## Current State
- Six-module layout defined; allowed/forbidden dependencies set.
- Root task copies feature AARs into `libs/` and consumers depend on it during `preBuild`.
- SDK owns Room + network; base URL lives in SDK BuildConfig; `:app` provides API key via BuildConfig.
- UI artifact is stateless and includes Coil; `:app` composes and owns navigation.

## Decisions
- Use-cases are **interfaces** + `XxxYyyUseCaseImpl`.
- Repositories follow interface + impl; bound in Hilt from `:app`.
- `:core:data` wraps the SDK; `:app` depends on `:core:data` for DI wiring.
- Feature AARs consumed from `libs/*.aar` (initial POC).

## Build Byproducts & Git Hygiene
- .gitignore standardized to exclude Gradle outputs, IDE files, OS junk, and generated AAR/JARs.
- libs/ kept in repo via libs/.gitkeep; generated AARs copied here by `prepareFeatureAarsDebug`.
- Root tasks:
  - `prepareFeatureAarsDebug`: assembles feature AARs and copies them into libs/.
  - `cleanGeneratedBinaries`: deletes libs/*.aar, feature builds, and app/core build folders.
- app and core:data `preBuild` depend on `:prepareFeatureAarsDebug` to guarantee AAR availability.

## Feature Modules Status
- `:features:countries-data-sdk` ✅ COMPLETE (19.6KB AAR)
  - Public API: CountriesSdk, CountriesClient, NetworkConfig, CountryDto, SdkError
  - Minimal implementation stub ready for Room + Retrofit expansion
- `:features:countries-ui-artifact` ✅ COMPLETE (45.5KB AAR)
  - Stateless UI components: CountryCard, CountriesListView
  - State models: CountriesUiState, CountriesUiEvent
  - Includes Coil for image loading (coil-compose2)
  - Material 3 + Compose integration

## Next Steps
1) Ensure `settings.gradle.kts` includes all six modules.
2) Add the `prepareFeatureAarsDebug` root task and make `:app`/`:core:data` `preBuild` depend on it.
3) Scaffold packages per module (di/, navigation/, ui/{countries,details}, etc.).
4) Implement repository impl + mappers/error mapping in `:core:data`.
5) Implement domain use-cases (I/F + Impl) and bind in `UseCaseModule`.
6) Provide `CountriesClient` in `SdkModule`.
7) Implement basic screens + VMs and compose the artifact views from `:app`.
8) Add tests and run `:app:assembleDebug` + `test`.
