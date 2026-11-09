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

**Requirements:** Java 25+ on PATH, Node.js (for JS tests)

**Java:** Do NOT set or override `JAVA_HOME`. Java 25+ is already on PATH. Run all commands using the system `java` directly. Gradle wrapper will pick it up automatically.

**Build commands:**

Project names are set in `settings.gradle.kts`, so Gradle paths differ from directory names (`api/dto` is `:api:api-dto`).

```powershell
./gradlew.bat build                    # Build all modules (use cautiously)
./gradlew.bat :api:api-dto:build       # Build specific module
./gradlew.bat clean build              # Clean build
```

**KSP Code Generation:** KSP2 generates feature implementations from interfaces annotated with
`@GenerateFeatureImplementation`. `:api:feature:api-feature-implementations` runs it per target through `kspKotlinJvm` and
`kspKotlinJs`, after `generateFeatureInterfaces`. Output is under `build/generated/ksp/jvm/jvmMain/kotlin` and
`build/generated/ksp/js/jsMain/kotlin`.

---

### Testing

**Framework:** TestBalloon with Kotest assertions. JVM tests run on JUnit Platform.

**Test commands:**

```powershell
./gradlew.bat test                                     # Run all tests (all modules/targets - use cautiously)
./gradlew.bat :api:api-dto:jvmTest                     # JVM tests for api-dto
./gradlew.bat :api:feature:api-feature-common:jvmTest  # JVM tests for api-feature-common
./gradlew.bat :api:api-dto:jsNodeTest                  # JS Node tests (if requested)
./gradlew.bat :api:api-dto:jsBrowserTest               # JS Browser tests (if requested)
```

**Adding new tests:**

1. Create test file in `src/commonTest/kotlin/` (for multiplatform) or `src/jvmTest/kotlin/` (JVM-only)
2. Declare a top-level `val ExampleTest by testSuite { ... }`
3. Use `test("description") { ... }` and nested `testSuite("group") { ... }` blocks

**Test file example:**

```kotlin
package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ExampleTest by testSuite {
    test("example test") {
        val result = 1 + 1
        result shouldBe 2
    }

    testSuite("grouped tests") {
        test("first") {
            true shouldBe true
        }
    }
}
```

**Test dependencies** (automatically provided by convention plugin `xyz.dussim.kotlin.common`):

- `testBalloon-framework-core` - test framework
- `kotest-assertions-core` - assertions (`shouldBe`, `shouldContain`, etc.)
- `testBalloon-integration-kotest-assertions` - TestBalloon and Kotest assertion integration
- `junit-platform-launcher` - JVM launcher (jvmTest only)

---

### Code Style & Patterns

**Formatter:** Kotlinter with ktlint_official style
**Max line length:** 180 characters

**Multiplatform:** This is a Kotlin Multiplatform project.
- `commonMain`: Shared logic, DTOs, interfaces.
- `jvmMain`: JVM-specific implementations (e.g., specialized performance optimizations).
- `webMain`: Code shared by web targets; `jsMain` depends on it.
- Always prefer `commonMain` unless platform-specific APIs are required.
- Use `expect`/`actual` for platform-specific functionality.

```powershell
./gradlew.bat formatKotlin    # Auto-format code
./gradlew.bat lintKotlin      # Check formatting
./gradlew.bat check           # Run all verifications (tests + lint)
```

**Style conventions:**

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

### Module Structure

See `README.adoc` at the repository root for the module table.

**Module notes:**

- **`api-dto`:** API enums have an `Unknown` case so new API values do not break decoding.
- **`api-auth`:** SAML-specific error payloads are intentionally separate from the shared error DTO.
- **`api-errors`:** `extendedPayload` and validation `context` are intentionally modeled as `JsonObject`.
- **`api-equipment`:** Status payloads intentionally model the endpoint responses, not the full equipment domain.
- **`api-users`:** Some nested response objects are intentionally partial because the upstream schema is loose.
- **`api-feature-annotations`:** Must stay a leaf module; every feature module and the processor depend on it.
- **`api-feature-common`:** `EfficientStringKeyMap` dispatches on an integer kind to keep call sites inlinable. Do not replace it with virtual dispatch without benchmarks.
- **`api-feature-processor`:** Update `FeatureSymbolValidator` and `docs/feature-processor-guide.adoc` together.
- **`api-feature-implementations`:** JSON fixture tests live here because they need concrete descriptors.
- **`client-core`:** The client sets `expectSuccess = false` because status mapping happens in the request helpers.

---

### Benchmarks (JVM only)

```powershell
./gradlew.bat :api:feature:api-feature-benchmark:jmh
```

Reports: `api/feature/benchmark/build/reports/jmh/results.json`

---

### Troubleshooting

**JAVA_HOME issues on Windows:** Do not set `JAVA_HOME` manually. Java 25+ must be on PATH. If Gradle complains about `JAVA_HOME`, unset it and rely on PATH.

**Configuration Cache:** Enabled by default. Problems report: `build/reports/problems/problems-report.html`

**KSP issues:** Ensure `generateFeatureInterfaces` runs before `kspKotlinJvm` and `kspKotlinJs`. Check generated sources in
`api/feature/implementations/build/generated/ksp/`.

---

### Feature Processor

For detailed information on how to create feature interfaces for code generation with `:api:feature:api-feature-processor`, see:
[Feature Processor Code Generation Guide](docs/feature-processor-guide.adoc)
