# System Patterns

## Module Dependencies
Allowed:
- :app → :core:ui, :core:domain, :core:common
- :core:data → :core:domain, :core:network, :core:common
- :core:ui → :core:common
- :core:domain → :core:common
- :core:network → :core:common

Forbidden:
- :app → :core:data
- :core:ui ↔ :core:data

## ViewModel Policy
- One ViewModel per navigation destination.
- ViewModels depend only on **UseCases** (no direct Data/Network).
- ViewModels receive coroutine **dispatchers via Hilt** (`@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`). **Do not** use `Dispatchers.IO` directly.
- Expose immutable `StateFlow<UiState>`; keep `MutableStateFlow` internal.
- Collect in UI with `collectAsStateWithLifecycle`; when adapting flows in VM, prefer `stateIn(SharingStarted.WhileSubscribed(5000))`.

## Error Mapping Path
- Data/Network layer converts Retrofit/OkHttp/serialization/IO failures to **AppError** (sealed).
- Domain layer returns `Result`/`Either<AppError, T>` without re-wrapping.
- App layer maps **AppError → UiError** (user-facing message + optional **retry** action).
- Log details (e.g., Timber) in Data layer; **never** expose raw exceptions/stacktraces to UI.
