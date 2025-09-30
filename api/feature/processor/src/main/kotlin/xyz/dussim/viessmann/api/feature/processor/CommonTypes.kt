package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import xyz.dussim.viessmann.feature.api.validation.ValidationResult

const val VALIDATION_PACKAGE = "xyz.dussim.viessmann.feature.api.validation"
const val DELEGATE = "delegate"
const val COMMAND = "command"

val validateAll = MemberName(VALIDATION_PACKAGE, "validateAll")
val commandRule = MemberName(VALIDATION_PACKAGE, "commandRule")
val validationResultOf = ValidationResult.Companion::class.member("of")
