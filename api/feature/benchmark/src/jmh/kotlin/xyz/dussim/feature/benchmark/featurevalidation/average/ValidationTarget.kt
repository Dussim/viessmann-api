package xyz.dussim.feature.benchmark.featurevalidation.average

import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

/**
 * A validator paired with the set of features it should be exercised against.
 * The benchmark loops over these targets and calls `validator.validate(feature)`
 * on every feature — simulating the "stress validation" scenario from
 * `api/dto/src/commonTest/.../ValidationOnlyStressTest.kt`.
 */
internal class ValidationTarget(
    val validator: ValidationRule<Feature, ValidationError>,
    val features: List<Feature>,
)
