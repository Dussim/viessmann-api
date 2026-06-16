package xyz.dussim.viessmann.feature.api

/**
 * Web actual: plain [RuntimeException]. JS/Wasm has no equivalent of `writableStackTrace = false`, so the
 * stackless perf win is JVM-only. See the common `expect` for the sentinel contract.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "ObjectInheritsException")
actual object GeneratedAccessException : RuntimeException()
