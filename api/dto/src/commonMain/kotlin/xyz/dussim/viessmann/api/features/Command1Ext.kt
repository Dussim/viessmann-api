package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

fun <T1> ViessmannFeature.command(constraint1: ViessmannFeatureCommandParamConstraints<T1>) =
    ReadOnlyProperty<Any, Command1<T1>> { _, property ->
        object : Command1<T1> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)

            override val constraint1 = constraint1
        }
    }

fun <T1, C : Command1<T1>> ViessmannFeature.command(
    producer: (Command1<T1>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraints<T1>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command1<T1> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)

            override val constraint1 = constraint1
        },
    )
}

fun <T1> ViessmannFeature.command(constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>) =
    ReadOnlyProperty<Any, Command1<T1>> { _, property ->
        object : Command1<T1> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)

            override val constraint1 = constraint1(property.name)
        }
    }

fun <T1, C : Command1<T1>> ViessmannFeature.command(
    producer: (Command1<T1>) -> C,
    constraint1: ViessmannFeatureCommandParamConstraintsProvider<out ViessmannFeatureCommandParamConstraints<T1>>,
) = ReadOnlyProperty<Any, C> { _, property ->
    producer(
        object : Command1<T1> {
            override val viessmannFeatureCommand = commandOrThrow(property.name)

            override val constraint1 = constraint1(property.name)
        },
    )
}
