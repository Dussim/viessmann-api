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

### Results
Benchmark reports are generated in `api/feature/benchmark/build/reports/jmh/`:
- **JSON**: `results.json`
- **Human-readable**: `human.txt`

## Key Benchmark Scenarios
- `ValidationBenchmark`: Performance of feature validation logic.
- `EfficientMapBenchmark`: Microbenchmarks comparing `EfficientStringKeyMap` against standard `Map` implementations.

## Usage
The benchmarks are JVM-only. They are intended for developers to monitor performance regressions and optimize the feature system.
