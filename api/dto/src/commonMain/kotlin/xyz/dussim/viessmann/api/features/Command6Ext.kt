@file:Suppress("LongParameterList")

package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

fun <T1, T2, T3, T4, T5, T6> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
    constraint4: ViessmannFeatureCommandParamConstraints<T4>,
    constraint5: ViessmannFeatureCommandParamConstraints<T5>,
    constraint6: ViessmannFeatureCommandParamConstraints<T6>,
) = ReadOnlyProperty<Any, Command6<T1, T2, T3, T4, T5, T6>> { _, property ->
    object : Command6<T1, T2, T3, T4, T5, T6> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1
        override val constraint2 = constraint2
        override val constraint3 = constraint3
        override val constraint4 = constraint4
        override val constraint5 = constraint5
        override val constraint6 = constraint6
    }
}

fun <T1, T2, T3, T4, T5, T6, C : Command6<T1, T2, T3, T4, T5, T6>> ViessmannFeature.command(
    producer: (Command6<T1, T2, T3, T4, T5, T6>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
    constraint4: ViessmannFeatureCommandParamConstraints<T4>,
    constraint5: ViessmannFeatureCommandParamConstraints<T5>,
    constraint6: ViessmannFeatureCommandParamConstraints<T6>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command6<T1, T2, T3, T4, T5, T6> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1
            override val constraint2 = constraint2
            override val constraint3 = constraint3
            override val constraint4 = constraint4
            override val constraint5 = constraint5
            override val constraint6 = constraint6
        },
    )
}

fun <T1, T2, T3, T4, T5, T6> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
    constraint4: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T4>>,
    constraint5: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T5>>,
    constraint6: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T6>>,
) = ReadOnlyProperty<Any, Command6<T1, T2, T3, T4, T5, T6>> { _, property ->
    object : Command6<T1, T2, T3, T4, T5, T6> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1(property.name)
        override val constraint2 = constraint2(property.name)
        override val constraint3 = constraint3(property.name)
        override val constraint4 = constraint4(property.name)
        override val constraint5 = constraint5(property.name)
        override val constraint6 = constraint6(property.name)
    }
}

fun <T1, T2, T3, T4, T5, T6, C : Command6<T1, T2, T3, T4, T5, T6>> ViessmannFeature.command(
    producer: (Command6<T1, T2, T3, T4, T5, T6>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
    constraint4: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T4>>,
    constraint5: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T5>>,
    constraint6: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T6>>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command6<T1, T2, T3, T4, T5, T6> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1(property.name)
            override val constraint2 = constraint2(property.name)
            override val constraint3 = constraint3(property.name)
            override val constraint4 = constraint4(property.name)
            override val constraint5 = constraint5(property.name)
            override val constraint6 = constraint6(property.name)
        },
    )
}
