### AI Hints for :api:feature:benchmark

This module contains JMH benchmarks for measuring feature system performance.

#### Key Benchmarks
- **`ValidationBenchmark.kt`**: Benchmarks different validation scenarios (valid/invalid, small/large features).
- **Map Benchmarks**: `OneElementMapBenchmark.kt` etc., used to evaluate the efficiency of specialized collections versus standard Kotlin/Java maps.

#### Execution Details
- **Environment**: Benchmarks should be run in a stable environment.
- **Commands**: Use `./gradlew.bat :api:feature:benchmark:jmh` for execution.
- **Reporting**: JSON results are in `build/reports/jmh/results.json`.

#### AI Context
- **Stability and Performance**: When suggesting performance optimizations, reference benchmarks in this module to justify changes.
- **Regressions**: If a change in `:api:feature:common` or `:api:feature:processor` is made, consider adding or running a benchmark here to detect performance regressions.
- **JVM Only**: This module is JVM-only. Benchmark results apply primarily to the JVM target but give a hint for JS performance trends.
