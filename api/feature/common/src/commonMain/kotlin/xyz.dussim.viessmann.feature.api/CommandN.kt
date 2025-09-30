@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

interface OfCommand {
    val command: Command
}

interface Command0 : OfCommand

interface Command1<T1> : OfCommand {
    val constraint1: Constraints<T1>
}

interface Command2<T1, T2> : OfCommand {
    val constraint1: Constraints<T1>
    val constraint2: Constraints<T2>
}

interface Command3<T1, T2, T3> : OfCommand {
    val constraint1: Constraints<T1>
    val constraint2: Constraints<T2>
    val constraint3: Constraints<T3>
}

interface Command4<T1, T2, T3, T4> : OfCommand {
    val constraint1: Constraints<T1>
    val constraint2: Constraints<T2>
    val constraint3: Constraints<T3>
    val constraint4: Constraints<T4>
}

interface Command5<T1, T2, T3, T4, T5> : OfCommand {
    val constraint1: Constraints<T1>
    val constraint2: Constraints<T2>
    val constraint3: Constraints<T3>
    val constraint4: Constraints<T4>
    val constraint5: Constraints<T5>
}

interface Command6<T1, T2, T3, T4, T5, T6> : OfCommand {
    val constraint1: Constraints<T1>
    val constraint2: Constraints<T2>
    val constraint3: Constraints<T3>
    val constraint4: Constraints<T4>
    val constraint5: Constraints<T5>
    val constraint6: Constraints<T6>
}
