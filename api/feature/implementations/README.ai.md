### AI Hints for :api:feature:implementations

This module serves as the primary location for the KSP-generated feature implementations.

#### Core Logic
- **`FeatureProcessor` Integration**: This module runs the `:api:feature:processor` KSP processor on feature interfaces to generate their concrete implementations.
- **YAML to Interface**: Like `:api:feature:definitions`, it also uses the YAML generation plugin for defining additional feature interfaces for testing or localized implementation.

#### Technical Details
- **Generated Code Location (KSP)**: Implementation classes are generated in `build/generated/ksp/metadata/commonMain/kotlin`.
- **Generated Code Location (YAML)**: Interface classes are generated in `build/generated/features`.
- **KSP Dependency**: The Gradle configuration ensures `kspCommonMainKotlinMetadata` runs before compilation.

#### AI Context
When working with this module, be aware that most of the functional code is generated. If you encounter issues in the implementation logic, you should modify the KSP processor in `:api:feature:processor`. If you need to change the structure of a feature, modify the YAML source or the YAML generator plugin.

**Caution**: Ensure you do not add manual code to this module's `src/commonMain/kotlin` unless it's strictly necessary and does not conflict with the generated code.
