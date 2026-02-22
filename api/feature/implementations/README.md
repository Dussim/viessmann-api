# Module :api:feature:implementations

## Overview
This module contains the generated feature implementation classes. It bridges the gap between feature interfaces and their concrete implementations using KSP.

## Functionality
- **Implementation Generation**: Uses the `:api:feature:processor` KSP processor to generate implementations of interfaces annotated with `@GenerateFeatureImplementation`.
- **Feature Definitions**: This module also generates its own feature interfaces from YAML API specifications, allowing for localized generation and testing.

## Integration
This module provides concrete implementations of the features defined in `:api:feature:definitions` (and those generated within itself). These implementations are then used by higher-level modules like `:api:dto`.

## Gradle Configuration
This module requires both the YAML generation plugin and the KSP processor:

```kotlin
plugins {
    alias(conventions.plugins.xyz.dussim.generate.features.yaml)
}

dependencies {
    add("kspCommonMainMetadata", projects.api.feature.processor)
}
```

## Documentation
For a deep dive into the code generation process, see:
- [Feature Processor Guide](../../../docs/feature-processor-guide.md)
- [:api:feature:processor README](../processor/README.md)
