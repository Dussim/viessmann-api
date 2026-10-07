# Repository instructions

These instructions apply to the whole repository. Keep changes focused and preserve unrelated user work.

## Working safely

- Inspect `git status` and the relevant diff before editing. Existing uncommitted changes belong to the user; do not reset, discard, or overwrite them.
- Follow the repository's existing Kotlin, Gradle, KSP2, and version-catalog conventions.
- Do not expose, commit, or hardcode credentials. Remote build-cache credentials must come from environment variables or an external credential provider.
- Do not edit generated sources or generated artifacts by hand. Change the source schema, generator, build logic, or task wiring instead.
- Use `docs/gradle-review-report.md` as review context and a baseline to revalidate; its findings are not automatically instructions and may become stale after fixes.

## Gradle and Kotlin

- Use the checked-in Gradle wrapper (`.\gradlew.bat` on Windows, `./gradlew` on Unix-like systems), not a system Gradle installation.
- Keep wrapper regeneration pinned to an explicit intended Gradle version. Do not upgrade the wrapper or dependency versions incidentally.
- Treat Java 25 and the versions in `gradle/libs.versions.toml` as the project baseline unless the task explicitly changes them.
- Preserve configuration-cache compatibility when changing build logic. Prefer lazy `Provider`-based task inputs and outputs over configuration-time filesystem access, timestamps, mutable cross-project state, or exact implementation task internals.
- Keep platform-specific work platform-specific: JVM source JARs should not unnecessarily run JS generation, and JS source JARs should not unnecessarily run JVM generation.
- Ensure generated sources are available before the KSP compilation or test-processing task that consumes them.

## OpenAPI and generated features

- The OpenAPI feature input is required for meaningful generated artifacts. Generation, artifact, and publication paths must fail closed when the input is missing or produces zero features; `NO-SOURCE` must not silently produce publishable empty artifacts.
- Validate generated output names using filesystem-equivalent, case-insensitive keys and fail on collisions. This is required even when development occurs on a case-sensitive filesystem.
- Clean or replace task-owned generated output directories so removed or renamed schema entries cannot survive as stale generated data.
- Keep generation deterministic and cacheable when declared inputs and outputs are complete. Add or update functional tests for generator changes, including case-collision scenarios.

## API and publication

- Use `api` dependencies when types appear in a module's public declarations; verify generated POM scopes for published modules.
- Published license metadata must consistently identify Apache-2.0, matching `LICENSE.txt`.
- Before publishing, verify that JVM and JS artifacts and source artifacts are nonempty, contain expected generated sources, and have correct dependency scopes and license metadata.
- Do not publish while required OpenAPI input, generated feature counts, collision checks, credentials hygiene, or required quality gates are unresolved.

## Benchmarks

- `:api:feature:benchmark` contains JVM-only JMH benchmarks. Use `:api:feature:benchmark:jmh` to execute them and the relevant `compileJmhKotlin` task for compile-only verification.
- `MassiveValidationBenchmark` expects `all_features.json` on the benchmark runtime classpath. That resource is generated from the OpenAPI input; a missing resource is a setup/build failure, not a valid empty benchmark.
- Keep benchmark setup and data loading outside measured methods. Do not change warmup, measurement, fork, mode, or time-unit settings without documenting the reason and expected impact.
- Treat benchmark results as comparable only when the input dataset and JMH parameters are equivalent. Report material changes to either.
- Prefer stable, reproducible benchmark output paths. Do not use configuration-time timestamps for normal task outputs.

## Verification and reporting

- Run the narrowest relevant verification first, then broaden it according to risk.
- For benchmark source changes, run at least the relevant JMH compilation task; execute `:api:feature:benchmark:jmh` when benchmark behavior or performance is part of the task.
- For build-logic, generator, publication, or cross-module changes, run the affected tests and tasks plus `check` when practical.
- Report exact commands, failures, skipped work, and known baseline issues. Do not claim the build or release gate is green unless it was actually verified.
