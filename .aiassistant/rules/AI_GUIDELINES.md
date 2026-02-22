---
apply: always
---

## MOST IMPORTANT INFO

1. NEVER RUN TESTS IF NOT ASKED TO
2. NEVER TRY TO LIST FILES IN BUILD DIR IF NOT ASKED TO
3. NEVER ANALYZE BUILD DIR IF NOT ASKED TO
4. NEVER BUILD TARGETS OTHER THAN JVM IF NOT ASKED TO

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
  `Map<String, T>`.
- **Feature Descriptors:** Use `FeatureDescriptor` and `FeatureFactory` for typed feature access.
- **Factory Methods:** Prefer using the DSL-like factory methods in `FactoryMethods.kt` and `Property.Companion.of...`.
- **Validation:** Use the `validation` package rules for consistency.
- **Enum Pattern:** For API enums, use the `sealed interface` with `Strict` and `Unknown` subclasses to handle future
  API changes safely (see `AggregatedStatus` for reference).

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