package xyz.dussim.viessmann.api.features

import kotlin.properties.ReadOnlyProperty

internal fun ViessmannFeature.commandOrThrow(command: String) =
    requireNotNull(commands[command]) {
        "Command $command not found in feature $feature, available commands: ${commands.keys}"
    }

fun ViessmannFeature.command() =
    ReadOnlyProperty<Any, Command0> { _, property ->
        object : Command0 {
            override val viessmannFeatureCommand = commandOrThrow(property.name)
        }
    }
