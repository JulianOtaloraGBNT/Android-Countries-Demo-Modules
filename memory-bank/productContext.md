# Product Context

## SDK Initialization
- `CountriesSdk.create(context, NetworkConfig(apiKey = BuildConfig.COUNTRIES_API_KEY))`
- Base URL is defined inside the SDK BuildConfig (`SDK_BASE_URL`).
- UI artifact is stateless (`CountriesState/Events`), no navigation/data access

## Dependency Graph (note)
- `:app` **does** depend on `:core:data` for DI wiring. The app never calls the SDK directly; all access goes through the Data layer.

## Data Access & Sync Policy
- **Local-first**: UI observes Room-backed state (from SDK or `:core:data`)
- **Search ≥2 chars**: local first, remote fallback `/v3.1/name/{q}` if local empty
- **TTL refresh**: ~24h background refresh policy
- **Upsert by cca3**: map only required fields

## Configuration Management
- **API Key**: lives in `:app`, passed to SDK via `NetworkConfig`
- **Secrets**: never commit to VCS

## Testing Strategy
- **ViewModels**: debounce (~300ms), Turbine assertions
- **SDK**: contract tests over `CountriesClient` with MockWebServer

## Binary Artifacts Policy
- Feature AARs are treated as ephemeral build outputs and must not be committed.
- AARs are generated in feature modules and copied into libs/ for local consumption.
- `mavenLocal()` publishing can be introduced later; for now, libs/*.aar stays ignored.
