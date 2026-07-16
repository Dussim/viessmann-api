package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock

fun generateValidationRuleFiles(
    ruleRegistry: RuleRegistry,
    chunkSize: Int,
): List<FileSpec> {
    val chunks =
        ruleRegistry
            .getAllRules()
            .entries
            .sortedBy { it.value.simpleName }
            .chunked(chunkSize)

    return chunks.mapIndexed { index, rules ->
        val fileName = if (chunks.size == 1) "ValidationRules" else "ValidationRules${index + 1}"
        FileSpec
            .builder(ruleRegistry.rulesPackage, fileName)
            .apply {
                rules.forEach { (signature, memberName) ->
                    addProperty(ruleProperty(signature, memberName))
                }
            }.build()
    }
}

private fun ruleProperty(
    signature: RuleSignature,
    memberName: MemberName,
): PropertySpec =
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

private fun Any.toCodeBlock(): CodeBlock =
    when (this) {
        is String -> CodeBlock.of("%S", this)
        is ClassName -> CodeBlock.of("%S", this.canonicalName)
        is MemberName -> CodeBlock.of("%M", this)
        else -> CodeBlock.of("%L", this)
    }
