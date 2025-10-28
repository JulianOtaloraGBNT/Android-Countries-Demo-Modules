# Project Brief

## Purpose
Android Clean Architecture demo with externalized AAR libraries and SDK consumption patterns.

## Core Structure
- `:app` (navigation & presentation glue)
- `:core:domain` (entities, repository contracts, use cases)
- `:core:data` (repositories, mappers; optional Room outside SDK)
- `:core:common` (Result/Either, AppError, dispatcher qualifiers, shared utils)

## Externalized Libraries (AAR Consumption)
- `countries-data-sdk.aar`: Networking/persistence SDK wrapped by `:core:data`
- `countries-ui-artifact.aar`: Stateless Compose UI consumed by `:app`

## Architecture Constraints
- **Call graph:** `MainActivity/MainNavHost → Screen → ViewModel → UseCase → Repository → Data Source`
- **DI:** Hilt with constructor injection and dispatcher qualifiers
- **Error handling:** Retrofit/OkHttp → AppError (sealed) → UiError (user-facing + retry)
- **Local-first:** UI observes Room-backed state with TTL-based refresh policy
