package xyz.dussim.viessmann.api.testing

import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
// import xyz.dussim.viessmann.api.features.ViessmannFeatureSerializersModule

abstract class ParseSpec(
    body: FunSpec.(Json) -> Unit = {},
) : FunSpec({
        val json =
            Json {
                explicitNulls = false
                encodeDefaults = false
                useAlternativeNames = false

                ignoreUnknownKeys = true

                serializersModule =
                    SerializersModule {
//                        include(ViessmannFeatureSerializersModule)
                    }
            }

        body(json)
    })
