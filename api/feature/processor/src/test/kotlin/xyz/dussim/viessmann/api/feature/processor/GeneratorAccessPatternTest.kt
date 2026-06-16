package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Origin
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
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class GeneratorAccessPatternTest :
    FunSpec({
        test("feature generator emits requirePropertyValue for required scalar property") {
            val code =
                buildPropertyInitCode(
                    ParameterProperty(
                        name = "temperature",
                        type = typeNameOf<DoubleValue>(),
                        property = dummyClassDeclaration(),
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
                        property = dummyClassDeclaration(),
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
                        property = dummyClassDeclaration(),
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
                        property = dummyClassDeclaration(),
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
                        type = ClassName("test.commands", "SetValue"),
                        implType = ClassName("test.commands", "SetValueImpl"),
                        signature = CommandSignature(name = "SetValue", parameters = emptyList()),
                        validationName = "setValue",
                        command = dummyClassDeclaration(),
                        isNullable = false,
                    ),
                )

            code shouldContain """requireCommand("setValue""""
            code shouldContain "SetValueImpl(delegate.commands."
            code shouldNotContain "!!"
        }

        test("feature fail-fast generator emits command fail-fast rule for parameterized command") {
            val property =
                CommandProperty(
                    name = "setValue",
                    type = ClassName("test.commands", "SetValue"),
                    implType = ClassName("test.commands", "SetValueImpl"),
                    signature =
                        CommandSignature(
                            name = "SetValue",
                            parameters = listOf("value" to typeNameOf<NumberConstraints>()),
                        ),
                    validationName = "setValue",
                    command = dummyClassDeclaration(),
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
                        property = dummyClassDeclaration(),
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
                        property = dummyClassDeclaration(),
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

private fun dummyClassDeclaration(
    packageName: String = "test",
    simpleName: String = "Dummy",
): KSClassDeclaration {
    val simple = ksName(simpleName)
    val packageKsName = ksName(packageName)
    val qualified = if (packageName.isBlank()) simpleName else "$packageName.$simpleName"
    val qualifiedKsName = ksName(qualified)

    val handler =
        InvocationHandler { proxy, method, args ->
            when (method.name) {
                "getSimpleName" -> simple
                "getPackageName" -> packageKsName
                "getQualifiedName" -> qualifiedKsName
                "getAnnotations" -> emptySequence<KSAnnotation>()
                "getClassKind" -> ClassKind.INTERFACE
                "getDeclarations" -> emptySequence<KSDeclaration>()
                "getSuperTypes" -> emptySequence<KSTypeReference>()
                "getTypeParameters" -> emptyList<KSTypeParameter>()
                "getPrimaryConstructor" -> null
                "isCompanionObject" -> false
                "getParentDeclaration" -> null
                "getContainingFile" -> null
                "getDocString" -> null
                "getModifiers" -> emptySet<Any>()
                "getOrigin" -> Origin.KOTLIN
                "getLocation" -> NonExistLocation
                "getParent" -> null
                "findActuals", "findExpects", "getSealedSubclasses", "getAllFunctions", "getAllProperties" -> emptySequence<Any>()
                "asType", "asStarProjectedType", "accept" -> throw UnsupportedOperationException("Not required by this test proxy")
                "isActual", "isExpect" -> false
                "toString" -> "KSClassDeclaration($qualified)"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> defaultValue(method.returnType)
            }
        }

    return Proxy.newProxyInstance(
        KSClassDeclaration::class.java.classLoader,
        arrayOf(KSClassDeclaration::class.java),
        handler,
    ) as KSClassDeclaration
}

private fun ksName(value: String): KSName {
    val qualifier = value.substringBeforeLast('.', "")
    val shortName = value.substringAfterLast('.')

    val handler =
        InvocationHandler { proxy, method, args ->
            when (method.name) {
                "asString" -> value
                "getQualifier" -> qualifier
                "getShortName" -> shortName
                "toString" -> value
                "hashCode" -> value.hashCode()
                "equals" -> (args?.firstOrNull() as? KSName)?.asString() == value
                else -> defaultValue(method.returnType)
            }
        }

    return Proxy.newProxyInstance(
        KSName::class.java.classLoader,
        arrayOf(KSName::class.java),
        handler,
    ) as KSName
}

private fun defaultValue(returnType: Class<*>): Any? =
    when {
        returnType == Boolean::class.javaPrimitiveType -> false
        returnType == Int::class.javaPrimitiveType -> 0
        returnType == Long::class.javaPrimitiveType -> 0L
        returnType == Double::class.javaPrimitiveType -> 0.0
        returnType == Float::class.javaPrimitiveType -> 0f
        returnType == Short::class.javaPrimitiveType -> 0.toShort()
        returnType == Byte::class.javaPrimitiveType -> 0.toByte()
        returnType == Char::class.javaPrimitiveType -> '\u0000'
        Sequence::class.java.isAssignableFrom(returnType) -> emptySequence<Any>()
        List::class.java.isAssignableFrom(returnType) -> emptyList<Any>()
        Set::class.java.isAssignableFrom(returnType) -> emptySet<Any>()
        else -> null
    }
