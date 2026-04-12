package xyz.dussim.viessmann.feature.api

/**
 * JVM actual: stackless singleton. `writableStackTrace = false` in the `RuntimeException` constructor suppresses
 * stack-trace filling; no explicit `fillInStackTrace` override required. See the common `expect` for the
 * sentinel contract.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "ObjectInheritsException")
@PublishedApi
internal actual object GeneratedAccessException : RuntimeException(null, null, false, false) {
    @Suppress("unused") // used by Java object deserialization to preserve singleton identity
    private fun readResolve(): Any = GeneratedAccessException
}
