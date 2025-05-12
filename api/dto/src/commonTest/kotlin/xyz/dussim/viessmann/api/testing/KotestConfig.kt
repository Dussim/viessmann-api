package xyz.dussim.viessmann.api.testing

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.names.TestNameCase
import io.kotest.engine.concurrency.SpecExecutionMode
import io.kotest.engine.concurrency.TestExecutionMode

object KotestConfig : AbstractProjectConfig() {
    override val testNameCase = TestNameCase.AsIs

    override val dumpConfig = true

    override val testExecutionMode = TestExecutionMode.Concurrent
    override val specExecutionMode = SpecExecutionMode.Concurrent
}
