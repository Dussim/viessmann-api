package xyz.dussim.viessmann.api.features

import io.kotest.datatest.withData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.testing.ParseSpec
import xyz.dussim.viessmann.api.testing.readFilesContentsIn

expect fun readFileFromResources(path: String): String

class ViessmannFeatureParsingTest :
    ParseSpec(
        { json ->
            var biggestCommand: ViessmannFeatureCommand? = null
            var maxCommandParams = 0
            afterSpec {
                println(
                    "Max number of command parameters: $maxCommandParams",
                )
                println(
                    "Biggest command: $biggestCommand",
                )
            }

            context("Parsing all device features") {
                withData(readFilesContentsIn("features/device")) { (_, content) ->
                    val features = json.decodeFromString<ResponseData<JsonObject>>(content).data
                    withData(
                        nameFn = { (it["feature"] as JsonPrimitive).content },
                        ts = features,
                    ) { featureJsonElement ->
                        val feature = json.decodeFromJsonElement<ViessmannFeature.Device>(featureJsonElement)
                        val command = feature.commands.maxByOrNull { (_, command) -> command.params.size }
                        if (maxOf(maxCommandParams, command?.value?.params?.size ?: maxCommandParams) == command?.value?.params?.size) {
                            biggestCommand = command.value
                            maxCommandParams = command.value.params.size
                        }
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
                        val feature = json.decodeFromJsonElement<ViessmannFeature.Gateway>(featureJsonElement)
                        val command = feature.commands.maxByOrNull { (_, command) -> command.params.size }
                        if (maxOf(maxCommandParams, command?.value?.params?.size ?: maxCommandParams) == command?.value?.params?.size) {
                            biggestCommand = command.value
                            maxCommandParams = command.value.params.size
                        }
                    }
                }
            }
        },
    )
