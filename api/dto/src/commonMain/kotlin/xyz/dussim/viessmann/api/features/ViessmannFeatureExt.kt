@file:Suppress("TooManyFunctions")

package xyz.dussim.viessmann.api.features

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleMap
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.ZigbeeDeviceStatus
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.BooleanProperty
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.DeviceListProperty
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.NumberProperty
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ScheduleProperty
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.StringProperty
import xyz.dussim.viessmann.api.features.internal.ViessmannDeviceFeatureImpl
import xyz.dussim.viessmann.api.features.internal.ViessmannFeatureUnspecified
import xyz.dussim.viessmann.api.features.internal.ViessmannGatewayFeatureImpl
import xyz.dussim.viessmann.api.features.internal.ViessmannGeofencingFeatureImpl
import kotlin.enums.enumEntries
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

typealias DeviceInfo = DeviceListProperty.Device
typealias DeviceError = ArrayContent.DeviceError

val ViessmannFeatureSerializersModule =
    SerializersModule {
        polymorphic(ViessmannFeature::class) {
            defaultDeserializer { ViessmannFeatureUnspecified.serializer() }
            subclass(ViessmannFeatureUnspecified::class, ViessmannFeatureUnspecified.serializer())
        }
        polymorphic(ViessmannFeature.Gateway::class) {
            defaultDeserializer { ViessmannGatewayFeatureImpl.serializer() }
            subclass(ViessmannGatewayFeatureImpl::class, ViessmannGatewayFeatureImpl.serializer())
        }
        polymorphic(ViessmannFeature.Device::class) {
            defaultDeserializer { ViessmannDeviceFeatureImpl.serializer() }
            subclass(ViessmannDeviceFeatureImpl::class, ViessmannDeviceFeatureImpl.serializer())
        }
        polymorphic(ViessmannFeature.Geofencing::class) {
            defaultDeserializer { ViessmannGeofencingFeatureImpl.serializer() }
            subclass(ViessmannGeofencingFeatureImpl::class, ViessmannGeofencingFeatureImpl.serializer())
        }
    }

fun ViessmannFeature.asDevice(): ViessmannFeature.Device = ViessmannDeviceFeatureImpl(this)

fun ViessmannFeature.asGateway(): ViessmannFeature.Gateway = ViessmannGatewayFeatureImpl(this)

fun ViessmannFeature.asGeofencing(): ViessmannFeature.Geofencing = ViessmannGeofencingFeatureImpl(this)

private inline fun <F, reified T : ViessmannFeatureProperty<F>> ViessmannFeature.property() =
    PropertyDelegateProvider<Any?, T> { _, property ->
        val viessmannFeatureProperty =
            requireNotNull(properties[property.name]) {
                "Property '${property.name}' not found in feature properties of ${properties.keys}"
            }

        require(viessmannFeatureProperty::class == T::class) {
            "Property '${property.name}' has type ${viessmannFeatureProperty::class.simpleName} but expected ${T::class.simpleName}"
        }

        viessmannFeatureProperty as T
    }

private inline fun <F, reified T : ViessmannFeatureProperty<List<ArrayContent<F>>>> ViessmannFeature.arrayProperty() =
    PropertyDelegateProvider<Any?, T> { _, property ->
        val viessmannFeatureProperty =
            requireNotNull(properties[property.name]) {
                "Property '${property.name}' not found in feature properties of ${properties.keys}"
            }

        require(viessmannFeatureProperty is T) {
            "Property '${property.name}' has type ${viessmannFeatureProperty::class.simpleName} but expected ArrayProperty"
        }

        viessmannFeatureProperty
    }

fun ViessmannFeature.string() = property<String, StringProperty>()

fun ViessmannFeature.double() = property<Double, NumberProperty>()

fun ViessmannFeature.boolean() = property<Boolean, BooleanProperty>()

fun ViessmannFeature.deviceList() = property<List<DeviceInfo>, DeviceListProperty>()

fun ViessmannFeature.int() = ReadOnlyProperty<Any?, Int> { thisRef, property -> double().provideDelegate(thisRef, property).value.toInt() }

fun ViessmannFeature.long() = ReadOnlyProperty<Any?, Long> { thisRef, property -> double().provideDelegate(thisRef, property).value.toLong() }

fun ViessmannFeature.float() = ReadOnlyProperty<Any?, Float> { thisRef, property -> double().provideDelegate(thisRef, property).value.toFloat() }

fun ViessmannFeature.schedule() = property<ScheduleMap, ScheduleProperty>()

inline fun <reified E> ViessmannFeature.enum() where E : Enum<E>, E : FeatureEnum =
    ReadOnlyProperty<Any?, E> { _, property ->
        val viessmannFeatureProperty =
            requireNotNull(properties[property.name]) {
                "Property '${property.name}' not found in feature properties of ${properties.keys}"
            }
        require(viessmannFeatureProperty is StringProperty) {
            "Property '${property.name}' has type ${viessmannFeatureProperty::class.simpleName} but expected StringProperty"
        }

        enumEntries<E>().first { it.propertyValue == viessmannFeatureProperty.value }
    }

// TODO, is this working correctly?
fun ViessmannFeature.deviceErrors() = arrayProperty<DeviceError, _>()

// TODO, is this working correctly?
fun ViessmannFeature.zigbeeDeviceStatuses() = arrayProperty<ZigbeeDeviceStatus, _>()

// TODO, is this working correctly?
fun ViessmannFeature.strings() = arrayProperty<String, _>()
