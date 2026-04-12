# Viessmann API — Kotlin Multiplatform DTOs & Feature System

A Kotlin Multiplatform project that models Viessmann API data (DTOs) and provides a type-safe, annotation-driven feature system with code generation (KSP2). The repository also includes JMH benchmarks and comprehensive tests across JVM and JS (Node + Browser) targets.

> **JVM target:** Java 21. **Kotlin:** 2.3.10. **KSP2:** Enabled.

## Overview
The project defines:
- A set of multiplatform DTOs for Viessmann API entities (devices, gateways, installations, enums, etc.).
- A feature system for modeling typed features, properties, and commands with validation, generated via KSP2 from annotations.
- A benchmarking suite to assess performance characteristics around feature parsing/validation.

### Tech Stack
- **Language:** Kotlin 2.3.10 (Multiplatform)
- **Frameworks:**
  - Kotlinx Serialization (JSON)
  - KSP2 (Code generation)
  - Kotest (Testing)
  - JMH (Benchmarking)
- **Package Manager:** Gradle (Wrapper included)
- **Static Analysis:** Detekt, Kotlinter

## Requirements
- Java 21+ (JDK 21 is the target).
- Gradle Wrapper (provided).
- Node.js (required for JS target tests).

## Project Structure
- `:api:dto` — Kotlin Multiplatform DTOs and feature models.
- `:api:feature:annotations` — Annotation definitions.
- `:api:feature:processor` — KSP2 processor for code generation.
- `:api:feature:common` — Common feature API (core types, validation, utilities).
- `:api:feature:benchmark` — JMH benchmarks (JVM only).

## Getting Started

### Clone & Build
```powershell
# Clean build all modules
./gradlew.bat clean build
```

### IDE Setup
- IntelliJ IDEA 2024.3+ recommended.
- Import as a Gradle project.
- KSP generated sources (`build/generated/ksp/`) are added to the `:api:dto` `commonMain` source set automatically.

## Scripts (Common Tasks)

### Build
```powershell
# Build everything
./gradlew.bat build

# Build a specific module
./gradlew.bat :api:dto:build
```

### Tests (Kotest)
```powershell
# All tests
./gradlew.bat test

# JVM-only tests for :api:dto
./gradlew.bat :api:dto:jvmTest

# JS tests (Node)
./gradlew.bat :api:dto:jsNodeTest

# JS tests (Browser)
./gradlew.bat :api:dto:jsBrowserTest
```

### Benchmarks (JMH)
```powershell
# Run benchmarks
./gradlew.bat :api:feature:benchmark:jmh
```

### Quality Assurance
```powershell
# Format
./gradlew.bat formatKotlin

# Lint
./gradlew.bat lintKotlin

# Detekt
./gradlew.bat detekt
```

## Env Vars
- `JAVA_HOME`: Should point to a valid JDK 21+ installation.
- No specific environment variables are strictly required for standard builds, but ensure Node.js is in your PATH for JS targets.

## TODOs / Unknowns
- [ ] Verify if any specific API credentials or configuration files are needed for real API interaction.

## License
Licensed under the Apache License, Version 2.0.
