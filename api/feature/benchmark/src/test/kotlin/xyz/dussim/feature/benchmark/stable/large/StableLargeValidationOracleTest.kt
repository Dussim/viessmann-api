package xyz.dussim.feature.benchmark.stable.large

import de.infix.testBalloon.framework.core.testSuite
import xyz.dussim.feature.benchmark.stable.StableSize
import xyz.dussim.feature.benchmark.stable.assertStableSuite

val StableLargeValidationOracleTest by testSuite {
    test("large stable validation suite matches its manifest oracle") {
        assertStableSuite(StableSize.LARGE)
    }
}
