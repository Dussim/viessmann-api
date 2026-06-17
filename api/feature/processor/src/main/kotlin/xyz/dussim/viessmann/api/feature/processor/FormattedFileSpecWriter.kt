package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.squareup.kotlinpoet.FileSpec
import java.io.StringWriter
import java.nio.charset.StandardCharsets

private val ktLintRuleEngine =
    ThreadLocal.withInitial {
        createKtLintRuleEngine()
    }

private fun createKtLintRuleEngine() =
    KtLintRuleEngine(
        ruleProviders = StandardRuleSetProvider().getRuleProviders(),
    )

fun FileSpec.writeFormattedTo(
    codeGenerator: CodeGenerator,
    dependencies: Dependencies,
) {
    writeTo(codeGenerator, dependencies, format = true)
}

private fun FileSpec.writeTo(
    codeGenerator: CodeGenerator,
    dependencies: Dependencies,
    format: Boolean,
) {
    codeGenerator
        .createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = name,
            extensionName = "kt",
        ).use { output ->
            output.write(renderGenerated(format))
        }
}

internal fun FileSpec.renderGenerated(format: Boolean): ByteArray {
    val source =
        if (format) {
            format()
        } else {
            val writer = StringWriter()
            writeTo(writer)
            writer.toString()
        }
    return source.toByteArray(StandardCharsets.UTF_8)
}

private fun FileSpec.format(): String =
    ktLintRuleEngine.get().format(Code.fromSnippet(toString())) { lintError ->
        if (lintError.canBeAutoCorrected) {
            AutocorrectDecision.ALLOW_AUTOCORRECT
        } else {
            AutocorrectDecision.NO_AUTOCORRECT
        }
    }
