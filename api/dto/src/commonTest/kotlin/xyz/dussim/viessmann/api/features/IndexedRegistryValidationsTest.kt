package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import xyz.dussim.viessmann.feature.api.FeatureRegistry

class IndexedRegistryValidationsTest :
    FunSpec({
        val features = loadFeatures(FeatureRegistry::indexed)

        singleThreadedValidationTest("Indexed registry validations test", features)
        parallelValidationTest("!Parallel indexed registry validations test", features)
    })
