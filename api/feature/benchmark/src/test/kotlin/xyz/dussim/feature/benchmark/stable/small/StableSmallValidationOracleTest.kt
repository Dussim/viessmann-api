package xyz.dussim.feature.benchmark.stable.small

import de.infix.testBalloon.framework.core.testSuite
import xyz.dussim.feature.benchmark.stable.StableSize
import xyz.dussim.feature.benchmark.stable.assertStableSuite

val StableSmallValidationOracleTest by testSuite {
    test("small stable validation suite matches its manifest oracle") {
        assertStableSuite(StableSize.SMALL)
    }
}
