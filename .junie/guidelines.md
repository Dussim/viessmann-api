### Build & Configuration

**Requirements:** Java 21+, Node.js (for JS tests)

**Build commands:**
```powershell
./gradlew.bat build                    # Build all modules
./gradlew.bat :api:dto:build           # Build specific module
./gradlew.bat clean build              # Clean build
```

**KSP Code Generation:** KSP2 generates feature implementations from annotations. Generated sources are in `build/generated/ksp/`. All compilation tasks depend on `kspCommonMainKotlinMetadata` - do not change this dependency order.

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

### Code Style

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

---

### Module Structure

| Module | Description |
|--------|-------------|
| `:api:dto` | Multiplatform DTOs, feature models. Uses KSP-generated sources. |
| `:api:feature:annotations` | Annotation definitions for KSP processor |
| `:api:feature:processor` | KSP2 processor generating feature implementations |
| `:api:feature:common` | Core types (`Feature`, `Property`, `Command`), validation, efficient maps |
| `:api:feature:benchmark` | JMH benchmarks (JVM only) |

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

**KSP issues:** Ensure `kspCommonMainKotlinMetadata` runs before other compilation tasks. Check generated sources in `build/generated/ksp/metadata/commonMain/kotlin/`.
