# Module :api:feature:definitions

## Overview
This module contains the generated feature interface definitions. These interfaces are automatically created from Viessmann API YAML specifications.

## Functionality
- **Interface Generation**: Uses the `xyz.dussim.generate.features.yaml` Gradle plugin to parse API specifications and generate Kotlin interfaces.
- **Targeted Outputs**: Generated interfaces are attached to `jvmMain` and `jsMain`, not `commonMain`.
- **API Modeling**: Serves as the source of truth for feature structures, properties, and commands as defined in the official API.

## Integration
This module publishes the interface definitions. Concrete generated implementations live in `:api:feature:implementations`.

## Gradle Configuration
The module uses the following configuration to point to the YAML sources:

```kotlin
generateFeatureInterfacesFromYaml {
    featuresYamls = openApiFeaturesDirectory
    packageName = "xyz.dussim.viessmann.api.features.generated"
}
```

The OpenAPI source directory is configured through the `openApiPath` build parameter. It defaults to `.ignored/featuresOpenApi` and can be overridden with `OPEN_API_PATH`.

## Documentation
For more information on the generation process, refer to the [Feature Processor Code Generation Guide](../../../docs/feature-processor-guide.md).
