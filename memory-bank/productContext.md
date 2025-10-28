# Product Context

## SDK Initialization
- `CountriesSdk.create(NetworkConfig(baseUrl, apiKey))` 
- UI artifact is stateless (`CountriesState/Events`), no navigation/data access
- No direct `:app → :core:data`

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
