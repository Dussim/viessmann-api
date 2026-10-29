# Module :api:feature:benchmark

## Overview
This module contains JMH (Java Microbenchmark Harness) benchmarks for the Viessmann API feature system. It is used to assess performance characteristics, such as parsing, validation, and map access speed.

## Running Benchmarks

### Using Gradle
Run all benchmarks:
```powershell
./gradlew.bat :api:feature:benchmark:jmh
```

Specific benchmark:
```powershell
./gradlew.bat :api:feature:benchmark:jmh --jmh-includes=".*ValidationBenchmark.*"
```

Run the versioned stable validation acceptance workload (the correctness oracle runs first):

```powershell
./gradlew.bat :api:feature:api-feature-benchmark:stableValidationJmh
```

Each workload size is also an independently runnable suite:

```powershell
./gradlew.bat :api:feature:api-feature-benchmark:stableSmallValidationJmh
./gradlew.bat :api:feature:api-feature-benchmark:stableMediumValidationJmh
./gradlew.bat :api:feature:api-feature-benchmark:stableLargeValidationJmh
```

These are typed `me.champeau.jmh.JMHTask` tasks selecting benchmarks from the corresponding `stable.small`,
`stable.medium`, and `stable.large` packages. Their benchmark classes define
three warmup iterations, five measurement iterations, one fork, one thread, one validation per invocation, and
nanoseconds per operation. One fork stays within the default three-fork run's own error (median deviation 1.3%)
and takes a third of the time. The Gradle configuration fixes a 2 GiB heap and enables the GC profiler.

The workload is generated from benchmark-owned `StableSmallFeature`, `StableMediumFeature`, and `StableLargeFeature`
interfaces. `stable-validation/v1/fixture-manifest.json` is the versioned input contract: it records every canonical
fixture hash, interface/rule counts, expected validity, exact aggregate error categories, expected first error, and the
single-operation invocation count. Fixture parsing and construction happen once in JMH trial setup and are not timed.

Alongside raw JMH JSON, the task writes `build/reports/jmh/environment.json` with the commit, manifest hash, JDK, OS,
CPU identifier, JVM flags, and measurement settings. Compare base and candidate raw JSON on the same idle worker; do
not use a developer-machine absolute score as an acceptance threshold.

### Results
Benchmark reports are generated in `api/feature/benchmark/build/reports/jmh/`:
- **Combined JSON**: `results.json`
- **Size-specific JSON**: `stable-small-results.json`, `stable-medium-results.json`, and `stable-large-results.json`
- **Environment fingerprint**: `environment.json` for stable runs
- **Human-readable**: `human.txt`

## One billion validations test
`OneBillionValidationsTest` runs at least one billion structure-matcher validations: six small benchmark-owned feature
definitions (`billion.definitions`) against a registry built from `src/test/resources/one-billion-validations/features.json`
(19 entries, decoded 500 times). Unlike `all_features.json`, the input does not depend on the OpenAPI schema, so the
expected match counts are stable. The fixture's properties, commands and parameter maps cover every
`EfficientStringKeyMap` representation (empty, 1–4 entries, N entries, and pass-through for 64+ character keys).
Fast checks of that coverage and of the expected matches always run.

Like the original test's `allOf(descriptor, descriptor.byStructure)` calls, every feature is validated by structure
and each match is converted to its typed feature, with the match count asserted. Matches are only counted, not
collected into a list or a registry cache, so the run measures validation and conversion. It runs single-threaded and
in parallel (one task per descriptor per round): two runs of at least one billion validations each.

The billion-validation runs are opt-in because they take a long time:

```powershell
$env:ONE_BILLION_VALIDATIONS = "true"; ./gradlew.bat :api:feature:api-feature-benchmark:test
```

## Key Benchmark Scenarios
- `ValidationBenchmark`: Performance of feature validation logic.
- `EfficientMapBenchmark`: Microbenchmarks comparing `EfficientStringKeyMap` against standard `Map` implementations.

## Usage
The benchmarks are JVM-only. They are intended for developers to monitor performance regressions and optimize the feature system.
