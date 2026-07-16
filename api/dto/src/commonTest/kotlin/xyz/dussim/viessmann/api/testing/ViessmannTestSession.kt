package xyz.dussim.viessmann.api.testing

import de.infix.testBalloon.framework.core.TestSession
import de.infix.testBalloon.integration.kotest.assertions.kotestAssertionsSupport

class ViessmannTestSession : TestSession(testConfig = DefaultConfiguration.kotestAssertionsSupport())
