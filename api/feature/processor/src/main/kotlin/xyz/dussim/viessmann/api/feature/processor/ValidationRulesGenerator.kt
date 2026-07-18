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
    val rules = ruleRegistry.getAllRules()
    val chunks =
        rules
            .dependencyOrderedEntries()
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

/**
 * Emits referenced rules before the composite rules that use them. Kotlin rejects a top-level
 * property initializer that reads a property declared later in the same file.
 */
private fun Map<RuleSignature, MemberName>.dependencyOrderedEntries(): List<Map.Entry<RuleSignature, MemberName>> {
    val signatureByMember = entries.associate { (signature, member) -> member to signature }
    val entryBySignature = entries.associateBy(Map.Entry<RuleSignature, MemberName>::key)
    val visited = mutableSetOf<RuleSignature>()
    val visiting = mutableSetOf<RuleSignature>()
    val ordered = mutableListOf<Map.Entry<RuleSignature, MemberName>>()

    fun visit(signature: RuleSignature) {
        if (signature in visited) return
        check(visiting.add(signature)) { "Cyclic generated validation-rule dependency: ${signature.generateName()}" }

        signature.args
            .filterIsInstance<MemberName>()
            .mapNotNull(signatureByMember::get)
            .sortedBy(RuleSignature::generateName)
            .forEach(::visit)

        visiting.remove(signature)
        visited.add(signature)
        ordered.add(entryBySignature.getValue(signature))
    }

    entries
        .sortedBy { it.value.simpleName }
        .forEach { visit(it.key) }

    return ordered
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
