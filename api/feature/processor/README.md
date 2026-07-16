# Module :api:feature:processor

## Overview

This module contains the KSP2 (Kotlin Symbol Processing) processor responsible for generating type-safe implementations
of Viessmann API features.

The processor targets interfaces and classes annotated with `@GenerateFeatureImplementation` from the
`:api:feature:annotations` module.

## Functionality

- **Code Generation**: Generates concrete implementations of feature interfaces, including property accessors and
  command dispatchers.
- **Validation**: Automatically generates validation logic based on the feature's structure and rules defined in
  `:api:feature:common`.
- **Performance**: Produces highly optimized code using specialized collections like `EfficientStringKeyMap` to ensure
  minimal overhead during feature parsing and access.

## Integration

The processor is integrated into the build process of modules that define or consume features (e.g., `:api:dto` and
`:api:feature:definitions`). It runs during KSP tasks for the relevant source sets or compilations to ensure that
generated code is available for the required targets.

### Usage in Gradle

To use the processor in a module, add it to the `ksp` configuration (using `kspCommonMainMetadata` only if you want to
generate code directly to `commonMain`):

```kotlin
dependencies {
    // Required only for direct code generation into commonMain
    add("kspCommonMainMetadata", projects.api.feature.processor)
}
```

## Key Components

- `FeatureImplementationGenerator`: The core engine that uses KotlinPoet to emit Kotlin source code.
- `FeatureImplementationProcessor`: The KSP `SymbolProcessor` that validates declarations, collects round-safe models,
  and emits generated files.

## Processor options

All sizes are logical generated declarations rather than flattened Kotlin properties or methods.

| Option                     |                 Default |    Allowed values | Purpose                                                                    |
|----------------------------|------------------------:|------------------:|----------------------------------------------------------------------------|
| `descriptorsChunkSize`     |                      64 |             1–256 | Maximum feature descriptors emitted in one descriptor source file.         |
| `validationRulesChunkSize` |                      64 |             1–128 | Maximum cached validation-rule properties emitted in one source file.      |
| `formatGeneratedSources`   |                  `true` | `true` or `false` | Applies generated-source formatting. Consumer modules normally disable it. |
| `renderParallelism`        | CPU-based, capped at 16 |              1–16 | Maximum parallel KotlinPoet rendering workers.                             |

Invalid values are reported as KSP errors and generation uses the documented safe default.

## Generated validation contract

Generated feature and command companions implement `GeneratedValidationRule`. `validate` preserves aggregating
validation, while `validateFailFast` evaluates the same semantic rule plan and returns after the first invalid result.
Descriptor structure matchers use the fail-fast path. No nested `FailFast` singleton is generated.

Feature and command inheritance is deliberately not supported: a generated feature must directly extend `Feature`, and a
generated command must directly extend exactly one `Command0` through `Command8`. Unsupported inheritance is reported at
the declaration instead of being accepted and failing in generated Kotlin.

## Documentation

For a detailed guide on how to model features for code generation, refer to
the [Feature Processor Code Generation Guide](../../../docs/feature-processor-guide.md).
