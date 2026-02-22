### AI Hints for :api:feature:annotations

This module contains annotations for the feature system and code generation.

#### Core Annotations
- **`@GenerateFeatureImplementation`**: Used to mark interfaces or classes for KSP code generation.

#### AI Context
- **Stability**: Very stable. Modifying these annotations will trigger KSP processing in all dependent modules.
- **Dependency**: This is a leaf module in the internal feature system. It should not depend on other feature modules.
- **Targeting**: When using `@GenerateFeatureImplementation`, provide the `featureName` exactly as it appears in the API (e.g., "HeatingDeviceSmartGridVariant").
