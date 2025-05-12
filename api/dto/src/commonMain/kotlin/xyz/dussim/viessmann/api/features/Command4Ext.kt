package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

fun <T1, T2, T3, T4> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
    constraint4: ViessmannFeatureCommandParamConstraints<T4>,
) = ReadOnlyProperty<Any, Command4<T1, T2, T3, T4>> { _, property ->
    object : Command4<T1, T2, T3, T4> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1
        override val constraint2 = constraint2
        override val constraint3 = constraint3
        override val constraint4 = constraint4
    }
}

fun <T1, T2, T3, T4, C : Command4<T1, T2, T3, T4>> ViessmannFeature.command(
    producer: (Command4<T1, T2, T3, T4>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
    constraint4: ViessmannFeatureCommandParamConstraints<T4>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command4<T1, T2, T3, T4> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1
            override val constraint2 = constraint2
            override val constraint3 = constraint3
            override val constraint4 = constraint4
        },
    )
}

fun <T1, T2, T3, T4> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
    constraint4: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T4>>,
) = ReadOnlyProperty<Any, Command4<T1, T2, T3, T4>> { _, property ->
    object : Command4<T1, T2, T3, T4> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1(property.name)
        override val constraint2 = constraint2(property.name)
        override val constraint3 = constraint3(property.name)
        override val constraint4 = constraint4(property.name)
    }
}

fun <T1, T2, T3, T4, C : Command4<T1, T2, T3, T4>> ViessmannFeature.command(
    producer: (Command4<T1, T2, T3, T4>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
    constraint4: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T4>>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command4<T1, T2, T3, T4> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1(property.name)
            override val constraint2 = constraint2(property.name)
            override val constraint3 = constraint3(property.name)
            override val constraint4 = constraint4(property.name)
        },
    )
}
