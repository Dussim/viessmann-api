package xyz.dussim.viessmann.api.models

import de.infix.testBalloon.framework.core.testSuite
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import xyz.dussim.viessmann.api.testing.readFilesContentsIn
import xyz.dussim.viessmann.api.utils.json

val ViessmannModelsTest by testSuite {
    testSuite("parses installations") {
        readFilesContentsIn("models/installation").forEach { (fileName, content) ->
            testSuite(fileName) {
                val installations = json.decodeFromString<ResponseData<JsonObject>>(content).data

                installations.forEach { item ->
                    val id = (item["id"] as JsonPrimitive).content
                    test(id) {
                        json.decodeFromJsonElement<Installation>(item)
                    }
                }
            }
        }
    }

    testSuite("parses gateways") {
        readFilesContentsIn("models/gateway").forEach { (fileName, content) ->
            testSuite(fileName) {
                val gateways = json.decodeFromString<ResponseData<JsonObject>>(content).data

                gateways.forEach { item ->
                    val serial = (item["serial"] as JsonPrimitive).content
                    test(serial) {
                        json.decodeFromJsonElement<Gateway>(item)
                    }
                }
            }
        }
    }

    testSuite("parses devices") {
        readFilesContentsIn("models/device").forEach { (fileName, content) ->
            testSuite(fileName) {
                val devices = json.decodeFromString<ResponseData<JsonObject>>(content).data

                devices.forEach { item ->
                    val id = (item["id"] as JsonPrimitive).content
                    test(id) {
                        json.decodeFromJsonElement<Device>(item)
                    }
                }
            }
        }
    }
}
