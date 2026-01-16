package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.testing.readFilesContentsIn
import xyz.dussim.viessmann.api.utils.json
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.GatewayFeature

class ViessmannFeatureParsingTest :
    FunSpec(
        {
            context("Parsing all device features") {
                withData(readFilesContentsIn("features/device")) { (_, content) ->
                    val features = json.decodeFromString<ResponseData<JsonObject>>(content).data
                    withData(
                        nameFn = { (it["feature"] as JsonPrimitive).content },
                        ts = features,
                    ) { featureJsonElement ->
                        json.decodeFromJsonElement(DeviceFeature.serializer(), featureJsonElement)
                    }
                }
            }

            context("Parsing all gateway features") {
                withData(readFilesContentsIn("features/gateway")) { (_, content) ->
                    val features = json.decodeFromString<ResponseData<JsonObject>>(content).data
                    withData(
                        nameFn = { (it["feature"] as JsonPrimitive).content },
                        ts = features,
                    ) { featureJsonElement ->
                        json.decodeFromJsonElement(GatewayFeature.serializer(), featureJsonElement)
                    }
                }
            }
        },
    )
