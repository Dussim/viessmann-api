---
apply: always
---

## MOST IMPORTANT INFO

1. **BE SUPER SAFE:** When running commands, NEVER analyze or modify build folders or temporary artifacts.
2. **OPERATE LOCALLY:** All Git work is LOCAL ONLY — commit only, never push or open PRs. No `git push`, `gh pr create`, or equivalent.
3. **JVM FIRST:** Prioritize JVM builds and tests. Only build or test other targets (JS/Native) if explicitly requested, as they are slower and may require specific environmental setups.
4. **TESTING:** Never run tests unless explicitly asked to do so by the task or to verify your own changes. Always run ALL relevant tests related to your changes.
5. **BUILD:** Never build targets other than JVM if not specifically asked.

### Agent Workflow Best Practices

- **Communication:** Use `update_status` to keep the user informed about progress. Always provide a clear, concise plan before taking multi-step actions.
- **Verification:** Always verify your changes. If you write code, add tests. If you fix a bug, add a reproduction test.
- **Read-Only Context:** Use `[ADVANCED_CHAT]` mode for exploring the codebase. Switch to `[CODE]` mode only when changes are necessary.
- **Naming:** Follow existing naming conventions strictly.
- **Cleanliness:** Do not create temporary files/folders unless necessary. Clean up after yourself if you created temporary artifacts.

### Build & Configuration

**Requirements:** Java 21+ on PATH, Node.js (for JS tests)

**Java:** Do NOT set or override `JAVA_HOME`. Java 21+ is already on PATH. Run all commands using the system `java` directly. Gradle wrapper will pick it up automatically.

**Build commands:**

```powershell
./gradlew.bat build                    # Build all modules (use cautiously)
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
./gradlew.bat test                         # Run all tests (all modules/targets - use cautiously)
./gradlew.bat :api:dto:jvmTest             # JVM tests for api:dto
./gradlew.bat :api:feature:common:jvmTest  # JVM tests for feature:common
./gradlew.bat :api:dto:jsNodeTest          # JS Node tests (if requested)
./gradlew.bat :api:dto:jsBrowserTest       # JS Browser tests (if requested)

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
- **Feature Registry:** Use `FeatureRegistry` to find and convert features from a list. Create instances via `FeatureRegistry(features)` (indexed+caching, default), `FeatureRegistry.indexed(features)`, `FeatureRegistry.caching(features)`, or `FeatureRegistry.of(features)` (basic, no caching). The `indexed` variant builds jump tables at construction time for O(1)/O(k) lookups by dispatching on internal `FeatureMatcher` types. It provides convenient methods like `findOf`, `firstOf`, `allOf`, and operator `get` for descriptor-based lookups.
- **Factory Methods:** Prefer using the DSL-like factory methods in `FactoryMethods.kt` and `Property.of...`.
- **Validation:** Use the `validation` package rules for consistency.
- **Enum Pattern:** For API enums, use the `sealed interface` with `Strict` and `Unknown` subclasses to handle future
  API changes safely.
- **FeatureEnumFactory:** When a feature property is an enum-like string, implement `FeatureEnumFactory` in the property's companion object to provide type-safe conversion from `StringValue`.
- **Property Access:** Always use the provided type-safe property accessors in generated features instead of manually querying the `properties` map.

---

### Project Conventions & Tooling

- **Convention Plugins:** The project uses custom convention plugins (e.g., `xyz.dussim.kotlin.common`, `xyz.dussim.generate.*`). When working on `build.gradle.kts` files, prefer applying these plugins rather than manually configuring dependencies, KSP, or serialization. Assume they handle essential boilerplate.
- **Version Catalog:** Always use `gradle/libs.versions.toml` to reference plugin and library versions. Do not hardcode versions in `build.gradle.kts`.
- **Anonymization:** For any generated or test-related JSON processing, check if the `xyz.dussim.anonymize.json` plugin or related utilities are applicable.

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

**JAVA_HOME issues on Windows:** Do not set `JAVA_HOME` manually. Java 21+ must be on PATH. If Gradle complains about `JAVA_HOME`, unset it and rely on PATH.

**Configuration Cache:** Enabled by default. Problems report: `build/reports/problems/problems-report.html`

**KSP issues:** Ensure `kspCommonMainKotlinMetadata` runs before other compilation tasks. Check generated sources in
`build/generated/ksp/metadata/commonMain/kotlin/`. Note: `kspCommonMainMetadata` should only be used if code is
meant to be generated directly into `commonMain`.

---

### Feature Processor

For detailed information on how to create feature interfaces for code generation with `:feature:processor`, see:
[Feature Processor Code Generation Guide](docs/feature-processor-guide.md)