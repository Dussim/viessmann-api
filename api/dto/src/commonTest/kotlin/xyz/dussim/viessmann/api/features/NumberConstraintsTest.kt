package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.NumberConstraints

class NumberConstraintsTest :
    FunSpec({
        test("test validate returns true for parameter within bounds") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.5)

            constraints.validate(2.5) shouldBe true
        }

        test("test validate returns false for parameter smaller than min") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.5)

            constraints.validate(-1.0) shouldBe false
        }

        test("test validate returns false for parameter greater than max") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.5)

            constraints.validate(10.5) shouldBe false
        }

        context("test validate returns false for parameter not adhering to stepping") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.5)

            withData(
                2.1,
                2.2,
                2.3,
                2.4,
                2.6,
                2.7,
                2.8,
                2.9,
            ) {
                constraints.validate(it) shouldBe false
            }
        }

        test("test validate returns true for parameter equal to min") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.5)

            constraints.validate(0.0) shouldBe true
        }

        test("test validate returns true for parameter equal to max") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.5)

            constraints.validate(10.0) shouldBe true
        }

        test("test validate returns false when stepping is not positive") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.0)

            constraints.validate(5.0) shouldBe false
        }

        test("test validate returns false when stepping is not negative") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = -0.5)

            constraints.validate(5.0) shouldBe false
        }

        test("test validate returns true for invalid steps due to floating-point imprecision") {
            val constraints = NumberConstraints(min = 0.0, max = 10.0, stepping = 0.1)

            constraints.validate(0.30000000000000004) shouldBe true
        }
    })
