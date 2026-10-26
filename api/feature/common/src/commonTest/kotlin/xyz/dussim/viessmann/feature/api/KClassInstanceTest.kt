package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlin.reflect.KClass

val KClassInstanceTest by testSuite {
    val classes: List<KClass<*>> = listOf(DoubleValue::class, StringValue::class, PropertyValue::class, Feature::class, Any::class)
    val values: List<Any?> = listOf(DoubleValue(1.0), StringValue("a"), ListEmptyValue, "text", null)

    test("hasInstance agrees with KClass.isInstance") {
        for (type in classes) {
            for (value in values) {
                type.hasInstance(value) shouldBe type.isInstance(value)
            }
        }
    }

    test("hasInstance accepts subtypes and rejects null") {
        PropertyValue::class.hasInstance(DoubleValue(1.0)) shouldBe true
        DoubleValue::class.hasInstance(StringValue("a")) shouldBe false
        DoubleValue::class.hasInstance(null) shouldBe false
    }
}
