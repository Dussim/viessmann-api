package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import xyz.dussim.viessmann.feature.api.FeatureRegistry

class BasicRegistryValidationsTest :
    FunSpec({
        val features = loadFeatures(FeatureRegistry::of)

        singleThreadedValidationTest("!Large number of validations performance testing", features)
        parallelValidationTest("!Parallel large number of validations performance testing", features)
    })
