### AI Hints for :api:feature:processor

This module contains the KSP2 processor responsible for generating feature and command implementations.

#### Core Logic
- **`FeatureProcessor`**: Entry point for KSP. Scans for `@GenerateFeatureImplementation`.
- **`FeatureImplementationGenerator`**: Core engine using KotlinPoet to emit Kotlin source code.
- **Generation Logic**: The processor generates concrete classes that extend `AbstractDeviceFeature` or `AbstractGatewayFeature`. It handles property extraction from the raw feature map and command dispatcher generation.

#### Technical Details
- **Output Path**: Generated code is placed in `build/generated/ksp/metadata/commonMain/kotlin`.
- **Dependencies**: It relies on `:api:feature:annotations` for symbols and `:api:feature:common` for runtime types.
- **Specialized Maps**: It generates code using `EfficientStringKeyMap` to optimize lookup.

#### AI Context
When working on this module, ensure you handle KotlinPoet types correctly. Any change to the generated structure might require updates to tests in `:api:dto` or other consuming modules. Pay attention to the `kspCommonMainKotlinMetadata` task dependency in Gradle to ensure proper compilation order.