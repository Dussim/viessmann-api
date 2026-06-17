### AI Hints for :api:feature:implementations

This module is the primary location for KSP-generated feature implementations.

#### Core Logic
- `:api:feature:definitions` generates annotated feature interfaces into its target source sets.
- This module depends on `:api:feature:definitions` for compiled interface types.
- This module also adds `:api:feature:definitions/build/generated/features` to the JVM and JS KSP task source roots so `GenerateFeatureImplementation` annotations are visible across modules.

#### Technical Details
- KSP output locations are `build/generated/ksp/jvm/jvmMain/kotlin` and `build/generated/ksp/js/jsMain/kotlin`.
- JSON fixture tests are generated in this module because they require concrete descriptors produced by the implementation processor.
