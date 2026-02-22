# Feature Processor Code Generation Guide

This document explains how the `:api:feature:processor` module generates feature implementations from interface definitions in this project.

## Overview

The feature processor is a Kotlin annotation processor that automatically generates concrete implementations of feature interfaces. It processes Kotlin interface definitions annotated with `@GenerateFeatureImplementation` to create type-safe, validated feature implementations.

These interface definitions can be either written manually or generated from YAML API specifications using the `xyz.dussim.generate.features.yaml` Gradle plugin (configured in modules like `:api:feature:definitions` and `:api:feature:implementations`).

## Supported Features

### Base Feature Types

The processor supports three base feature types, each extending from `Feature`:

1. **`Feature.Device`** - Device-level features
   - Requires: `deviceId: String`, `gatewayId: String`
   - Generated implementations extend `AbstractDeviceFeature`

2. **`Feature.Gateway`** - Gateway-level features
   - Requires: `gatewayId: String`
   - Generated implementations extend `AbstractGatewayFeature`

3. **`Feature.Geofencing`** - Geofencing features
   - Requires: `isActive: Boolean`
   - Generated implementations extend `AbstractGeofencingFeature`

All base types automatically include:
- `feature: String` - Feature name
- `wildcardFeature: String` - Feature pattern with wildcards
- `isEnabled: Boolean` - Feature enabled state
- `isReady: Boolean` - Feature ready state
- `apiVersion: Int` - API version
- `timestamp: Instant` - Timestamp
- `uri: String` - Feature URI
- `properties: EfficientStringKeyMap<Property>` - Property map
- `commands: EfficientStringKeyMap<Command>` - Command map

### Property Types

Feature interfaces can declare properties with these supported types (all must be subclasses of `PropertyValue<T>`):

#### Primitive Property Values
- `StringValue` - String properties
- `DoubleValue` - Numeric properties
- `BooleanValue` - Boolean properties
- `UnknownValue` - Unknown JSON elements

#### List Property Values
- `ListStringValue` - List of strings
- `ListDoubleValue` - List of numbers
- `ListDeviceErrorValue` - List of device errors
- `ListZigbeeDeviceStatusValue` - List of Zigbee device statuses
- `ListRoomActorValue` - List of room actors
- `ListDeviceValue` - List of devices
- `ListEmptyValue` - Empty list

#### Complex Property Values
- `ObjectOtherRoomConfigurationValue` - Other room configuration object
- `ScheduleValue` - Schedule map (`Map<String, List<Schedule>>`)

#### Custom Property Values
- **Custom sealed interfaces** (enums): Annotated with `@FeatureEnum`

**Note:** All property types must extend `PropertyValue<T>`. Raw collections like `List<T>`, `Set<T>`, or `Map<K, V>` are NOT supported directly - use the appropriate `PropertyValue` wrapper instead.

### Command Types

Commands are defined as nested interfaces implementing `Command0` through `Command6`:

- **`Command0`** - No parameters
- **`Command1<T1>`** - One parameter
- **`Command2<T1, T2>`** - Two parameters
- **`Command3<T1, T2, T3>`** - Three parameters
- **`Command4<T1, T2, T3, T4>`** - Four parameters
- **`Command5<T1, T2, T3, T4, T5>`** - Five parameters
- **`Command6<T1, T2, T3, T4, T5, T6>`** - Six parameters

Each `CommandN` interface exposes `constraintN` properties of type `Constraints<T>` that provide validation rules and allowed values for command parameters.

### Indexed Features

Features can be **indexed** (wildcarded), indicated by `{}` in the feature name:
- Static feature: `"device.timezone"`
- Indexed feature: `"heating.circuits.{}"`
- Multi-indexed: `"rooms.{}.operating.programs.{}"`

## Interface Requirements

### Basic Structure

```kotlin
@GenerateFeatureImplementation("feature.name.here")
interface MyFeature : Feature.Device {
    companion object

    // Properties
    val propertyName: PropertyType

    // Commands
    val commandName: MyCommandInterface

    @CommandName("actualCommandName")
    interface MyCommandInterface : Command1<ParamType> {
        companion object
        val parameterName: Constraints<ParamType>
    }
}
```

### Required Elements

1. **Annotation**: `@GenerateFeatureImplementation("feature.name")`
   - Must include the exact feature name as it appears in the API
   - Use `{}` for indexed features

2. **Base interface**: Must extend one of:
   - `Feature.Device`
   - `Feature.Gateway`
   - `Feature.Geofencing`

3. **Companion object**: Required in both:
   - Main feature interface
   - Each command interface

4. **Command annotation**: Use `@CommandName("name")` on command interfaces
   - Specifies the actual command name in the API
   - Allows Kotlin-friendly naming in code

### Property Naming

- Properties map to API property names automatically
- Use backticks for reserved keywords: `` val `value`: StringValue ``
- Property names should match API schema exactly

### Command Parameters

Command parameters must:
1. Inherit from appropriate `CommandN<T1, T2, ...>` interface
2. Declare named constraint properties matching parameter names
3. Provide deprecated fallback to `constraintN` properties for compatibility

Example with named parameters:
```kotlin
@CommandName("setLocation")
interface SetLocation : Command3<Double, Double, Double> {
    @Deprecated(
        message = "Use 'altitude' instead",
        replaceWith = ReplaceWith("altitude"),
        level = DeprecationLevel.WARNING
    )
    override val constraint1: NumberConstraints
        get() = altitude

    val altitude: NumberConstraints
    val latitude: NumberConstraints
    val longitude: NumberConstraints

    companion object
}
```

## Examples

### Simple Device Feature with Property

```kotlin
@GenerateFeatureImplementation("heating.boiler.pumps.internal")
interface HeatingBoilerPumpsInternalFeature : Feature.Device {
    companion object

    @FeatureEnum
    sealed interface Status {
        data object On : Status
        data object Off : Status

        @JvmRecord
        data class Unknown(val value: String) : Status

        @FeatureEnum.Factory
        companion object : FeatureEnumFactory<StringValue, Status> {
            override fun invoke(propertyValue: StringValue): Status =
                when (propertyValue.element) {
                    "on" -> On
                    "off" -> Off
                    else -> Unknown(propertyValue.element)
                }
        }
    }

    val status: Status
}
```

### Device Feature with Command

```kotlin
@GenerateFeatureImplementation("device.timezone")
interface DeviceTimezoneFeature : Feature.Device {
    companion object

    val value: StringValue
    val setTimezone: SetTimezone

    @CommandName("setTimezone")
    interface SetTimezone : Command1<String> {
        @Deprecated(
            message = "Use 'value' instead",
            replaceWith = ReplaceWith("value"),
            level = DeprecationLevel.WARNING
        )
        override val constraint1: StringConstraints
            get() = value

        val value: StringConstraints
        companion object
    }
}
```

### Feature with Multiple Parameters

```kotlin
@GenerateFeatureImplementation("heating.configuration.houseLocation")
interface HeatingConfigurationHouseLocationFeature : Feature.Device {
    companion object

    val altitude: DoubleValue
    val latitude: DoubleValue
    val longitude: DoubleValue
    val setLocation: SetLocation

    @CommandName("setLocation")
    interface SetLocation : Command3<Double, Double, Double> {
        @Deprecated(
            message = "Use 'altitude' instead",
            replaceWith = ReplaceWith("altitude"),
            level = DeprecationLevel.WARNING
        )
        override val constraint1: NumberConstraints
            get() = altitude

        @Deprecated(
            message = "Use 'latitude' instead",
            replaceWith = ReplaceWith("latitude"),
            level = DeprecationLevel.WARNING
        )
        override val constraint2: NumberConstraints
            get() = latitude

        @Deprecated(
            message = "Use 'longitude' instead",
            replaceWith = ReplaceWith("longitude"),
            level = DeprecationLevel.WARNING
        )
        override val constraint3: NumberConstraints
            get() = longitude

        val altitude: NumberConstraints
        val latitude: NumberConstraints
        val longitude: NumberConstraints
        companion object
    }
}
```

## What is NOT Supported

1. **Non-standard base types**: Features must extend `Feature.Device`, `Feature.Gateway`, or `Feature.Geofencing`
2. **More than 6 command parameters**: Maximum is `Command6`
3. **Mutable properties**: All properties are read-only (`val`)
4. **Functions other than commands**: Only command interfaces are supported
5. **Generic type parameters**: Feature interfaces cannot be generic
6. **Raw collection types**: Properties cannot use `List<T>`, `Set<T>`, or `Map<K, V>` directly - must use `PropertyValue` subclasses
7. **Inheritance between feature interfaces**: Each feature must be self-contained

## Generated Code

The processor generates:

1. **Implementation classes**: Concrete implementations extending abstract base classes
2. **Factory objects**: Type-safe factories for creating feature instances
3. **Descriptor objects**: Feature metadata for pattern matching and validation
4. **Validation rules**: Automatic validation for features and commands
5. **Extension properties**: Convenient accessors on `Feature` type

Generated implementations handle:
- Property extraction from generic `Feature` instances
- Command parameter validation
- Type-safe conversions
- Efficient equality and hashing

## Related Files

- Annotation: `api/feature/annotations/src/commonMain/kotlin/.../GenerateFeatureImplementation.kt`
- Processor logic: `api/feature/processor/src/main/kotlin/.../CommonTypes.kt`
- Abstract base classes: `api/feature/common/src/commonMain/kotlin/.../AbstractFeatures.kt`
- Command interfaces: `api/feature/common/src/commonMain/kotlin/.../CommandN.kt`
- YAML Interface Generator: `gradle/build-logic/src/main/kotlin/.../YamlFeatureInterfaceGenerator.kt`
