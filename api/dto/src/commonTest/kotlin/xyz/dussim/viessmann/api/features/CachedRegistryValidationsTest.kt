package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import xyz.dussim.viessmann.feature.api.FeatureRegistry

class CachedRegistryValidationsTest :
    FunSpec({
        val features = loadFeatures(FeatureRegistry::caching)

        singleThreadedValidationTest("!Cached registry validations test", features)
        parallelValidationTest("!Parallel cached registry validations test", features)
    })
