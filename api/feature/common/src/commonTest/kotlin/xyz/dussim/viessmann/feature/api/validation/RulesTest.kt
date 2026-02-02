package xyz.dussim.viessmann.feature.api.validation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.GatewayFeature
import xyz.dussim.viessmann.feature.api.GeofencingFeature
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListEmptyValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.OtherRoomConfiguration
import xyz.dussim.viessmann.feature.api.Parameter
import xyz.dussim.viessmann.feature.api.Property
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.of
import kotlin.time.Instant

class RulesTest :
    FunSpec({
        fun createDeviceFeature(
            properties: Map<String, Property> = emptyMap(),
            commands: Map<String, Command> = emptyMap(),
        ): Feature.Device =
            DeviceFeature(
                feature = "test.feature",
                deviceId = "device1",
                gatewayId = "gateway1",
                isEnabled = true,
                isReady = true,
                apiVersion = 1,
                timestamp = Instant.fromEpochMilliseconds(0),
                uri = "http://test",
                properties = EfficientStringKeyMap(properties),
                commands = EfficientStringKeyMap(commands),
                isActive = null,
            )

        fun createGatewayFeature(
            properties: Map<String, Property> = emptyMap(),
            commands: Map<String, Command> = emptyMap(),
        ): Feature.Gateway =
            GatewayFeature(
                feature = "test.feature",
                gatewayId = "gateway1",
                isEnabled = true,
                isReady = true,
                apiVersion = 1,
                timestamp = Instant.fromEpochMilliseconds(0),
                uri = "http://test",
                properties = EfficientStringKeyMap(properties),
                commands = EfficientStringKeyMap(commands),
                deviceId = null,
                isActive = null,
            )

        fun createGeofencingFeature(
            properties: Map<String, Property> = emptyMap(),
            commands: Map<String, Command> = emptyMap(),
        ): Feature.Geofencing =
            GeofencingFeature(
                feature = "test.feature",
                isActive = true,
                isEnabled = true,
                isReady = true,
                apiVersion = 1,
                timestamp = Instant.fromEpochMilliseconds(0),
                uri = "http://test",
                properties = EfficientStringKeyMap(properties),
                commands = EfficientStringKeyMap(commands),
                deviceId = null,
                gatewayId = null,
            )

        fun createCommand(params: Map<String, Parameter> = emptyMap()): Command =
            Command(
                uri = "http://test/command",
                name = "testCommand",
                isExecutable = true,
                params = EfficientStringKeyMap(params),
            )

        context("stringPropertyRule") {
            test("returns Valid when property exists with StringValue") {
                val feature = createDeviceFeature(mapOf("value" to Property("string", StringValue("test"))))
                val rule = stringPropertyRule("value")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns MissingComponent when property is missing") {
                val feature = createDeviceFeature()
                val rule = stringPropertyRule("value")
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
                result.asIterable().first().shouldBeInstanceOf<ValidationError.MissingComponent>()
            }

            test("returns ComponentTypeMismatch when property has wrong type") {
                val feature = createDeviceFeature(mapOf("value" to Property("boolean", BooleanValue(true))))
                val rule = stringPropertyRule("value")
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
                result.asIterable().first().shouldBeInstanceOf<ValidationError.ComponentTypeMismatch>()
            }
        }

        context("booleanPropertyRule") {
            test("returns Valid when property exists with BooleanValue") {
                val feature = createDeviceFeature(mapOf("active" to Property("boolean", BooleanValue(true))))
                val rule = booleanPropertyRule("active")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns MissingComponent when property is missing") {
                val feature = createDeviceFeature()
                val rule = booleanPropertyRule("active")
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
            }

            test("returns ComponentTypeMismatch when property has wrong type") {
                val feature = createDeviceFeature(mapOf("active" to Property("string", StringValue("yes"))))
                val rule = booleanPropertyRule("active")
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
            }
        }

        context("doublePropertyRule") {
            test("returns Valid when property exists with DoubleValue") {
                val feature = createDeviceFeature(mapOf("temperature" to Property("number", DoubleValue(20.5))))
                val rule = doublePropertyRule("temperature")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns MissingComponent when property is missing") {
                val feature = createDeviceFeature()
                val rule = doublePropertyRule("temperature")
                rule.validate(feature).isInvalid shouldBe true
            }
        }

        context("listDoublePropertyRule") {
            test("returns Valid when property exists with ListDoubleValue") {
                val feature = createDeviceFeature(mapOf("values" to Property("array", ListDoubleValue(listOf(1.0, 2.0)))))
                val rule = listDoublePropertyRule("values")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Valid when property exists with ListEmptyValue") {
                val feature = createDeviceFeature(mapOf("values" to Property("array", ListEmptyValue)))
                val rule = listDoublePropertyRule("values")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns ComponentTypeMismatch for wrong list type") {
                val feature = createDeviceFeature(mapOf("values" to Property("array", ListStringValue(listOf("a")))))
                val rule = listDoublePropertyRule("values")
                rule.validate(feature).isInvalid shouldBe true
            }
        }

        context("listStringPropertyRule") {
            test("returns Valid when property exists with ListStringValue") {
                val feature = createDeviceFeature(mapOf("modes" to Property("array", ListStringValue(listOf("on", "off")))))
                val rule = listStringPropertyRule("modes")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Valid when property exists with ListEmptyValue") {
                val feature = createDeviceFeature(mapOf("modes" to Property("array", ListEmptyValue)))
                val rule = listStringPropertyRule("modes")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("listDeviceErrorPropertyRule") {
            test("returns Valid when property exists with ListDeviceErrorValue") {
                val feature = createDeviceFeature(mapOf("errors" to Property("array", ListDeviceErrorValue.EMPTY)))
                val rule = listDeviceErrorPropertyRule("errors")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Valid when property exists with ListEmptyValue") {
                val feature = createDeviceFeature(mapOf("errors" to Property("array", ListEmptyValue)))
                val rule = listDeviceErrorPropertyRule("errors")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("listZigbeeDeviceStatusPropertyRule") {
            test("returns Valid when property exists with ListZigbeeDeviceStatusValue") {
                val feature = createDeviceFeature(mapOf("status" to Property("array", ListZigbeeDeviceStatusValue.EMPTY)))
                val rule = listZigbeeDeviceStatusPropertyRule("status")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Valid with ListEmptyValue") {
                val feature = createDeviceFeature(mapOf("status" to Property("array", ListEmptyValue)))
                val rule = listZigbeeDeviceStatusPropertyRule("status")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("listRoomActorPropertyRule") {
            test("returns Valid when property exists with ListRoomActorValue") {
                val feature = createDeviceFeature(mapOf("actors" to Property("array", ListRoomActorValue.EMPTY)))
                val rule = listRoomActorPropertyRule("actors")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Valid with ListEmptyValue") {
                val feature = createDeviceFeature(mapOf("actors" to Property("array", ListEmptyValue)))
                val rule = listRoomActorPropertyRule("actors")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("listDevicePropertyRule") {
            test("returns Valid when property exists with ListDeviceValue") {
                val feature = createDeviceFeature(mapOf("devices" to Property("DeviceList", ListDeviceValue.EMPTY)))
                val rule = listDevicePropertyRule("devices")
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Valid with ListEmptyValue") {
                val feature = createDeviceFeature(mapOf("devices" to Property("DeviceList", ListEmptyValue)))
                val rule = listDevicePropertyRule("devices")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("objectOtherRoomConfigurationPropertyRule") {
            test("returns Valid when property exists with ObjectOtherRoomConfigurationValue") {
                val config =
                    OtherRoomConfiguration(
                        hydraulicBalance = true,
                        heatupSpeed = "normal",
                        trvAlgoActive = false,
                        openPointDetection = true,
                        virtualClimateSensor = false,
                        etrvSync = true,
                        useTrvOpenWindow = false,
                        heatOnTime = true,
                    )
                val feature = createDeviceFeature(mapOf("config" to Property("object", ObjectOtherRoomConfigurationValue(config))))
                val rule = objectOtherRoomConfigurationPropertyRule("config")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("schedulePropertyRule") {
            test("returns Valid when property exists with ScheduleValue") {
                val feature = createDeviceFeature(mapOf("schedule" to Property("Schedule", ScheduleValue(emptyMap()))))
                val rule = schedulePropertyRule("schedule")
                rule.validate(feature).isInvalid shouldBe false
            }
        }

        context("numberOfParametersRule") {
            test("returns Valid when parameter count matches") {
                val command = createCommand(mapOf("temp" to Parameter.of(NumberConstraints())))
                val rule = numberOfParametersRule(1, "setTemperature")
                rule.validate(command).isInvalid shouldBe false
            }

            test("returns Invalid when parameter count does not match") {
                val command = createCommand(mapOf("temp" to Parameter.of(NumberConstraints())))
                val rule = numberOfParametersRule(2, "setTemperature")
                val result = rule.validate(command)
                result.isInvalid shouldBe true
                result.asIterable().first().shouldBeInstanceOf<ValidationError.NumberOfParametersMismatch>()
            }

            test("returns Valid for zero parameters") {
                val command = createCommand()
                val rule = numberOfParametersRule(0, "activate")
                rule.validate(command).isInvalid shouldBe false
            }
        }

        context("stringConstraintsRule") {
            test("returns Valid when parameter has StringConstraints") {
                val command = createCommand(mapOf("mode" to Parameter.of(StringConstraints())))
                val rule = stringConstraintsRule("mode")
                rule.validate(command).isInvalid shouldBe false
            }

            test("returns MissingComponent when parameter is missing") {
                val command = createCommand()
                val rule = stringConstraintsRule("mode")
                val result = rule.validate(command)
                result.isInvalid shouldBe true
            }

            test("returns ComponentTypeMismatch when parameter has wrong constraints") {
                val command = createCommand(mapOf("mode" to Parameter.of(NumberConstraints())))
                val rule = stringConstraintsRule("mode")
                val result = rule.validate(command)
                result.isInvalid shouldBe true
            }
        }

        context("numberConstraintsRule") {
            test("returns Valid when parameter has NumberConstraints") {
                val command = createCommand(mapOf("temp" to Parameter.of(NumberConstraints(min = 10.0, max = 30.0))))
                val rule = numberConstraintsRule("temp")
                rule.validate(command).isInvalid shouldBe false
            }

            test("returns MissingComponent when parameter is missing") {
                val command = createCommand()
                val rule = numberConstraintsRule("temp")
                rule.validate(command).isInvalid shouldBe true
            }
        }

        context("booleanConstraintsRule") {
            test("returns Valid when parameter has BooleanConstraints") {
                val command = createCommand(mapOf("active" to Parameter.of(BooleanConstraints)))
                val rule = booleanConstraintsRule("active")
                rule.validate(command).isInvalid shouldBe false
            }
        }

        context("scheduleConstraintsRule") {
            test("returns Valid when parameter has ScheduleConstraints") {
                val constraints =
                    ScheduleConstraints(
                        modes = listOf("on", "off"),
                        maxEntries = 10,
                        resolution = 15,
                        defaultMode = "off",
                        overlapAllowed = false,
                    )
                val command = createCommand(mapOf("schedule" to Parameter.of(constraints)))
                val rule = scheduleConstraintsRule("schedule")
                rule.validate(command).isInvalid shouldBe false
            }
        }

        context("commandRule") {
            test("returns Valid when command exists and passes inner rule") {
                val command = createCommand(mapOf("temp" to Parameter.of(NumberConstraints())))
                val feature = createDeviceFeature(commands = mapOf("setTemperature" to command))
                val rule = commandRule("setTemperature", numberOfParametersRule(1, "setTemperature"))
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns MissingComponent when command is missing") {
                val feature = createDeviceFeature()
                val rule = commandRule("setTemperature", numberOfParametersRule(1, "setTemperature"))
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
                result.asIterable().first().shouldBeInstanceOf<ValidationError.MissingComponent>()
            }

            test("returns inner rule error when command exists but fails inner validation") {
                val command = createCommand() // 0 params
                val feature = createDeviceFeature(commands = mapOf("setTemperature" to command))
                val rule = commandRule("setTemperature", numberOfParametersRule(1, "setTemperature"))
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
                result.asIterable().first().shouldBeInstanceOf<ValidationError.NumberOfParametersMismatch>()
            }
        }

        context("deviceFeatureRule") {
            test("returns Valid for Feature.Device") {
                val feature = createDeviceFeature()
                val rule = deviceFeatureRule()
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Invalid for Feature.Gateway") {
                val feature = createGatewayFeature()
                val rule = deviceFeatureRule()
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
                result.asIterable().first().shouldBeInstanceOf<ValidationError.ComponentTypeMismatch>()
            }

            test("returns Invalid for Feature.Geofencing") {
                val feature = createGeofencingFeature()
                val rule = deviceFeatureRule()
                val result = rule.validate(feature)
                result.isInvalid shouldBe true
            }
        }

        context("gatewayFeatureRule") {
            test("returns Valid for Feature.Gateway") {
                val feature = createGatewayFeature()
                val rule = gatewayFeatureRule()
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Invalid for Feature.Device") {
                val feature = createDeviceFeature()
                val rule = gatewayFeatureRule()
                rule.validate(feature).isInvalid shouldBe true
            }

            test("returns Invalid for Feature.Geofencing") {
                val feature = createGeofencingFeature()
                val rule = gatewayFeatureRule()
                rule.validate(feature).isInvalid shouldBe true
            }
        }

        context("geofencingFeatureRule") {
            test("returns Valid for Feature.Geofencing") {
                val feature = createGeofencingFeature()
                val rule = geofencingFeatureRule()
                rule.validate(feature).isInvalid shouldBe false
            }

            test("returns Invalid for Feature.Device") {
                val feature = createDeviceFeature()
                val rule = geofencingFeatureRule()
                rule.validate(feature).isInvalid shouldBe true
            }

            test("returns Invalid for Feature.Gateway") {
                val feature = createGatewayFeature()
                val rule = geofencingFeatureRule()
                rule.validate(feature).isInvalid shouldBe true
            }
        }

        context("propertyHash") {
            test("combines hashCode and length into Long") {
                val hash = propertyHash(12345, 5)
                hash shouldBe ((12345L shl 32) or 5L)
            }

            test("handles negative hashCodes") {
                val hash = propertyHash(-1, 3)
                hash shouldBe ((-1L shl 32) or 3L)
            }
        }
    })
