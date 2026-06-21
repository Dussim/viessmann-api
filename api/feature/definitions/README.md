# Module :api:feature:definitions

## Overview
This module contains the generated feature interface definitions. These interfaces are automatically created from Viessmann API YAML specifications.

## Functionality
- **JSON Generation**: Uses the `xyz.dussim.generate.features.json` Gradle plugin to parse API YAML specifications into feature JSON descriptors.
- **Interface Generation**: Uses the `xyz.dussim.generate.features` Gradle plugin to generate Kotlin interfaces from the produced JSON descriptors.
- **Targeted Outputs**: Generated interfaces are attached to `jvmMain` and `jsMain`, not `commonMain`.
- **API Modeling**: Serves as the source of truth for feature structures, properties, and commands as defined in the official API.

## Integration
This module publishes the interface definitions. Concrete generated implementations live in `:api:feature:implementations`.

## Gradle Configuration
The module uses the following configuration to point to the YAML sources and wire the two generation steps:

```kotlin
generateFeatureJsonsFromYaml {
    featuresYamls = openApiFeaturesDirectory
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")
}

generateFeatureInterfaces {
    featuresJsons = generateFeatureJsonsFromYamlTask.flatMap { it.generatedJsons }
    generatedSources = layout.buildDirectory.dir("generated/features")
    sourceSets = listOf("jvmMain", "jsMain")
    packageName = "xyz.dussim.viessmann.api.features.generated"
}
```

The OpenAPI source directory is configured through the `openApiPath` build parameter. It defaults to `.ignored/featuresOpenApi` and can be overridden with `OPEN_API_PATH`.

## Documentation
For more information on the generation process, refer to the [Feature Processor Code Generation Guide](../../../docs/feature-processor-guide.md).
