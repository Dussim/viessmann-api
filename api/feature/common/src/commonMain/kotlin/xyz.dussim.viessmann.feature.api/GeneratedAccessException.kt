package xyz.dussim.viessmann.feature.api

/**
 * Sentinel thrown by generated feature/command init blocks on missing-key or type-mismatch access failures.
 *
 * Contract:
 * - Never user-observable. Always caught inside the generated init block and rewrapped into
 *   [FeatureValidationException] / [CommandValidationException], which run full validation to produce the real
 *   diagnostic.
 * - Carries no message, no kind, no expected/actual context. The rewrapping validation pass provides those.
 * - Modelled as an `object` so JVM can skip stack-trace filling and jump straight to the catch block.
 *
 * Suppressing `ObjectInheritsException` is intentional: this singleton exists specifically to eliminate the
 * per-throw stack fill cost on the fail-fast path.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "ObjectInheritsException")
@PublishedApi
internal expect object GeneratedAccessException : RuntimeException
