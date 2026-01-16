package xyz.dussim.viessmann.api.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import xyz.dussim.viessmann.api.testing.readFilesContentsIn
import xyz.dussim.viessmann.api.utils.json

class ViessmannModelsTest :
    FunSpec({
        context("parses installations") {
            withData(readFilesContentsIn("models/installation")) { (_, content) ->
                val installations = json.decodeFromString<ResponseData<JsonObject>>(content).data

                withData(
                    nameFn = { (it["id"] as JsonPrimitive).content },
                    ts = installations,
                ) {
                    json.decodeFromJsonElement<Installation>(it)
                }
            }
        }

        context("parses gateways") {
            withData(readFilesContentsIn("models/gateway")) { (_, content) ->
                val gateways = json.decodeFromString<ResponseData<JsonObject>>(content).data

                withData(
                    nameFn = { (it["serial"] as JsonPrimitive).content },
                    ts = gateways,
                ) {
                    json.decodeFromJsonElement<Gateway>(it)
                }
            }
        }

        context("parses devices") {
            withData(readFilesContentsIn("models/device")) { (_, content) ->
                val devices = json.decodeFromString<ResponseData<JsonObject>>(content).data

                withData(
                    nameFn = { (it["id"] as JsonPrimitive).content },
                    ts = devices,
                ) {
                    json.decodeFromJsonElement<Device>(it)
                }
            }
        }
    })
