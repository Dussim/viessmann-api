@file:Suppress("LongParameterList")

package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

fun <T1, T2, T3, T4, T5> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
    constraint4: ViessmannFeatureCommandParamConstraints<T4>,
    constraint5: ViessmannFeatureCommandParamConstraints<T5>,
) = ReadOnlyProperty<Any, Command5<T1, T2, T3, T4, T5>> { _, property ->
    object : Command5<T1, T2, T3, T4, T5> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1
        override val constraint2 = constraint2
        override val constraint3 = constraint3
        override val constraint4 = constraint4
        override val constraint5 = constraint5
    }
}

fun <T1, T2, T3, T4, T5, C : Command5<T1, T2, T3, T4, T5>> ViessmannFeature.command(
    producer: (Command5<T1, T2, T3, T4, T5>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
    constraint4: ViessmannFeatureCommandParamConstraints<T4>,
    constraint5: ViessmannFeatureCommandParamConstraints<T5>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command5<T1, T2, T3, T4, T5> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1
            override val constraint2 = constraint2
            override val constraint3 = constraint3
            override val constraint4 = constraint4
            override val constraint5 = constraint5
        },
    )
}

fun <T1, T2, T3, T4, T5> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
    constraint4: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T4>>,
    constraint5: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T5>>,
) = ReadOnlyProperty<Any, Command5<T1, T2, T3, T4, T5>> { _, property ->
    object : Command5<T1, T2, T3, T4, T5> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1(property.name)
        override val constraint2 = constraint2(property.name)
        override val constraint3 = constraint3(property.name)
        override val constraint4 = constraint4(property.name)
        override val constraint5 = constraint5(property.name)
    }
}

fun <T1, T2, T3, T4, T5, C : Command5<T1, T2, T3, T4, T5>> ViessmannFeature.command(
    producer: (Command5<T1, T2, T3, T4, T5>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
    constraint4: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T4>>,
    constraint5: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T5>>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command5<T1, T2, T3, T4, T5> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1(property.name)
            override val constraint2 = constraint2(property.name)
            override val constraint3 = constraint3(property.name)
            override val constraint4 = constraint4(property.name)
            override val constraint5 = constraint5(property.name)
        },
    )
}
