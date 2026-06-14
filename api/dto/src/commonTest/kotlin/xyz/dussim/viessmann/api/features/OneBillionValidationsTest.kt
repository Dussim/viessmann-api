package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import xyz.dussim.viessmann.feature.api.FeatureRegistry

class OneBillionValidationsTest :
    FunSpec({
        val features = loadFeatures(FeatureRegistry::of)

        singleThreadedValidationTest("One billion validations test", features)
        parallelValidationTest("!Parallel One billion validations test", features)
        singleThreadedFailFastValidationTest("!One billion validations fail fast test", features)
        parallelFailFastValidationTest("!Parallel One billion validations fail fast test", features)
    })
