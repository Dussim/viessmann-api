### AI Hints for :api:feature:common

This module defines the core API and runtime for the feature system.

#### Core Concepts
- **`Feature`**: Base interface for all features (Device/Gateway).
- **`Property<T>`**: Typed property with a name and value.
- **`Command`**: Typed command that can be dispatched.
- **`ValidationResult` / `ValidationRule`**: Core of the validation system.

#### Performance Utilities
- **`EfficientStringKeyMap`**: Optimized for small maps with string keys. Used in generated features.
- **`IntToObjectMap`**: Optimized for integer-keyed mappings.

#### AI Context
- **Stability**: This is the most stable module. Changes here affect everything else.
- **Validation**: When adding new property types, ensure there's a corresponding validation rule helper in `xyz.dussim.viessmann.feature.api.validation`.
- **Factory Methods**: Use `FactoryMethods.kt` to see how to properly construct properties and features manually for testing or manual implementation.
- **KMP Support**: Keep everything in `commonMain` where possible for full platform support.
