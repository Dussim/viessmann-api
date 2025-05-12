package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

fun <T1, T2> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
) = ReadOnlyProperty<Any, Command2<T1, T2>> { _, property ->
    object : Command2<T1, T2> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)

        override val constraint1 = constraint1
        override val constraint2 = constraint2
    }
}

fun <T1, T2, C : Command2<T1, T2>> ViessmannFeature.command(
    producer: (Command2<T1, T2>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command2<T1, T2> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)

            override val constraint1 = constraint1
            override val constraint2 = constraint2
        },
    )
}

fun <T1, T2> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
) = ReadOnlyProperty<Any, Command2<T1, T2>> { _, property ->
    object : Command2<T1, T2> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)

        override val constraint1 = constraint1(property.name)
        override val constraint2 = constraint2(property.name)
    }
}

fun <T1, T2, C : Command2<T1, T2>> ViessmannFeature.command(
    producer: (Command2<T1, T2>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command2<T1, T2> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1(property.name)
            override val constraint2 = constraint2(property.name)
        },
    )
}
