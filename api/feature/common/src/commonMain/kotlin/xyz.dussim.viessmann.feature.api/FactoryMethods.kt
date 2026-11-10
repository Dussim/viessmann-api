package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.GeneratedValidationRule
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import kotlin.reflect.KClass

fun Indexed(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Indexed =
    object : FeatureMatchers.Indexed {
        override val structureValidator = validation
        override val failFastStructureValidator = failFast

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(failFast)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, failFast)

        private val indexedMatchers = IndexedMatcherCache(wildcardName, failFast)

        override fun byName(index: Int): FeatureMatcher = indexedMatchers.byName(index)

        override fun byNameThenStructure(index: Int): FeatureMatcher = indexedMatchers.byNameThenStructure(index)
    }

fun Indexed(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Indexed =
    object : FeatureMatchers.Indexed {
        override val structureValidator = validation
        override val failFastStructureValidator = validation

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, validation)

        private val indexedMatchers = IndexedMatcherCache(wildcardName, validation)

        override fun byName(index: Int): FeatureMatcher = indexedMatchers.byName(index)

        override fun byNameThenStructure(index: Int): FeatureMatcher = indexedMatchers.byNameThenStructure(index)
    }

private const val CACHED_MATCHER_INDEX_COUNT = 16

/**
 * Indexed matchers of one descriptor for indices `0..<16`, created on first use. Lookups by index would otherwise
 * build the indexed name and three matcher objects on every call; a cached matcher also keeps its name's hash code.
 *
 * Not synchronized: descriptors are shared between threads, but a race only creates an equivalent duplicate matcher,
 * and matchers hold nothing but final fields. Indices outside the cached range get a new matcher per call.
 */
private class IndexedMatcherCache(
    private val wildcardName: String,
    private val structureRule: ValidationRule<Feature, ValidationError>,
) {
    private var byName: Array<FeatureMatcher?>? = null
    private var byNameThenStructure: Array<FeatureMatcher?>? = null

    fun byName(index: Int): FeatureMatcher {
        if (index !in 0..<CACHED_MATCHER_INDEX_COUNT) return createByName(index)
        val cache = byName ?: arrayOfNulls<FeatureMatcher>(CACHED_MATCHER_INDEX_COUNT).also { byName = it }
        return cache[index] ?: createByName(index).also { cache[index] = it }
    }

    fun byNameThenStructure(index: Int): FeatureMatcher {
        if (index !in 0..<CACHED_MATCHER_INDEX_COUNT) return createByNameThenStructure(index)
        val cache = byNameThenStructure ?: arrayOfNulls<FeatureMatcher>(CACHED_MATCHER_INDEX_COUNT).also { byNameThenStructure = it }
        return cache[index] ?: createByNameThenStructure(index).also { cache[index] = it }
    }

    private fun indexedName(index: Int): String = wildcardName.replace("{N}", index.toString())

    private fun createByName(index: Int): FeatureMatcher = FeatureMatcher.byName(indexedName(index))

    private fun createByNameThenStructure(index: Int): FeatureMatcher = FeatureMatcher.byNameThenStructure(indexedName(index), structureRule)
}

fun Static(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Static =
    object : FeatureMatchers.Static {
        override val structureValidator = validation
        override val failFastStructureValidator = failFast

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(failFast)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, failFast)
    }

fun Static(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Static =
    object : FeatureMatchers.Static {
        override val structureValidator = validation
        override val failFastStructureValidator = validation

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, validation)
    }

fun FeatureMatchers(
    featureName: String,
    validation: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
): FeatureMatchers =
    when (featureName.contains("{N}")) {
        true -> Indexed(featureName, validation, failFast)
        false -> Static(featureName, validation, failFast)
    }

inline fun <reified F : Feature> FeatureDescriptor(
    wildcardName: String,
    matchers: FeatureMatchers,
    noinline factory: (Feature) -> F,
    rule: ValidationRule<Feature, ValidationError>,
): FeatureDescriptor<F> =
    when (matchers) {
        is FeatureMatchers.Indexed -> {
            FeatureDescriptorIndexedImpl(
                featureClass = F::class,
                wildcardName = wildcardName,
                factory = FeatureFactory(F::class, factory),
                matchers = matchers,
                rule = rule,
            )
        }

        is FeatureMatchers.Static -> {
            FeatureDescriptorStaticImpl(
                featureClass = F::class,
                wildcardName = wildcardName,
                factory = FeatureFactory(F::class, factory),
                matchers = matchers,
                rule = rule,
            )
        }
    }

/**
 * Creates a descriptor when regular validation and fail-fast validation use different rules.
 *
 * Use this overload when fail-fast validation must stop after the first invalid rule while regular validation
 * still aggregates all validation results.
 */
inline fun <reified F : Feature> FeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor<F> =
    FeatureDescriptor(
        wildcardName = wildcardName,
        matchers = FeatureMatchers(wildcardName, rule, failFast),
        factory = factory,
        rule = rule,
    )

/**
 * Creates a static feature descriptor when the same rule is used for regular and fail-fast validation.
 *
 * This avoids generating or passing a separate fail-fast rule for cases where both validation paths have the
 * same behavior, such as descriptors backed by a single validation rule.
 */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Static(wildcardName, rule),
        rule = rule,
    )

/**
 * Creates a static feature descriptor when regular validation and fail-fast validation use different rules.
 *
 * Use this overload when regular validation should collect all structure errors but matcher validation should use
 * the supplied fail-fast rule.
 */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Static(wildcardName, rule, failFast),
        rule = rule,
    )

/**
 * Creates an indexed feature descriptor when regular validation and fail-fast validation use different rules.
 *
 * Use this overload when regular validation should collect all structure errors but matcher validation should use
 * the supplied fail-fast rule.
 */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Indexed(wildcardName, rule, failFast),
        rule = rule,
    )

/**
 * Creates an indexed feature descriptor when the same rule is used for regular and fail-fast validation.
 *
 * This avoids generating or passing a separate fail-fast rule for cases where both validation paths have the
 * same behavior, such as descriptors backed by a single validation rule.
 */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Indexed(wildcardName, rule),
        rule = rule,
    )

/**
 * Creates a static feature descriptor backed by a single-pass factory.
 *
 * [createOrNull] must return the converted feature exactly when [failFast] (and therefore [rule]) accepts it, and
 * `null` otherwise. [FeatureDescriptor.getOrNull] then calls it directly instead of validating first and constructing
 * afterwards, and [FeatureDescriptor.getOrThrow] throws [FeatureValidationException] naming [featureType] when it
 * returns `null`.
 */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    featureType: String,
    noinline createOrNull: (Feature) -> F?,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = SinglePassFeatureFactory(F::class, featureType, rule, createOrNull),
        matchers = Static(wildcardName, rule, failFast),
        rule = rule,
    )

/** Single-pass [staticFeatureDescriptor] for descriptors whose regular and fail-fast validation are the same rule. */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    featureType: String,
    noinline createOrNull: (Feature) -> F?,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = SinglePassFeatureFactory(F::class, featureType, rule, createOrNull),
        matchers = Static(wildcardName, rule),
        rule = rule,
    )

/** Single-pass [indexedFeatureDescriptor]; see the single-pass [staticFeatureDescriptor] for the [createOrNull] contract. */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    featureType: String,
    noinline createOrNull: (Feature) -> F?,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = SinglePassFeatureFactory(F::class, featureType, rule, createOrNull),
        matchers = Indexed(wildcardName, rule, failFast),
        rule = rule,
    )

/** Single-pass [indexedFeatureDescriptor] for descriptors whose regular and fail-fast validation are the same rule. */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    featureType: String,
    noinline createOrNull: (Feature) -> F?,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = SinglePassFeatureFactory(F::class, featureType, rule, createOrNull),
        matchers = Indexed(wildcardName, rule),
        rule = rule,
    )

/**
 * Factory that validates while it constructs: [createOrNull] returns `null` exactly when [rule] reports errors, so
 * neither conversion needs a separate validation pass. [rule] only runs (lazily) to build the exception message.
 */
@PublishedApi
internal class SinglePassFeatureFactory<F : Feature>(
    override val featureClass: KClass<F>,
    private val featureType: String,
    private val rule: ValidationRule<Feature, ValidationError>,
    private val createOrNull: (Feature) -> F?,
) : SinglePassFactory<F> {
    override fun getOrNull(feature: Feature): F? = createOrNull(feature)

    override fun getOrThrow(feature: Feature): F = createOrNull(feature) ?: throw FeatureValidationException(featureType, feature, rule)
}

/** A factory whose [getOrNull] validates while it constructs; see [SinglePassDescriptor]. */
@PublishedApi
internal interface SinglePassFactory<F : Feature> : FeatureFactory<F>

/**
 * Implemented by the companion of a generated feature implementation: its validation rules plus the single-pass
 * factory. [createOrNull] returns the implementation exactly when [validateFailFast] (and therefore [validate])
 * accepts the feature, and `null` otherwise.
 */
interface GeneratedFeatureCompanion<out F : Feature> : GeneratedValidationRule<Feature, ValidationError> {
    fun createOrNull(sourceFeature: Feature): F?
}

/**
 * Single-pass factory over a generated companion. One implementation may serve several feature interfaces, so the
 * check for a feature that already has the target type is made here, against [featureClass], before converting.
 */
@PublishedApi
internal class GeneratedFeatureFactory<F : Feature>(
    override val featureClass: KClass<F>,
    private val featureType: String,
    private val generated: GeneratedFeatureCompanion<F>,
) : SinglePassFactory<F> {
    override fun getOrNull(feature: Feature): F? {
        if (featureClass.hasInstance(feature)) {
            @Suppress("UNCHECKED_CAST")
            return feature as F
        }
        return generated.createOrNull(feature)
    }

    override fun getOrThrow(feature: Feature): F = getOrNull(feature) ?: throw FeatureValidationException(featureType, feature, generated)
}

/** The fail-fast side of a generated companion as a rule. Not inline, so all descriptors share one class for it. */
@PublishedApi
internal fun failFastRule(generated: GeneratedFeatureCompanion<*>): ValidationRule<Feature, ValidationError> =
    ValidationRule { feature -> generated.validateFailFast(feature) }

/**
 * Creates a static feature descriptor from the companion of a generated implementation, which is both its rule and
 * its single-pass factory. [hasSeparateFailFast] tells whether the companion's fail-fast validation differs from its
 * regular validation; when it does not, the companion itself serves as the fail-fast rule.
 */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    generated: GeneratedFeatureCompanion<F>,
    hasSeparateFailFast: Boolean,
    featureType: String,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = GeneratedFeatureFactory(F::class, featureType, generated),
        matchers = if (hasSeparateFailFast) Static(wildcardName, generated, failFastRule(generated)) else Static(wildcardName, generated),
        rule = generated,
    )

/** Indexed counterpart of the [staticFeatureDescriptor] for generated companions. */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    generated: GeneratedFeatureCompanion<F>,
    hasSeparateFailFast: Boolean,
    featureType: String,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = GeneratedFeatureFactory(F::class, featureType, generated),
        matchers = if (hasSeparateFailFast) Indexed(wildcardName, generated, failFastRule(generated)) else Indexed(wildcardName, generated),
        rule = generated,
    )

/**
 * Implemented by descriptors whose [FeatureFactory.getOrNull] may replace a separate structure check: when
 * [convertsInSinglePassFor] returns `true` for a structure rule, `getOrNull` returns `null` exactly when that rule
 * rejects the feature, for every feature that is not already an instance of the descriptor's feature class.
 */
@PublishedApi
internal interface SinglePassDescriptor {
    fun convertsInSinglePassFor(structureRule: ValidationRule<Feature, *>): Boolean
}

@PublishedApi
internal class FeatureDescriptorIndexedImpl<F : Feature>(
    override val featureClass: KClass<F>,
    override val wildcardName: String,
    private val factory: FeatureFactory<F>,
    private val matchers: FeatureMatchers.Indexed,
    rule: ValidationRule<Feature, ValidationError>,
) : FeatureDescriptor.Indexed<F>,
    FeatureFactory<F> by factory,
    FeatureMatchers.Indexed by matchers,
    ValidationRule<Feature, ValidationError> by rule,
    SinglePassDescriptor {
    private val singlePass = factory as? SinglePassFactory<F>

    override fun getOrNull(feature: Feature): F? {
        val singlePass = singlePass
        if (singlePass != null) return singlePass.getOrNull(feature)
        return getOrNullWithoutValidationException(feature, featureClass, factory, matchers.byStructure)
    }

    override fun convertsInSinglePassFor(structureRule: ValidationRule<Feature, *>): Boolean =
        singlePass != null && matchers.failFastStructureValidator === structureRule

    override fun toString(): String = "FeatureDescriptor.Indexed[$wildcardName](${featureClass.simpleName})"
}

@PublishedApi
internal class FeatureDescriptorStaticImpl<F : Feature>(
    override val featureClass: KClass<F>,
    override val wildcardName: String,
    private val factory: FeatureFactory<F>,
    private val matchers: FeatureMatchers.Static,
    rule: ValidationRule<Feature, ValidationError>,
) : FeatureDescriptor.Static<F>,
    FeatureFactory<F> by factory,
    FeatureMatchers.Static by matchers,
    ValidationRule<Feature, ValidationError> by rule,
    SinglePassDescriptor {
    private val singlePass = factory as? SinglePassFactory<F>

    override fun getOrNull(feature: Feature): F? {
        val singlePass = singlePass
        if (singlePass != null) return singlePass.getOrNull(feature)
        return getOrNullWithoutValidationException(feature, featureClass, factory, matchers.byStructure)
    }

    override fun convertsInSinglePassFor(structureRule: ValidationRule<Feature, *>): Boolean =
        singlePass != null && matchers.failFastStructureValidator === structureRule

    override fun toString(): String = "FeatureDescriptor.Static[$wildcardName](${featureClass.simpleName})"
}

private fun <F : Feature> getOrNullWithoutValidationException(
    feature: Feature,
    featureClass: KClass<F>,
    factory: FeatureFactory<F>,
    structureMatcher: FeatureMatcher,
): F? {
    if (featureClass.hasInstance(feature)) {
        @Suppress("UNCHECKED_CAST")
        return feature as F
    }
    if (!structureMatcher.matches(feature)) return null
    return factory.getOrNull(feature)
}
