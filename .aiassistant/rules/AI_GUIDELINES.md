---
apply: always
---

## MOST IMPORTANT INFO

1. BE SUPER SAFE: When running commands against the modules, NEVER analyze build folders or temporary artifacts.
2. NEVER RUN TESTS IF NOT ASKED TO
3. NEVER TRY TO LIST FILES IN BUILD DIR IF NOT ASKED TO
4. NEVER ANALYZE BUILD DIR IF NOT ASKED TO
5. NEVER BUILD TARGETS OTHER THAN JVM IF NOT ASKED TO

### Build & Configuration

**Requirements:** Java 21+, Node.js (for JS tests)

**Build commands:**

```powershell
./gradlew.bat build                    # Build all modules
./gradlew.bat :api:dto:build           # Build specific module
./gradlew.bat clean build              # Clean build
```

**KSP Code Generation:** KSP2 generates feature implementations from interfaces annotated with
`@GenerateFeatureImplementation`. Generated sources are in `build/generated/ksp/`. Note that `kspCommonMainMetadata`
is only needed if you want to generate code directly into `commonMain`. All compilation tasks depend on
`kspCommonMainKotlinMetadata` - do not change this dependency order.

---

### Testing

**Framework:** Kotest with FunSpec style. JVM tests run on JUnit Platform.

**Test commands:**

```powershell
./gradlew.bat test                         # All tests (all modules/targets)
./gradlew.bat :api:dto:jvmTest             # JVM tests for api:dto
./gradlew.bat :api:feature:common:jvmTest  # JVM tests for feature:common
./gradlew.bat :api:dto:jsNodeTest          # JS Node tests
./gradlew.bat :api:dto:jsBrowserTest       # JS Browser tests

# Run specific test class
./gradlew.bat :api:dto:jvmTest --tests "xyz.dussim.viessmann.api.enums.AccessLevelTest"
```

**Adding new tests:**

1. Create test file in `src/commonTest/kotlin/` (for multiplatform) or `src/jvmTest/kotlin/` (JVM-only)
2. Extend `FunSpec` from Kotest
3. Use `test("description") { ... }` or `context("group") { ... }` blocks

**Test file example:**

```kotlin
package xyz.dussim.viessmann.feature.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExampleTest :
    FunSpec({
        test("example test") {
            val result = 1 + 1
            result shouldBe 2
        }

        context("grouped tests") {
            test("first") {
                true shouldBe true
            }
        }
    })
```

**Test dependencies** (automatically provided by convention plugin `xyz.dussim.kotlin.common`):

- `kotest-framework-engine` - test framework
- `kotest-assertions-core` - assertions (`shouldBe`, `shouldContain`, etc.)
- `kotest-runner-junit5` - JVM runner (jvmTest only)

---

### Code Style & Patterns

**Formatter:** Kotlinter with ktlint_official style
**Static analysis:** Detekt (config: `config/detekt/detekt.yml`)
**Max line length:** 180 characters

**Multiplatform:** This is a Kotlin Multiplatform project.
- `commonMain`: Shared logic, DTOs, interfaces.
- `jvmMain`: JVM-specific implementations (e.g., JMH benchmarks, specialized performance optimizations).
- Always prefer `commonMain` unless platform-specific APIs are required.
- Use `expect`/`actual` for platform-specific functionality.

```powershell
./gradlew.bat formatKotlin    # Auto-format code
./gradlew.bat lintKotlin      # Check formatting
./gradlew.bat detekt          # Run static analysis
./gradlew.bat check           # Run all verifications (tests + lint + detekt)
```

**Style conventions:**

- Class definition with lambda on new line (see test examples)
- Trailing commas in multi-line lists
- Explicit type parameters when needed for clarity

**Architecture Patterns:**

- **JvmRecord:** Use `@JvmRecord` for all DTOs and simple data classes to improve performance and Java interop.
- **Performance Maps:** Use `EfficientStringKeyMap` for `Feature` properties and commands instead of standard
  `Map<String, T>`. Note that `EfficientStringKeyMap` is **immutable** and optimized for fast lookups using precomputed hashes.
- **Feature Descriptors:** Use `FeatureDescriptor` and `FeatureFactory` for typed feature access.
- **Feature Resolver:** Use `FeatureResolver` to find and convert features from a list. It provides convenient methods like `findOf`, `firstOf`, and `allOf`.
- **Factory Methods:** Prefer using the DSL-like factory methods in `FactoryMethods.kt` and `Property.of...`.
- **Validation:** Use the `validation` package rules for consistency.
- **Enum Pattern:** For API enums, use the `sealed interface` with `Strict` and `Unknown` subclasses to handle future
  API changes safely.
- **FeatureEnumFactory:** When a feature property is an enum-like string, implement `FeatureEnumFactory` in the property's companion object to provide type-safe conversion from `StringValue`.
- **Property Access:** Always use the provided type-safe property accessors in generated features instead of manually querying the `properties` map.

---

### Module Structure

For each module, there is a standard `README.md` for human developers and a `README.ai.md` (AI Hints) file that provides high-level technical context, design patterns, and internal implementation details specifically for AI agents.

| Module                     | Description                                                                                                                                                     |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:api:dto`                 | Multiplatform DTOs, feature models. Uses KSP-generated sources.                                                                                                 |
| `:api:feature:annotations` | Annotation definitions for KSP processor ([README](api/feature/annotations/README.md), [AI Hints](api/feature/annotations/README.ai.md))                        |
| `:api:feature:processor`   | KSP2 processor generating feature implementations ([README](api/feature/processor/README.md), [AI Hints](api/feature/processor/README.ai.md))                   |
| `:api:feature:common`      | Core types (`Feature`, `Property`, `Command`), validation, efficient maps ([README](api/feature/common/README.md), [AI Hints](api/feature/common/README.ai.md)) |
| `:api:feature:benchmark`   | JMH benchmarks (JVM only) ([README](api/feature/benchmark/README.md), [AI Hints](api/feature/benchmark/README.ai.md))                                           |
| `:api:feature:definitions` | Generated feature interfaces from YAML ([README](api/feature/definitions/README.md), [AI Hints](api/feature/definitions/README.ai.md))                       |
| `:api:feature:implementations` | KSP-generated feature implementation classes ([README](api/feature/implementations/README.md), [AI Hints](api/feature/implementations/README.ai.md)) |

---

### Benchmarks (JVM only)

```powershell
./gradlew.bat :api:feature:benchmark:jmh
```

Reports: `api/feature/benchmark/build/reports/jmh/results.json`

---

### Troubleshooting

**JAVA_HOME issues on Windows:**

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-21"
```

**Configuration Cache:** Enabled by default. Problems report: `build/reports/problems/problems-report.html`

**KSP issues:** Ensure `kspCommonMainKotlinMetadata` runs before other compilation tasks. Check generated sources in
`build/generated/ksp/metadata/commonMain/kotlin/`. Note: `kspCommonMainMetadata` should only be used if code is
meant to be generated directly into `commonMain`.

---

### Feature Processor

For detailed information on how to create feature interfaces for code generation with `:feature:processor`, see:
[Feature Processor Code Generation Guide](docs/feature-processor-guide.md)