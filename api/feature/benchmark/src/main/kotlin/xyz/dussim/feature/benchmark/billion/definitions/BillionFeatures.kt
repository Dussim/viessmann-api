package xyz.dussim.feature.benchmark.billion.definitions

import xyz.dussim.viessmann.api.feature.annotations.CommandName
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command0
import xyz.dussim.viessmann.feature.api.Command1
import xyz.dussim.viessmann.feature.api.Command2
import xyz.dussim.viessmann.feature.api.Command3
import xyz.dussim.viessmann.feature.api.Command4
import xyz.dussim.viessmann.feature.api.Command5
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue

// Benchmark-owned, deliberately small feature shapes for the one-billion-validations test. They are independent of
// the OpenAPI input, so the expected match counts in the test only change when these definitions or the fixture do.
//
// Together the shapes need properties, commands and parameter maps of every EfficientStringKeyMap size:
//
// | feature          | properties | commands | parameters per command |
// |------------------|------------|----------|------------------------|
// | BillionSystem    | 0          | 5 (N)    | 0, 0, 1, 2, 3          |
// | BillionPump      | 1          | 0        | -                      |
// | BillionThermostat| 2          | 2        | 1, 2                   |
// | BillionSensor    | 3          | 1        | 1                      |
// | BillionProgram   | 4          | 2 or 3   | 0, 1 (optional), 3     |
// | BillionCircuit   | 6 (N)      | 4        | 0, 1, 4, 5 (N)         |
//
// Command parameters are declared alphabetically, matching the generated definitions.

@GenerateFeatureImplementation("billion.system")
interface BillionSystemFeature : Feature {
    val start: Start

    val stop: Stop

    val setLevel: SetLevel

    val setRange: SetRange

    val setWindow: SetWindow

    @CommandName("start")
    interface Start : Command0 {
        companion object
    }

    @CommandName("stop")
    interface Stop : Command0 {
        companion object
    }

    @CommandName("setLevel")
    interface SetLevel : Command1<Double> {
        val level: NumberConstraints

        companion object
    }

    @CommandName("setRange")
    interface SetRange : Command2<Double, Double> {
        val lower: NumberConstraints

        val upper: NumberConstraints

        companion object
    }

    @CommandName("setWindow")
    interface SetWindow : Command3<String, Double, String> {
        val closes: StringConstraints

        val level: NumberConstraints

        val opens: StringConstraints

        companion object
    }

    companion object
}

@GenerateFeatureImplementation("billion.pumps.{N}")
interface BillionPumpFeature : Feature {
    val status: StringValue

    companion object
}

@GenerateFeatureImplementation("billion.thermostat")
interface BillionThermostatFeature : Feature {
    val active: BooleanValue

    val temperature: DoubleValue

    val setTemperature: SetTemperature

    val setMode: SetMode

    @CommandName("setTemperature")
    interface SetTemperature : Command1<Double> {
        val targetTemperature: NumberConstraints

        companion object
    }

    @CommandName("setMode")
    interface SetMode : Command2<Double, String> {
        val duration: NumberConstraints

        val mode: StringConstraints

        companion object
    }

    companion object
}

@GenerateFeatureImplementation("billion.sensors.{N}.temperature")
interface BillionSensorFeature : Feature {
    val value: DoubleValue

    val minimum: DoubleValue

    val maximum: DoubleValue

    val calibrate: Calibrate

    @CommandName("calibrate")
    interface Calibrate : Command1<Double> {
        val offset: NumberConstraints

        companion object
    }

    companion object
}

@GenerateFeatureImplementation("billion.program")
interface BillionProgramFeature : Feature {
    val name: StringValue

    val enabled: BooleanValue

    val start: StringValue

    val end: StringValue

    val activate: Activate

    val rename: Rename?

    val reschedule: Reschedule

    @CommandName("activate")
    interface Activate : Command0 {
        companion object
    }

    @CommandName("rename")
    interface Rename : Command1<String> {
        val name: StringConstraints

        companion object
    }

    @CommandName("reschedule")
    interface Reschedule : Command3<String, Boolean, String> {
        val end: StringConstraints

        val repeat: BooleanConstraints

        val start: StringConstraints

        companion object
    }

    companion object
}

@GenerateFeatureImplementation("billion.circuits.{N}")
interface BillionCircuitFeature : Feature {
    val mode: StringValue

    val supplyTemperature: DoubleValue

    val returnTemperature: DoubleValue

    val pressure: DoubleValue

    val demand: StringValue

    val frostProtection: BooleanValue

    val reset: Reset

    val changeMode: ChangeMode

    val setLimits: SetLimits

    val configure: Configure

    @CommandName("reset")
    interface Reset : Command0 {
        companion object
    }

    @CommandName("changeMode")
    interface ChangeMode : Command1<String> {
        val mode: StringConstraints

        companion object
    }

    @CommandName("setLimits")
    interface SetLimits : Command4<Double, Double, Double, Double> {
        val delay: NumberConstraints

        val hysteresis: NumberConstraints

        val maximum: NumberConstraints

        val minimum: NumberConstraints

        companion object
    }

    @CommandName("configure")
    interface Configure : Command5<String, Boolean, String, Double, Double> {
        val curve: StringConstraints

        val enabled: BooleanConstraints

        val label: StringConstraints

        val shift: NumberConstraints

        val slope: NumberConstraints

        companion object
    }

    companion object
}
