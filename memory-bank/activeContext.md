# Active Context

## Current State
- Memory bank synced to android-rules.md
- Core modules: :app, :core:domain, :core:data, :core:common
- AAR consumption strategy: `mavenLocal()` preferred; `flatDir { dirs("local-aars") }` alternative

## AAR Consumption
- :app consumes countries-ui-artifact.aar
- :core:data consumes countries-data-sdk.aar and wraps CountriesClient

## Next Steps
1. Create core modules per defined structure
2. Configure AAR resolution (mavenLocal/flatDir)
3. Implement CountriesClient wrapper in :core:data
4. Develop base use cases and ViewModels
