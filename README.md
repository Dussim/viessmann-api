# Viessmann API — Kotlin Multiplatform DTOs & Feature System

A Kotlin Multiplatform project that models Viessmann API data (DTOs) and provides a type-safe, annotation-driven feature system with code generation (KSP2). The repository also includes JMH benchmarks and comprehensive tests across JVM and JS (Node + Browser) targets.

> JVM target: Java 21. Kotlin: 2.2.21 (plugins). KSP2 is enabled.

---

## Contents
- Overview
- Architecture & Modules
- Requirements
- Getting Started
  - Clone & Build
  - IDE Setup
- Working With the Project
  - Build
  - Tests (Kotest)
  - Benchmarks (JMH)
  - Formatting, Linting, Static Analysis
- Code Generation (KSP2)
- DTOs, Features, and Examples
- Performance Utilities
- Troubleshooting & Known Issues
- Gradle Tips
- Contributing
- License

---

## Overview
The project defines:
- A set of multiplatform DTOs for Viessmann API entities (devices, gateways, installations, enums, etc.).
- A feature system for modeling typed features, properties, and commands with validation, generated via KSP2 from annotations.
- A small benchmarking suite to assess performance characteristics around feature parsing/validation.

Technologies:
- Kotlin Multiplatform (JVM, JS: Node.js + Browser)
- Kotlinx Serialization
- KSP2 for code generation
- Kotest for testing
- Detekt + Kotlinter for static analysis and formatting
- JMH for benchmarks (JVM only)


## Architecture & Modules
This is a multi-module Gradle project with type-safe project accessors.

- `:api:dto` — Kotlin Multiplatform DTOs and feature models used by consumers.
  - Targets: JVM 21, JS (Node + Browser, ES modules, TypeScript definitions generated)
  - Depends on `:api:feature:common` and uses KSP-generated sources
- `:api:feature:annotations` — Annotation definitions used by the KSP processor (e.g., `@GenerateFeatureImplementation`).
- `:api:feature:processor` — KSP2 processor which generates concrete feature/command implementations.
- `:api:feature:common` — Common feature API (core types like `Feature`, `Property`, `Command`, validation utilities, and efficient maps).
- `:api:feature:benchmark` — JMH benchmarks for selected feature scenarios (JVM only).

Root project metadata:
- Group: `xyz.dussim`
- Version: `0.0.1`


## Requirements
- Java 21+ (JDK 21 is the target). Newer Java versions also work (see Known Issues).
- Gradle Wrapper is included — no separate Gradle installation needed.
- Node.js for JS tests (if you want to run JS target tests).

Optional (for benchmarks):
- Ensure you run benchmarks on a stable, thermally consistent environment.


## Getting Started

### Clone & Build
```powershell
# From project root
# Clean build all modules
./gradlew.bat clean build
```

If you encounter JAVA_HOME errors on Windows PowerShell:
```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25"
```

### IDE Setup
- IntelliJ IDEA 2024.3+ recommended.
- Import as a Gradle project.
- Configuration Cache is enabled; the IDE will respect that.
- KSP generated sources are under `build/generated/ksp/` and are added to the `:api:dto` `commonMain` source set automatically.


## Working With the Project

### Build
```powershell
# Build everything
./gradlew.bat build

# Build a specific module
./gradlew.bat :api:dto:build
```

### Tests (Kotest)
Kotest is used for unit and integration-style tests.

```powershell
# All tests (all modules and targets)
./gradlew.bat test

# Only :api:dto tests
./gradlew.bat :api:dto:test

# JVM-only tests for :api:dto
./gradlew.bat :api:dto:jvmTest

# JS tests (Node)
./gradlew.bat :api:dto:jsNodeTest

# JS tests (Browser)
./gradlew.bat :api:dto:jsBrowserTest

# Kotest task (where available)
./gradlew.bat :api:dto:kotest
```

Test reports are in `build/reports/tests/` of each module.

### Benchmarks (JMH)
```powershell
# Run all benchmarks in the benchmark module
./gradlew.bat :api:feature:benchmark:jmh
```

Benchmark reports:
- JSON: `api/feature/benchmark/build/reports/jmh/results.json`
- Human-readable: `api/feature/benchmark/build/reports/jmh/human.txt`

### Formatting, Linting, Static Analysis
```powershell
# Format with Kotlinter (ktlint_official style)
./gradlew.bat formatKotlin

# Lint with Kotlinter
./gradlew.bat lintKotlin

# Static analysis with Detekt
./gradlew.bat detekt

# All verifications (tests + lint + detekt, etc.)
./gradlew.bat check
```

Detekt configuration: `config/detekt/detekt.yml`


## Code Generation (KSP2)
This project uses KSP2 to generate feature and command implementations based on annotations from `:api:feature:annotations` and processed by `:api:feature:processor`.

Key facts:
- KSP2 is enabled (`ksp.useKSP2=true` in `gradle.properties`).
- All Kotlin compilation tasks depend on `kspCommonMainKotlinMetadata` to ensure generated code is available during compilation.
- Generated sources live under:
  - `build/generated/ksp/` (module-local)
  - For `:api:dto`: `build/generated/ksp/metadata/commonMain/kotlin`
- Generated sources are added to the `commonMain` source set for `:api:dto`.


## DTOs, Features, and Examples

### DTOs
You’ll find DTOs under `:api:dto/src/commonMain/kotlin/xyz/dussim/viessmann/api` in these packages:
- `enums` — Model enums (e.g., `AccessLevel`, `TokenType`, `HeatingType`, etc.) with utilities for stable handling of unknown values.
- `features` — High-level feature descriptors (e.g., `DeviceFeatures`, `GatewayFeatures`).
- `models` — Core models (e.g., `Device`, `Gateway`, `Installation`, `ResponseData`).
- `utils` — Serialization helpers, factories, and entry holders.

There are comprehensive tests with real JSON samples in `:api:dto/src/commonTest/resources`.

### Feature System (Common API)
Common feature API is defined in `:api:feature:common`:
- Key types: `Feature`, `Property`, `Command`, `FeatureFactory`, `FeatureEnumFactory`.
- Validation: `validation` package provides rules, combinators, and exceptions.
- Performance utilities: `EfficientStringKeyMap`, `IntToObjectMap`.

### Annotations (for Codegen)
`@GenerateFeatureImplementation` is used to declare that a feature model should get a generated implementation.

```kotlin
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation

@GenerateFeatureImplementation(featureName = "DeviceZigbeeCoordinator")
class DeviceZigbeeCoordinatorFeature // fields/properties modeled in common API
```

The KSP processor will generate the feature and command boilerplate used by `:api:dto`.

### Parsing & Validation Example (DTO side)
```kotlin
import kotlinx.serialization.json.Json
import xyz.dussim.viessmann.api.features.DeviceFeatures

val json = Json { ignoreUnknownKeys = true }
val payload = /* load your JSON here */
val deviceFeatures: DeviceFeatures = json.decodeFromString(payload)

// Access properties/commands in a type-safe manner after generation
```

### JS Targets
The JS target produces ES modules and generates TypeScript definitions for better interop.


## Performance Utilities
Benchmark scenarios are provided in `:api:feature:benchmark`:
- `ValidationBenchmark.kt`, `ValidationSmallFeatureBenchmark.kt` — validation performance
- `OneElementMapBenchmark.kt`, `TwoElementMapBenchmark.kt`, `ThreeElementMapBenchmark.kt` — map microbenchmarks

Use them to compare alternative implementations or regressions over time.


## Troubleshooting & Known Issues
- JAVA_HOME / JDK: Ensure Java 21+ is selected. If needed on Windows PowerShell:
  ```powershell
  $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25"
  ```
- Configuration Cache: Enabled by default. Problems are reported as warnings; report is at `build/reports/problems/problems-report.html`.
- KSP Compilation Order: Compilation tasks depend on `kspCommonMainKotlinMetadata`. Do not change this without thorough testing.
- Java 25 with JMH: You may see deprecation warnings about `sun.misc.Unsafe`. Harmless and can be ignored.


## Gradle Tips
```powershell
# List all tasks
./gradlew.bat tasks

# Verification tasks
./gradlew.bat tasks --group=verification

# Build tasks
./gradlew.bat tasks --group=build
```

Version catalog: `gradle/libs.versions.toml` contains plugin and library versions.


## Contributing
- Use the Gradle wrapper and Java 21+.
- Run `./gradlew.bat check` before submitting changes.
- Follow the ktlint_official code style (enforced by Kotlinter). Max line length 180.
- Prefer adding/adjusting tests in the appropriate multiplatform source set (`src/commonTest`, `src/jvmTest`, `src/jsTest`).
- For KSP-related changes, inspect generated sources under `build/generated/ksp/` and ensure compilation order and IDE source inclusion remain correct.


## License
No license has been declared yet. If you intend to use or distribute this project, please add a LICENSE file and update this section accordingly.
