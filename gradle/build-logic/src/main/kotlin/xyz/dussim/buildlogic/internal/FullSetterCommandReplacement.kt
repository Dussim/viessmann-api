package xyz.dussim.buildlogic.internal

/**
 * Describes a pair of opposite commands that is completely replaced by a Boolean setter.
 *
 * The replacement is applied only when all three command names are present on the same
 * feature. Partial command sets retain their original commands.
 */
internal data class FullSetterCommandReplacement(
    val positiveCommand: String,
    val negativeCommand: String,
    val setterCommand: String,
) {
    val oppositeCommands: Set<String> = setOf(positiveCommand, negativeCommand)
}

internal val FULL_SETTER_COMMAND_REPLACEMENTS =
    listOf(
        FullSetterCommandReplacement(
            positiveCommand = "activate",
            negativeCommand = "deactivate",
            setterCommand = "setActive",
        ),
        FullSetterCommandReplacement(
            positiveCommand = "enable",
            negativeCommand = "disable",
            setterCommand = "setEnabled",
        ),
        FullSetterCommandReplacement(
            positiveCommand = "grant",
            negativeCommand = "revoke",
            setterCommand = "setUseApproved",
        ),
    )

internal fun commandsReplacedByFullSetter(commandNames: Iterable<String>): Set<String> {
    val availableCommands = commandNames.toSet()
    return FULL_SETTER_COMMAND_REPLACEMENTS
        .filter { replacement ->
            availableCommands.containsAll(
                setOf(
                    replacement.positiveCommand,
                    replacement.negativeCommand,
                    replacement.setterCommand,
                ),
            )
        }.flatMapTo(linkedSetOf()) { it.oppositeCommands }
}
