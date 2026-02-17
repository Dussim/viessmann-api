@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

@RequiresOptIn(
    message = "constraintN accessors should not be used directly unless you operate on CommmandN directly and not subclass. Use replacementProperty instead.",
    level = RequiresOptIn.Level.WARNING,
)
annotation class CommandIndexedConstraintUsage(
    val replacementProperty: String = "",
)

interface OfCommand {
    val command: Command
}

interface Command0 : OfCommand

interface Command1<T1> : OfCommand {
    @CommandIndexedConstraintUsage
    val constraint1: Constraints<T1>
}

interface Command2<T1, T2> : OfCommand {
    @CommandIndexedConstraintUsage
    val constraint1: Constraints<T1>

    @CommandIndexedConstraintUsage
    val constraint2: Constraints<T2>
}

interface Command3<T1, T2, T3> : OfCommand {
    @CommandIndexedConstraintUsage
    val constraint1: Constraints<T1>

    @CommandIndexedConstraintUsage
    val constraint2: Constraints<T2>

    @CommandIndexedConstraintUsage
    val constraint3: Constraints<T3>
}

interface Command4<T1, T2, T3, T4> : OfCommand {
    @CommandIndexedConstraintUsage
    val constraint1: Constraints<T1>

    @CommandIndexedConstraintUsage
    val constraint2: Constraints<T2>

    @CommandIndexedConstraintUsage
    val constraint3: Constraints<T3>

    @CommandIndexedConstraintUsage
    val constraint4: Constraints<T4>
}

interface Command5<T1, T2, T3, T4, T5> : OfCommand {
    @CommandIndexedConstraintUsage
    val constraint1: Constraints<T1>

    @CommandIndexedConstraintUsage
    val constraint2: Constraints<T2>

    @CommandIndexedConstraintUsage
    val constraint3: Constraints<T3>

    @CommandIndexedConstraintUsage
    val constraint4: Constraints<T4>

    @CommandIndexedConstraintUsage
    val constraint5: Constraints<T5>
}

interface Command6<T1, T2, T3, T4, T5, T6> : OfCommand {
    @CommandIndexedConstraintUsage
    val constraint1: Constraints<T1>

    @CommandIndexedConstraintUsage
    val constraint2: Constraints<T2>

    @CommandIndexedConstraintUsage
    val constraint3: Constraints<T3>

    @CommandIndexedConstraintUsage
    val constraint4: Constraints<T4>

    @CommandIndexedConstraintUsage
    val constraint5: Constraints<T5>

    @CommandIndexedConstraintUsage
    val constraint6: Constraints<T6>
}
