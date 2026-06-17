package xyz.dussim.viessmann.api.features.generated

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.feature.api.ArrayBooleanConstraints
import xyz.dussim.viessmann.feature.api.ArrayNumberConstraints
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureDescriptor
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectConstraints
import xyz.dussim.viessmann.feature.api.OfCommand
import xyz.dussim.viessmann.feature.api.PropertyValue
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.viessmann.feature.api.json

inline fun <F : Feature> featureJsonTest(
    resourcePath: String,
    descriptor: FeatureDescriptor<F>,
    assertions: F.(F) -> Unit,
) {
    val content =
        Thread
            .currentThread()
            .contextClassLoader
            .getResource(resourcePath)!!
            .readText()
    val genericFeature = json.decodeFromString(ViessmannFeature.serializer(), content)
    val _ = assertSoftly(descriptor.getOrThrow(genericFeature), assertions)
}

infix fun <T> PropertyValue<T>?.shouldHaveElement(expected: T) {
    val _ = this?.element shouldBe expected
}

fun OfCommand?.shouldHaveCommand(
    name: String,
    isExecutable: Boolean,
    uri: String,
) {
    val _ = this?.command?.name shouldBe name
    val _ = this?.command?.isExecutable shouldBe isExecutable
    val _ = this?.command?.uri shouldBe uri
}

fun StringConstraints?.shouldHaveStringConstraints(
    minLength: Int? = null,
    maxLength: Int? = null,
    regEx: String? = null,
    enum: List<String>? = null,
    sameDayAllowed: Boolean? = null,
) {
    val _ = this?.minLength shouldBe minLength
    val _ = this?.maxLength shouldBe maxLength
    val _ = this?.regEx shouldBe regEx
    val _ = this?.enum shouldBe enum
    val _ = this?.sameDayAllowed shouldBe sameDayAllowed
}

fun NumberConstraints?.shouldHaveNumberConstraints(
    min: Double? = null,
    efficientLowerBorder: Double? = null,
    efficientUpperBorder: Double? = null,
    max: Double? = null,
    stepping: Double? = null,
    enum: List<Double>? = null,
) {
    val _ = this?.min shouldBe min
    val _ = this?.efficientLowerBorder shouldBe efficientLowerBorder
    val _ = this?.efficientUpperBorder shouldBe efficientUpperBorder
    val _ = this?.max shouldBe max
    val _ = this?.stepping shouldBe stepping
    val _ = this?.enum shouldBe enum
}

fun ArrayStringConstraints?.shouldHaveArrayStringConstraints(
    minLength: Int? = null,
    maxLength: Int? = null,
    enum: List<String>? = null,
) {
    val _ = this?.minLength shouldBe minLength
    val _ = this?.maxLength shouldBe maxLength
    val _ = this?.enum shouldBe enum
}

fun ArrayNumberConstraints?.shouldHaveArrayNumberConstraints(
    minLength: Int? = null,
    maxLength: Int? = null,
    enum: List<Double>? = null,
) {
    val _ = this?.minLength shouldBe minLength
    val _ = this?.maxLength shouldBe maxLength
    val _ = this?.enum shouldBe enum
}

fun ArrayBooleanConstraints?.shouldHaveArrayBooleanConstraints(
    minLength: Int? = null,
    maxLength: Int? = null,
    enum: List<Boolean>? = null,
) {
    val _ = this?.minLength shouldBe minLength
    val _ = this?.maxLength shouldBe maxLength
    val _ = this?.enum shouldBe enum
}

fun ObjectConstraints?.shouldHaveObjectConstraints(
    minProperties: Int? = null,
    maxProperties: Int? = null,
    required: List<String>? = null,
) {
    val _ = this?.minProperties shouldBe minProperties
    val _ = this?.maxProperties shouldBe maxProperties
    val _ = this?.required shouldBe required
}

fun ScheduleConstraints?.shouldHaveScheduleConstraints(
    modes: List<String>,
    maxEntries: Int,
    resolution: Int,
    defaultMode: String,
    overlapAllowed: Boolean,
) {
    val _ = this?.modes shouldBe modes
    val _ = this?.maxEntries shouldBe maxEntries
    val _ = this?.resolution shouldBe resolution
    val _ = this?.defaultMode shouldBe defaultMode
    val _ = this?.overlapAllowed shouldBe overlapAllowed
}
