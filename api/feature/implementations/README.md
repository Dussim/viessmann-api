# Module :api:feature:implementations

## Overview
This module contains the generated feature implementation classes. It runs the `:api:feature:processor` KSP processor for JVM and JS targets.

## Cross-module scanning
KSP is fed the generated Kotlin sources from `:api:feature:definitions` as explicit processor source roots. The Kotlin compilation still depends on `:api:feature:definitions` for the compiled interface classes, so the implementation artifact does not need to own YAML interface generation itself.

## Documentation
For a deep dive into the code generation process, see:
- [Feature Processor Guide](../../../docs/feature-processor-guide.md)
- [:api:feature:processor README](../processor/README.md)
