### AI Hints for :api:errors

This module contains shared error payload DTOs used across service-specific API modules.

- Prefer `ViessmannApiError` for generic service errors.
- `ErrorType` follows the existing sealed enum pattern with `Unknown`.
- `extendedPayload` and validation `context` are intentionally modeled as `JsonObject`.
