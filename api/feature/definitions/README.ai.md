### AI Hints for :api:feature:definitions

This module is the primary source of truth for the generated feature interfaces from YAML API specifications.

#### Core Logic
- **`YamlFeatureInterfaceGenerator`**: Gradle plugin located in `gradle/build-logic`. It parses YAML files and uses KotlinPoet to generate feature interfaces annotated with `@GenerateFeatureImplementation`.
- **YAML Location**: Sources are expected in `.ignored/featuresOpenApi/features/` (relative to settings directory).

#### Technical Details
- **Output Path**: Generated Kotlin source code is placed in `build/generated/features` and attached to `jvmMain` and `jsMain`.
- **Compilation Dependency**: All Kotlin compilation tasks in this module depend on the `generateFeatureInterfacesFromYaml` task.

#### AI Context
When working with this module, avoid manually editing the files in `build/generated/features`. If you need to change the structure of a feature, modify the YAML source or the `YamlFeatureInterfaceGenerator` plugin in `gradle/build-logic`.

**Caution**: This module only owns generated feature interface definitions. KSP-backed implementation artifacts belong to `:api:feature:implementations`.
