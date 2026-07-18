package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.typeNameOf
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.NumberConstraints

val GeneratorAccessPatternTest by testSuite {
    test("feature generator emits requirePropertyValue for required scalar property") {
        val code =
            buildPropertyInitCode(
                ParameterProperty(
                    name = "temperature",
                    type = typeNameOf<DoubleValue>(),
                    isEnumProperty = false,
                ),
            )

        code shouldContain "requirePropertyValue(\"temperature\""
        code shouldContain "xyz.dussim.viessmann.feature.api.DoubleValue::class"
        code shouldContain "this.temperature = "
        code shouldNotContain "!!"
    }

    test("feature generator emits findPropertyValueOrNull for nullable scalar property") {
        val code =
            buildPropertyInitCode(
                ParameterProperty(
                    name = "temperature",
                    type = typeNameOf<DoubleValue?>(),
                    isEnumProperty = false,
                ),
            )

        code shouldContain "findPropertyValueOrNull(\"temperature\""
        code shouldContain "xyz.dussim.viessmann.feature.api.DoubleValue::class"
        code shouldContain "this.temperature = "
        code shouldNotContain "!!"
    }

    test("feature generator emits requirePropertyValueOrPromoteEmpty for required list property") {
        val code =
            buildPropertyInitCode(
                ParameterProperty(
                    name = "supported",
                    type = typeNameOf<ListStringValue>(),
                    isEnumProperty = false,
                ),
            )

        code shouldContain
            "requirePropertyValueOrPromoteEmpty(\"supported\""
        code shouldContain "this.supported = "
        code shouldContain "ListStringValue.EMPTY"
        code shouldNotContain "!!"
    }

    test("feature generator emits findPropertyValueOrPromoteEmpty for nullable list property") {
        val code =
            buildPropertyInitCode(
                ParameterProperty(
                    name = "supported",
                    type = typeNameOf<ListStringValue?>(),
                    isEnumProperty = false,
                ),
            )

        code shouldContain
            "findPropertyValueOrPromoteEmpty(\"supported\""
        code shouldContain "this.supported = "
        code shouldContain "ListStringValue.EMPTY"
        code shouldNotContain "!!"
    }

    test("feature generator emits requireCommand for required command property") {
        val code =
            buildCommandInitCode(
                CommandProperty(
                    name = "setValue",
                    apiName = "set_value",
                    implType = ClassName("test.commands", "SetValueImpl"),
                    signature = CommandSignature(name = "SetValue", parameters = emptyList()),
                    isNullable = false,
                ),
            )

        code shouldContain """requireCommand("set_value""""
        code shouldContain "SetValueImpl(sourceCommands."
        code shouldNotContain "delegate.commands"
        code shouldNotContain "!!"
    }

    test("feature generator emits cached feature access locals when needed") {
        buildDelegateAccessLocalsCode(hasProperties = true, hasCommands = false) shouldContain
            "val sourceProperties = sourceFeature.properties"
        buildDelegateAccessLocalsCode(hasProperties = false, hasCommands = true) shouldContain
            "val sourceCommands = sourceFeature.commands"

        val code = buildDelegateAccessLocalsCode(hasProperties = true, hasCommands = true)

        code shouldContain "val sourceProperties = sourceFeature.properties"
        code shouldContain "val sourceCommands = sourceFeature.commands"
        code shouldNotContain "!!"
    }

    test("feature fail-fast generator emits command fail-fast rule for parameterized command") {
        val property =
            CommandProperty(
                name = "setValue",
                apiName = "setValue",
                implType = ClassName("test.commands", "SetValueImpl"),
                signature =
                    CommandSignature(
                        name = "SetValue",
                        parameters = listOf("value" to typeNameOf<NumberConstraints>()),
                    ),
                isNullable = false,
            )

        commandRuleExpression(property).toString() shouldContain "test.commands.SetValueImpl.rule"
        commandRuleExpression(property, isFailFast = true).toString() shouldContain "test.commands.SetValueImpl.failFastRule"
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
        code shouldContain """requireConstraints(xyz.dussim.viessmann.feature.api.NumberConstraints::class)"""
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

    test("command generator specializes one-parameter aggregate and fail-fast rules") {
        val registry = RuleRegistry("test.rules")
        val parameterRule =
            registry.register(
                function = validationRule("numberConstraintsRule"),
                args = listOf("value"),
                targetType = COMMAND_VALIDATION_RULE_TYPE,
            )
        val plan = oneParameterValidationRulePlan(registry, "setValue", parameterRule)
        val generatedRules = generateValidationRuleFiles(registry, chunkSize = 64).single().toString()

        generatedRules shouldContain "oneParameterCommandRule(\"setValue\""
        generatedRules shouldContain "oneParameterCommandFailFastRule(\"setValue\""
        generatedRules shouldNotContain "numberOfParametersRule"
        val atomicRuleIndex = generatedRules.indexOf("numberConstraintsRule(\"value\")")
        val compositeRuleIndex = generatedRules.indexOf("oneParameterCommandRule(\"setValue\"")
        (atomicRuleIndex < compositeRuleIndex) shouldBe true
        (plan.normalExpression.toString() == plan.failFastExpression.toString()) shouldBe false
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

    test("validation plan reuses normal rule when fail-fast behavior is identical") {
        ValidationPlan(emptyList()).requiresSeparateFailFast shouldBe false
        ValidationPlan(listOf(ValidationRulePlan(CodeBlock.of("singleRule")))).requiresSeparateFailFast shouldBe false
    }

    test("validation plan keeps separate fail-fast control flow for aggregated rules") {
        ValidationPlan(
            listOf(
                ValidationRulePlan(CodeBlock.of("firstRule")),
                ValidationRulePlan(CodeBlock.of("secondRule")),
            ),
        ).requiresSeparateFailFast shouldBe true
    }

    test("validation plan keeps separate fail-fast rule for a sole parameterized command") {
        ValidationPlan(
            listOf(
                ValidationRulePlan(
                    normalExpression = CodeBlock.of("CommandImpl.rule"),
                    failFastExpression = CodeBlock.of("CommandImpl.failFastRule"),
                ),
            ),
        ).requiresSeparateFailFast shouldBe true
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
                        CommandFeatureSignature(
                            propertyName = "setValue",
                            apiName = "setValue",
                            signature =
                                CommandSignature(
                                    name = "SetValue",
                                    parameters = listOf("value" to typeNameOf<NumberConstraints>()),
                                ),
                            isNullable = false,
                        ),
                    ),
            )

        signature.implName shouldBe "FeatFeatureActiveDoubleValueModeListStri3C193C8175CA4D50Impl"
    }

    test("generated descriptor names distinguish equal simple names in different packages") {
        val first = generateDescriptorName(ClassName("one.package", "SameFeature"))
        val second = generateDescriptorName(ClassName("another.package", "SameFeature"))

        (first == second) shouldBe false
    }

    test("rule names include the complete signature") {
        val function = validationRule("stringPropertyRule")
        val first = RuleSignature(function, listOf("a-b"), typeNameOf<Feature>()).generateName()
        val second = RuleSignature(function, listOf("a_b"), typeNameOf<Feature>()).generateName()

        (first == second) shouldBe false
    }

    test("processor options reject malformed and unsafe values") {
        val errors = mutableListOf<String>()
        val options =
            FeatureProcessorOptions.from(
                mapOf(
                    FeatureImplementationProcessorProvider.DESCRIPTORS_CHUNK_SIZE_OPTION to "0",
                    FeatureImplementationProcessorProvider.FORMAT_GENERATED_SOURCES_OPTION to "yes",
                    FeatureImplementationProcessorProvider.RENDER_PARALLELISM_OPTION to "999999999999",
                ),
                errors::add,
            )

        options.descriptorsChunkSize shouldBe FeatureImplementationProcessorProvider.DEFAULT_DESCRIPTORS_CHUNK_SIZE
        options.formatGeneratedSources shouldBe true
        errors.size shouldBe 3
    }

    test("processor option descriptor chunk size is measured in logical descriptors") {
        val options =
            FeatureProcessorOptions.from(
                mapOf(FeatureImplementationProcessorProvider.DESCRIPTORS_CHUNK_SIZE_OPTION to "64"),
            )

        options.descriptorsChunkSize shouldBe 64
    }

    test("processor option validation rule chunk size is measured in logical rules") {
        val options =
            FeatureProcessorOptions.from(
                mapOf(FeatureImplementationProcessorProvider.VALIDATION_RULES_CHUNK_SIZE_OPTION to "32"),
            )

        options.validationRulesChunkSize shouldBe 32
    }

    test("validation rules are split at the configured boundary") {
        fun generatedFiles(ruleCount: Int) =
            generateValidationRuleFiles(
                ruleRegistry =
                    RuleRegistry("test.rules").apply {
                        repeat(ruleCount) { index ->
                            register(
                                function = validationRule("booleanPropertyRule"),
                                args = listOf("property$index", true),
                                targetType = FEATURE_VALIDATION_RULE_TYPE,
                            )
                        }
                    },
                chunkSize = 64,
            )

        generatedFiles(1).single().name shouldBe "ValidationRules"
        Regex("internal val ").findAll(generatedFiles(64).single().toString()).count() shouldBe 64

        val filesAtBoundary = generatedFiles(65)
        filesAtBoundary.map { it.name } shouldBe listOf("ValidationRules1", "ValidationRules2")
        Regex("internal val ").findAll(filesAtBoundary.first().toString()).count() shouldBe 64
        Regex("internal val ").findAll(filesAtBoundary.last().toString()).count() shouldBe 1
    }

    test("one type adapter supplies validation access conversion and naming metadata") {
        val propertyAdapter = PROPERTY_TYPE_ADAPTERS.getValue(typeNameOf<DoubleValue>())
        propertyAdapter.validationRule.simpleName shouldBe "doublePropertyRule"
        propertyAdapter.requiredAccessor?.simpleName shouldBe "requirePropertyValue"
        propertyAdapter.optionalAccessor?.simpleName shouldBe "findPropertyValueOrNull"

        val constraintAdapter = CONSTRAINT_TYPE_ADAPTERS.getValue(typeNameOf<ArrayStringConstraints>())
        constraintAdapter.validationRule.simpleName shouldBe "arrayStringConstraintsRule"
        constraintAdapter.constraintConverter?.simpleName shouldBe "toArrayStringConstraintsOrThrow"
        constraintAdapter.abbreviation shouldBe "AS"
    }
}

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
