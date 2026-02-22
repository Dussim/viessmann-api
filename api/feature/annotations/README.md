# Module :api:feature:annotations

## Overview
This module contains annotation definitions used by the Viessmann API feature system.

## Key Annotations

### `@GenerateFeatureImplementation`
Annotate a feature interface or class with `@GenerateFeatureImplementation` to signal the KSP processor (`:api:feature:processor`) to generate a concrete implementation.

- **`featureName`**: The name of the feature as defined in the Viessmann API.

## Usage
Add this module as an `api` dependency to modules that define features:

```kotlin
dependencies {
    api(projects.api.feature.annotations)
}
```
The KSP processor will then pick up these annotations during compilation.
