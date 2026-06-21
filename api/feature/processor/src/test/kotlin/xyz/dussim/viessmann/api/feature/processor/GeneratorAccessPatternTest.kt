package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.typeNameOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.NumberConstraints

class GeneratorAccessPatternTest :
    FunSpec({
        test("feature generator emits requirePropertyValue for required scalar property") {
            val code =
                buildPropertyInitCode(
                    ParameterProperty(
                        name = "temperature",
                        type = typeNameOf<DoubleValue>(),
                        isListProperty = false,
                        isEnumProperty = false,
                    ),
                )

            code shouldContain "requirePropertyValue<xyz.dussim.viessmann.feature.api.DoubleValue>(\"temperature\""
            code shouldContain "temperature = "
            code shouldNotContain "!!"
        }

        test("feature generator emits findPropertyValueOrNull for nullable scalar property") {
            val code =
                buildPropertyInitCode(
                    ParameterProperty(
                        name = "temperature",
                        type = typeNameOf<DoubleValue?>(),
                        isListProperty = false,
                        isEnumProperty = false,
                    ),
                )

            code shouldContain "findPropertyValueOrNull<xyz.dussim.viessmann.feature.api.DoubleValue>(\"temperature\""
            code shouldContain "temperature = "
            code shouldNotContain "!!"
        }

        test("feature generator emits requirePropertyValueOrPromoteEmpty for required list property") {
            val code =
                buildPropertyInitCode(
                    ParameterProperty(
                        name = "supported",
                        type = typeNameOf<ListStringValue>(),
                        isListProperty = true,
                        isEnumProperty = false,
                    ),
                )

            code shouldContain
                "requirePropertyValueOrPromoteEmpty<xyz.dussim.viessmann.feature.api.ListStringValue>(\"supported\""
            code shouldContain "supported = "
            code shouldContain "ListStringValue.EMPTY"
            code shouldNotContain "!!"
        }

        test("feature generator emits findPropertyValueOrPromoteEmpty for nullable list property") {
            val code =
                buildPropertyInitCode(
                    ParameterProperty(
                        name = "supported",
                        type = typeNameOf<ListStringValue?>(),
                        isListProperty = true,
                        isEnumProperty = false,
                    ),
                )

            code shouldContain
                "findPropertyValueOrPromoteEmpty<xyz.dussim.viessmann.feature.api.ListStringValue>(\"supported\""
            code shouldContain "supported = "
            code shouldContain "ListStringValue.EMPTY"
            code shouldNotContain "!!"
        }

        test("feature generator emits requireCommand for required command property") {
            val code =
                buildCommandInitCode(
                    CommandProperty(
                        name = "setValue",
                        implType = ClassName("test.commands", "SetValueImpl"),
                        signature = CommandSignature(name = "SetValue", parameters = emptyList()),
                        validationName = "setValue",
                        isNullable = false,
                    ),
                )

            code shouldContain """requireCommand("setValue""""
            code shouldContain "SetValueImpl(commands."
            code shouldNotContain "delegate.commands"
            code shouldNotContain "!!"
        }

        test("feature generator emits cached feature access locals when needed") {
            buildDelegateAccessLocalsCode(hasProperties = true, hasCommands = false) shouldContain "val properties = properties"
            buildDelegateAccessLocalsCode(hasProperties = false, hasCommands = true) shouldContain "val commands = `commands`"

            val code = buildDelegateAccessLocalsCode(hasProperties = true, hasCommands = true)

            code shouldContain "val properties = properties"
            code shouldContain "val commands = `commands`"
            code shouldNotContain "!!"
        }

        test("feature fail-fast generator emits command fail-fast rule for parameterized command") {
            val property =
                CommandProperty(
                    name = "setValue",
                    implType = ClassName("test.commands", "SetValueImpl"),
                    signature =
                        CommandSignature(
                            name = "SetValue",
                            parameters = listOf("value" to typeNameOf<NumberConstraints>()),
                        ),
                    validationName = "setValue",
                    isNullable = false,
                )

            commandRuleExpression(property).toString() shouldContain "test.commands.SetValueImpl.rule"
            commandRuleExpression(property, isFailFast = true).toString() shouldContain "test.commands.SetValueImpl.FailFast.rule"
        }

        test("command generator emits requireParam + requireConstraints for non-array constraints") {
            val code =
                constraintPropertyInitializer(
                    ConstraintProperty(
                        name = "value",
                        type = typeNameOf<NumberConstraints>(),
                    ),
                ).toString()

            code shouldContain """requireParam("value""""
            code shouldContain """requireConstraints<xyz.dussim.viessmann.feature.api.NumberConstraints>()"""
            code shouldNotContain "!!.constraints as"
        }

        test("command generator emits toArray*ConstraintsOrThrow for array constraints") {
            val code =
                constraintPropertyInitializer(
                    ConstraintProperty(
                        name = "equipment",
                        type = typeNameOf<ArrayStringConstraints>(),
                    ),
                ).toString()

            code shouldContain "command.params."
            code shouldContain """requireParam("equipment""""
            code shouldContain "toArrayStringConstraintsOrThrow()"
            code shouldNotContain "!!.constraints"
        }

        test("fail-fast generator directly returns single rule result") {
            val code =
                generateValidateFunction(
                    targetType = typeNameOf<Feature>(),
                    ruleExpressions = listOf(CodeBlock.of("singleRule")),
                    isFailFast = true,
                ).toString()

            code shouldContain "= singleRule.validate(value)"
            code shouldNotContain "val result0"
            code shouldNotContain "isInvalid"
            code shouldNotContain "Valid()"
        }

        test("fail-fast generator directly returns last rule result") {
            val code =
                generateValidateFunction(
                    targetType = typeNameOf<Feature>(),
                    ruleExpressions = listOf(CodeBlock.of("firstRule"), CodeBlock.of("secondRule")),
                    isFailFast = true,
                ).toString()

            code shouldContain "val result0 = firstRule.validate(value)"
            code shouldContain "if (result0.isInvalid) return result0"
            code shouldContain "return secondRule.validate(value)"
            code shouldNotContain "val result1"
            code shouldNotContain "Valid()"
        }

        test("regular generator directly returns single rule result") {
            val code =
                generateValidateFunction(
                    targetType = typeNameOf<Feature>(),
                    ruleExpressions = listOf(CodeBlock.of("singleRule")),
                ).toString()

            code shouldContain "= singleRule.validate(value)"
            code shouldNotContain "of("
        }

        test("feature generator reuses companion validation when fail-fast body would be identical") {
            requiresDedicatedFailFastRule(emptyList()) shouldBe false
            requiresDedicatedFailFastRule(listOf(CodeBlock.of("singleRule"))) shouldBe false
        }

        test("feature generator keeps dedicated fail-fast validation for aggregated rules") {
            requiresDedicatedFailFastRule(
                listOf(
                    CodeBlock.of("firstRule"),
                    CodeBlock.of("secondRule"),
                ),
            ) shouldBe true
        }

        test("feature signature implementation name is stable") {
            val signature =
                FeatureSignature(
                    baseFeature = BaseFeature.Feature,
                    properties =
                        listOf(
                            "active" to typeNameOf<DoubleValue>(),
                            "mode" to typeNameOf<ListStringValue?>(),
                        ),
                    commands =
                        listOf(
                            Triple(
                                "setValue",
                                CommandSignature(
                                    name = "SetValue",
                                    parameters = listOf("value" to typeNameOf<NumberConstraints>()),
                                ),
                                false,
                            ),
                        ),
                )

            signature.implName shouldBe "FeatFeatureActiveDoubleValueModeListStriGOB341Impl"
        }
    })

private fun buildPropertyInitCode(property: ParameterProperty): String {
    val builder = CodeBlock.builder()
    builder.addPropertyInitialization(property)
    return builder.build().toString()
}

private fun buildCommandInitCode(property: CommandProperty): String {
    val builder = CodeBlock.builder()
    builder.addCommandInitialization(property)
    return builder.build().toString()
}

private fun buildDelegateAccessLocalsCode(
    hasProperties: Boolean,
    hasCommands: Boolean,
): String {
    val builder = CodeBlock.builder()
    builder.addDelegateAccessLocals(hasProperties, hasCommands)
    return builder.build().toString()
}
