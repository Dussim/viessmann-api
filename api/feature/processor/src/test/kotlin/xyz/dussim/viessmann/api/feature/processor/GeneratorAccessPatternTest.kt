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
import xyz.dussim.viessmann.feature.api.NullableStringValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.validation.propertyHash

val GeneratorAccessPatternTest by testSuite {
    test("optional command validates through a direct companion call") {
        val registry = RuleRegistry("test.rules")
        val command =
            CommandProperty(
                name = "optionalCommand",
                apiName = "setValue",
                implType = ClassName("test", "CommandImpl").copy(nullable = true),
                signature = CommandSignature("SetValue", listOf("value" to typeNameOf<NumberConstraints>())),
                isNullable = true,
            )
        val hash = hashOf("setValue")
        val normal = generateValidateFunction(typeNameOf<Feature>(), listOf(commandResultExpression(command, registry))).toString()
        val failFast =
            generateValidateFunction(
                typeNameOf<Feature>(),
                listOf(commandResultExpression(command, registry, isFailFast = true)),
                isFailFast = true,
            ).toString()

        normal shouldContain "value.commands[\"setValue\", $hash]"
        normal shouldContain "ValidationResult.Valid else test.CommandImpl.validate(it)"
        failFast shouldContain "ValidationResult.Valid else test.CommandImpl.validateFailFast(it)"
        listOf(normal, failFast).forEach { code ->
            code shouldNotContain "optionalCommandRule"
            code shouldNotContain "optionalCommandFailFastRule"
            code shouldNotContain ".rule"
            code shouldNotContain "?.let"
        }
        registry.getAllRules().isEmpty() shouldBe true
    }

    test("required command validates through a direct companion call with a shared missing result") {
        val registry = RuleRegistry("test.rules")
        val command =
            CommandProperty(
                name = "setValue",
                apiName = "setValue",
                implType = ClassName("test.commands", "SetValueImpl"),
                signature = CommandSignature("SetValue", listOf("value" to typeNameOf<NumberConstraints>())),
                isNullable = false,
            )
        val normal = commandResultExpression(command, registry).toString()
        val failFast = commandResultExpression(command, registry, isFailFast = true).toString()

        val missing = registry.getAllRules().entries.single()
        missing.key.function.simpleName shouldBe "missingCommandResult"
        missing.key.args shouldBe listOf("setValue")
        normal shouldContain "if (it == null) ${missing.value.canonicalName} else test.commands.SetValueImpl.validate(it)"
        failFast shouldContain "if (it == null) ${missing.value.canonicalName} else test.commands.SetValueImpl.validateFailFast(it)"
        // `.rule` alone would match the generated rules package (`test.rules`); the companion property is `Impl.rule`.
        normal shouldNotContain "Impl.rule"
        failFast shouldNotContain "failFastRule"
        // KotlinPoet may wrap after `=`, so compare with whitespace collapsed.
        generateValidationRuleFiles(registry, chunkSize = 64).single().toString().replace(Regex("\\s+"), " ") shouldContain
            "ValidationResult<ValidationError> = missingCommandResult(\"setValue\")"
    }

    test("size bounds count distinct required property and command names only") {
        val properties =
            listOf(
                ParameterProperty(name = "a", type = typeNameOf<DoubleValue>(), isEnumProperty = false),
                ParameterProperty(name = "a", type = typeNameOf<DoubleValue>(), isEnumProperty = false),
                ParameterProperty(name = "b", type = typeNameOf<DoubleValue>(), isEnumProperty = false),
                ParameterProperty(name = "optional", type = typeNameOf<DoubleValue>().copy(nullable = true), isEnumProperty = false),
            )

        fun command(
            apiName: String,
            isNullable: Boolean,
        ) = CommandProperty(
            name = apiName,
            apiName = apiName,
            implType = ClassName("test", "${apiName}Impl").copy(nullable = isNullable),
            signature = CommandSignature(apiName, emptyList()),
            isNullable = isNullable,
        )
        val commands = listOf(command("set", false), command("set", false), command("reset", true))

        requiredPropertyCount(properties) shouldBe 2
        requiredCommandCount(commands) shouldBe 1
        requiredPropertyCount(emptyList()) shouldBe 0
        requiredCommandCount(emptyList()) shouldBe 0
    }

    test("single-pass factory rejects a missing or mistyped required scalar property") {
        val code = buildPropertyExtractionCode(ParameterProperty("temperature", typeNameOf<DoubleValue>(), isEnumProperty = false))

        code shouldBe
            "val temperature = sourceProperties[\"temperature\", ${hashOf("temperature")}]?.value as? " +
            "xyz.dussim.viessmann.feature.api.DoubleValue ?: return null\n"
        code shouldNotContain "::class"
        code shouldNotContain "throw"
    }

    test("single-pass factory accepts an absent optional scalar property but rejects a mistyped one") {
        val code = buildPropertyExtractionCode(ParameterProperty("temperature", typeNameOf<DoubleValue?>(), isEnumProperty = false))

        code shouldContain "sourceProperties[\"temperature\", ${hashOf("temperature")}].let { if (it == null) null else "
        code shouldContain "it.value as? xyz.dussim.viessmann.feature.api.DoubleValue ?: return null }"
    }

    test("single-pass factory wraps non-null values for nullable value properties") {
        val required = buildPropertyExtractionCode(ParameterProperty("name", typeNameOf<NullableStringValue>(), isEnumProperty = false))
        val optional = buildPropertyExtractionCode(ParameterProperty("name", typeNameOf<NullableStringValue?>(), isEnumProperty = false))

        // Extension members render fully qualified in a bare CodeBlock; generated files import them.
        required shouldContain "]?.value?.xyz.dussim.viessmann.feature.api.toNullableStringValueOrNull() ?: return null"
        optional shouldContain "if (it == null) null else it.value.xyz.dussim.viessmann.feature.api.toNullableStringValueOrNull() ?: return null"
    }

    test("single-pass factory promotes empty lists and rejects mistyped or missing required lists") {
        val code = buildPropertyExtractionCode(ParameterProperty("supported", typeNameOf<ListStringValue>(), isEnumProperty = false))

        code shouldContain "val supported = when (val found = sourceProperties[\"supported\", ${hashOf("supported")}]?.value) {"
        code shouldContain "null -> return null"
        code shouldContain "is xyz.dussim.viessmann.feature.api.ListEmptyValue -> xyz.dussim.viessmann.feature.api.ListStringValue.EMPTY"
        code shouldContain "is xyz.dussim.viessmann.feature.api.ListStringValue -> found"
        code shouldContain "else -> return null"
    }

    test("single-pass factory keeps EMPTY for an absent optional list") {
        val code = buildPropertyExtractionCode(ParameterProperty("supported", typeNameOf<ListStringValue?>(), isEnumProperty = false))

        code shouldContain "null -> xyz.dussim.viessmann.feature.api.ListStringValue.EMPTY"
        code shouldContain "else -> return null"
    }

    test("single-pass factory wraps enum properties after checking the underlying value") {
        val enumType = ClassName("test", "Feature", "Mode")
        val property =
            ParameterProperty(
                name = "mode",
                type = enumType,
                isEnumProperty = true,
                underlyingType = typeNameOf<StringValue>(),
                validationFunction = validationRule("stringPropertyRule"),
            )

        buildPropertyExtractionCode(property) shouldBe
            "val mode = test.Feature.Mode(sourceProperties[\"mode\", ${hashOf("mode")}]?.value as? " +
            "xyz.dussim.viessmann.feature.api.StringValue ?: return null)\n"
        buildPropertyExtractionCode(property.copy(type = enumType.copy(nullable = true))) shouldContain
            "if (it == null) null else test.Feature.Mode(it.value as? xyz.dussim.viessmann.feature.api.StringValue ?: return null)"
    }

    test("single-pass factory builds commands through their companion createOrNull") {
        val command =
            CommandProperty(
                name = "setValue",
                apiName = "set_value",
                implType = ClassName("test.commands", "SetValueImpl"),
                signature = CommandSignature(name = "SetValue", parameters = emptyList()),
                isNullable = false,
            )

        buildCommandExtractionCode(command) shouldBe
            "val setValue = sourceCommands[\"set_value\", ${hashOf("set_value")}]?.let { " +
            "test.commands.SetValueImpl.createOrNull(it) } ?: return null\n"
        buildCommandExtractionCode(command.copy(implType = command.implType.copy(nullable = true), isNullable = true)) shouldContain
            "if (it == null) null else test.commands.SetValueImpl.createOrNull(it) ?: return null"
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

    test("single-pass factory rejects undersized features before any lookup") {
        buildSizeGateCode(2, 1) shouldBe "if (sourceProperties.size < 2 || sourceCommands.size < 1) return null\n"
        buildSizeGateCode(3, 0) shouldBe "if (sourceProperties.size < 3) return null\n"
        buildSizeGateCode(0, 2) shouldBe "if (sourceCommands.size < 2) return null\n"
        buildSizeGateCode(0, 0) shouldBe ""
    }

    test("command single-pass factory type-checks non-array constraints") {
        buildConstraintExtractionCode(ConstraintProperty("value", typeNameOf<NumberConstraints>())) shouldBe
            "val `value` = command.params[\"value\", ${hashOf("value")}]?.constraints as? " +
            "xyz.dussim.viessmann.feature.api.NumberConstraints ?: return null\n"
    }

    test("command single-pass factory converts array constraints without throwing") {
        val code = buildConstraintExtractionCode(ConstraintProperty("equipment", typeNameOf<ArrayStringConstraints>()))

        code shouldContain
            "command.params[\"equipment\", ${hashOf("equipment")}]?.constraints?.xyz.dussim.viessmann.feature.api.toArrayStringConstraintsOrNull() ?: return null"
        code shouldNotContain "OrThrow"
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

    test("rule result applies the rule to the validated value") {
        ruleResult(validationRule("someRule")).toString() shouldBe
            "xyz.dussim.viessmann.feature.api.validation.someRule.validate(value)"
    }

    test("fail-fast generator directly returns single rule result") {
        val code =
            generateValidateFunction(
                targetType = typeNameOf<Feature>(),
                ruleExpressions = listOf(CodeBlock.of("singleResult")),
                isFailFast = true,
            ).toString()

        code shouldContain "= singleResult"
        code shouldNotContain "val result0"
        code shouldNotContain "isInvalid"
        code shouldNotContain "Valid()"
    }

    test("fail-fast generator directly returns last rule result") {
        val code =
            generateValidateFunction(
                targetType = typeNameOf<Feature>(),
                ruleExpressions = listOf(CodeBlock.of("firstResult"), CodeBlock.of("secondResult")),
                isFailFast = true,
            ).toString()

        code shouldContain "val result0 = firstResult\n"
        code shouldContain "if (result0.isInvalid) return result0"
        code shouldContain "return secondResult\n"
        code shouldNotContain "val result1"
        code shouldNotContain "Valid()"
    }

    test("regular generator directly returns single rule result") {
        val code =
            generateValidateFunction(
                targetType = typeNameOf<Feature>(),
                ruleExpressions = listOf(CodeBlock.of("singleResult")),
            ).toString()

        code shouldContain "= singleResult"
        code shouldNotContain "of("
    }

    test("regular generator aggregates result expressions as they are") {
        val code =
            generateValidateFunction(
                targetType = typeNameOf<Feature>(),
                ruleExpressions = listOf(CodeBlock.of("firstResult"), CodeBlock.of("secondResult")),
            ).toString()

        code shouldContain "firstResult,"
        code shouldContain "secondResult,"
        code shouldNotContain ".validate(value)"
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
                    normalExpression = CodeBlock.of("CommandImpl.validate(it)"),
                    failFastExpression = CodeBlock.of("CommandImpl.validateFailFast(it)"),
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
        propertyAdapter.propertyAccessorStrategy shouldBe PropertyAccessorStrategy.STANDARD

        val nullableAdapter = PROPERTY_TYPE_ADAPTERS.getValue(typeNameOf<NullableStringValue>())
        nullableAdapter.validationRule.simpleName shouldBe "nullableStringPropertyRule"
        nullableAdapter.valueConverter?.simpleName shouldBe "toNullableStringValueOrNull"

        val constraintAdapter = CONSTRAINT_TYPE_ADAPTERS.getValue(typeNameOf<ArrayStringConstraints>())
        constraintAdapter.validationRule.simpleName shouldBe "arrayStringConstraintsRule"
        constraintAdapter.constraintConverter?.simpleName shouldBe "toArrayStringConstraintsOrNull"
        constraintAdapter.abbreviation shouldBe "AS"
    }
}

private fun buildSizeGateCode(
    minPropertyCount: Int,
    minCommandCount: Int,
): String {
    val builder = CodeBlock.builder()
    builder.addSizeGate(minPropertyCount, minCommandCount)
    return builder.build().toString()
}

private fun buildPropertyExtractionCode(property: ParameterProperty): String {
    val builder = CodeBlock.builder()
    builder.addPropertyExtraction(property)
    return builder.build().toString()
}

private fun buildCommandExtractionCode(property: CommandProperty): String {
    val builder = CodeBlock.builder()
    builder.addCommandExtraction(property)
    return builder.build().toString()
}

private fun buildConstraintExtractionCode(property: ConstraintProperty): String {
    val builder = CodeBlock.builder()
    builder.addConstraintExtraction(property)
    return builder.build().toString()
}

/** The precomputed lookup hash of [name] as the generator renders it (KotlinPoet adds digit separators to `%L` numbers). */
private fun hashOf(name: String): String = CodeBlock.of("%L", propertyHash(name.hashCode(), name.length)).toString()

private fun buildDelegateAccessLocalsCode(
    hasProperties: Boolean,
    hasCommands: Boolean,
): String {
    val builder = CodeBlock.builder()
    builder.addDelegateAccessLocals(hasProperties, hasCommands)
    return builder.build().toString()
}
