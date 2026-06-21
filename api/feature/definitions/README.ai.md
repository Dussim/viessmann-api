### AI Hints for :api:feature:definitions

This module is the primary source of truth for the generated feature interfaces from YAML API specifications.

#### Core Logic
- **`GenerateFeatureInterfacesFromParsedFeaturePlugin`**: Gradle plugin located in `gradle/build-logic`. It reads generated feature JSON metadata and uses KotlinPoet to generate feature interfaces annotated with `@GenerateFeatureImplementation`.
- **YAML Location**: Sources are expected in `${openApiPath}/features`, where `openApiPath` defaults to `.ignored/featuresOpenApi` and can be overridden with `OPEN_API_PATH`.

#### Technical Details
- **Output Path**: Generated Kotlin source code is placed in `build/generated/features` and attached to `jvmMain` and `jsMain`.
- **Compilation Dependency**: All Kotlin compilation tasks in this module depend on the `generateFeatureInterfaces` task.

#### AI Context
When working with this module, avoid manually editing the files in `build/generated/features`. If you need to change the structure of a feature, modify the YAML source or the parsed-feature interface generation in `gradle/build-logic`.

**Caution**: This module only owns generated feature interface definitions. KSP-backed implementation artifacts belong to `:api:feature:implementations`.
