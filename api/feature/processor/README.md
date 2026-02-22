# Module :api:feature:processor

## Overview
This module contains the KSP2 (Kotlin Symbol Processing) processor responsible for generating type-safe implementations of Viessmann API features.

The processor targets interfaces and classes annotated with `@GenerateFeatureImplementation` from the `:api:feature:annotations` module.

## Functionality
- **Code Generation**: Generates concrete implementations of feature interfaces, including property accessors and command dispatchers.
- **Validation**: Automatically generates validation logic based on the feature's structure and rules defined in `:api:feature:common`.
- **Performance**: Produces highly optimized code using specialized collections like `EfficientStringKeyMap` to ensure minimal overhead during feature parsing and access.

## Integration
The processor is integrated into the build process of modules that define or consume features (e.g., `:api:dto`, `:api:feature:definitions`, and `:api:feature:implementations`). It runs during the `kspCommonMainKotlinMetadata` task (or similar metadata tasks) to ensure that generated code is available for all multiplatform targets.

### Usage in Gradle
To use the processor in a module, add it to the `ksp` configuration (using `kspCommonMainMetadata` only if you want to generate code directly to `commonMain`):

```kotlin
dependencies {
    // Required only for direct code generation into commonMain
    add("kspCommonMainMetadata", projects.api.feature.processor)
}
```

## Key Components
- `FeatureImplementationGenerator`: The core engine that uses KotlinPoet to emit Kotlin source code.
- `FeatureProcessor`: The KSP `SymbolProcessor` implementation that orchestrates the scanning and generation process.

## Documentation
For a detailed guide on how to model features for code generation, refer to the [Feature Processor Code Generation Guide](../../../docs/feature-processor-guide.md).
