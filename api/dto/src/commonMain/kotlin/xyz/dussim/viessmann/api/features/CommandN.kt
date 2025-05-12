@file:Suppress("LongParameterList")

package xyz.dussim.viessmann.api.features

interface WrapsViessmannFeatureCommand {
    val viessmannFeatureCommand: ViessmannFeatureCommand
}

interface Command0 : WrapsViessmannFeatureCommand

interface Command1<T1> : WrapsViessmannFeatureCommand {
    val constraint1: ViessmannFeatureCommandParamConstraints<T1>

    fun verify(param1: T1) = constraint1.validate(param1)
}

interface Command2<T1, T2> : WrapsViessmannFeatureCommand {
    val constraint1: ViessmannFeatureCommandParamConstraints<T1>
    val constraint2: ViessmannFeatureCommandParamConstraints<T2>

    fun verify(
        param1: T1,
        param2: T2,
    ) = constraint1.validate(param1) && constraint2.validate(param2)
}

interface Command3<T1, T2, T3> : WrapsViessmannFeatureCommand {
    val constraint1: ViessmannFeatureCommandParamConstraints<T1>
    val constraint2: ViessmannFeatureCommandParamConstraints<T2>
    val constraint3: ViessmannFeatureCommandParamConstraints<T3>

    fun verify(
        param1: T1,
        param2: T2,
        param3: T3,
    ) = constraint1.validate(param1) && constraint2.validate(param2) && constraint3.validate(param3)
}

interface Command4<T1, T2, T3, T4> : WrapsViessmannFeatureCommand {
    val constraint1: ViessmannFeatureCommandParamConstraints<T1>
    val constraint2: ViessmannFeatureCommandParamConstraints<T2>
    val constraint3: ViessmannFeatureCommandParamConstraints<T3>
    val constraint4: ViessmannFeatureCommandParamConstraints<T4>

    fun verify(
        param1: T1,
        param2: T2,
        param3: T3,
        param4: T4,
    ) = constraint1.validate(param1) && constraint2.validate(param2) && constraint3.validate(param3) && constraint4.validate(param4)
}

interface Command5<T1, T2, T3, T4, T5> : WrapsViessmannFeatureCommand {
    val constraint1: ViessmannFeatureCommandParamConstraints<T1>
    val constraint2: ViessmannFeatureCommandParamConstraints<T2>
    val constraint3: ViessmannFeatureCommandParamConstraints<T3>
    val constraint4: ViessmannFeatureCommandParamConstraints<T4>
    val constraint5: ViessmannFeatureCommandParamConstraints<T5>

    fun verify(
        param1: T1,
        param2: T2,
        param3: T3,
        param4: T4,
        param5: T5,
    ) = constraint1.validate(param1) && constraint2.validate(param2) && constraint3.validate(param3) && constraint4.validate(param4) && constraint5.validate(param5)
}

interface Command6<T1, T2, T3, T4, T5, T6> : WrapsViessmannFeatureCommand {
    val constraint1: ViessmannFeatureCommandParamConstraints<T1>
    val constraint2: ViessmannFeatureCommandParamConstraints<T2>
    val constraint3: ViessmannFeatureCommandParamConstraints<T3>
    val constraint4: ViessmannFeatureCommandParamConstraints<T4>
    val constraint5: ViessmannFeatureCommandParamConstraints<T5>
    val constraint6: ViessmannFeatureCommandParamConstraints<T6>

    fun verify(
        param1: T1,
        param2: T2,
        param3: T3,
        param4: T4,
        param5: T5,
        param6: T6,
    ) = constraint1.validate(param1) &&
        constraint2.validate(param2) &&
        constraint3.validate(param3) &&
        constraint4.validate(param4) &&
        constraint5.validate(param5) &&
        constraint6.validate(param6)
}

fun interface ViessmannFeatureCommandParamConstraintsProvider<T : ViessmannFeatureCommandParamConstraints<*>> {
    operator fun invoke(param: String): T
}
