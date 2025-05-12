package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

fun <T1, T2, T3> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
) = ReadOnlyProperty<Any, Command3<T1, T2, T3>> { _, property ->
    object : Command3<T1, T2, T3> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1
        override val constraint2 = constraint2
        override val constraint3 = constraint3
    }
}

fun <T1, T2, T3, C : Command3<T1, T2, T3>> ViessmannFeature.command(
    producer: (Command3<T1, T2, T3>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
    constraint2: ViessmannFeatureCommandParamConstraints<T2>,
    constraint3: ViessmannFeatureCommandParamConstraints<T3>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command3<T1, T2, T3> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1
            override val constraint2 = constraint2
            override val constraint3 = constraint3
        },
    )
}

fun <T1, T2, T3> ViessmannFeature.command(
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
) = ReadOnlyProperty<Any, Command3<T1, T2, T3>> { _, property ->
    object : Command3<T1, T2, T3> {
        override val viessmannFeatureCommand = commandOrThrow(property.name)
        override val constraint1 = constraint1(property.name)
        override val constraint2 = constraint2(property.name)
        override val constraint3 = constraint3(property.name)
    }
}

fun <T1, T2, T3, C : Command3<T1, T2, T3>> ViessmannFeature.command(
    producer: (Command3<T1, T2, T3>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
    constraint2: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T2>>,
    constraint3: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T3>>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command3<T1, T2, T3> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
            override val constraint1 = constraint1(property.name)
            override val constraint2 = constraint2(property.name)
            override val constraint3 = constraint3(property.name)
        },
    )
}
