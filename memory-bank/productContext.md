# Product Context

## Data Access Rules
- Local-first: UI observes Room DB via Flow
- TTL: 24h cache expiration → fetch /v3.1/all
- Search: ≥2 chars, 300ms debounce → local LIKE → remote /v3.1/name/{q} fallback
- Images: Coil2 with shared OkHttpClient
