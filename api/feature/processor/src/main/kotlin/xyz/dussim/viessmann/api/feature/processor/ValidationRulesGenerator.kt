package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock

fun generateValidationRules(ruleRegistry: RuleRegistry): FileSpec {
    val fileSpec = FileSpec.builder(ruleRegistry.rulesPackage, "ValidationRules")

    ruleRegistry.getAllRules().forEach { (signature, memberName) ->
        val property =
            PropertySpec
                .builder(memberName.simpleName, signature.targetType)
                .addModifiers(KModifier.INTERNAL)
                .initializer(
                    buildCodeBlock {
                        add("%M(", signature.function)
                        signature.args.forEachIndexed { index, arg ->
                            if (index > 0) add(", ")
                            add("%L", arg.toCodeBlock())
                        }
                        add(")")
                    },
                ).build()
        fileSpec.addProperty(property)
    }

    return fileSpec.build()
}

private fun Any.toCodeBlock(): CodeBlock =
    when (this) {
        is String -> CodeBlock.of("%S", this)
        is ClassName -> CodeBlock.of("%S", this.canonicalName)
        is MemberName -> CodeBlock.of("%M", this)
        else -> CodeBlock.of("%L", this)
    }
