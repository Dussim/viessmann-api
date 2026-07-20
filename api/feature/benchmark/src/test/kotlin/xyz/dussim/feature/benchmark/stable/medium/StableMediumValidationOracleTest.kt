package xyz.dussim.feature.benchmark.stable.medium

import de.infix.testBalloon.framework.core.testSuite
import xyz.dussim.feature.benchmark.stable.StableSize
import xyz.dussim.feature.benchmark.stable.assertStableSuite

val StableMediumValidationOracleTest by testSuite {
    test("medium stable validation suite matches its manifest oracle") {
        assertStableSuite(StableSize.MEDIUM)
    }
}
