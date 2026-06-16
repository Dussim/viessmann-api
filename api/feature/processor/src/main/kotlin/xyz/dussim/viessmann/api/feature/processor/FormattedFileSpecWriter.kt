package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.squareup.kotlinpoet.FileSpec
import java.nio.charset.StandardCharsets

private val ktLintRuleEngine =
    KtLintRuleEngine(
        ruleProviders = StandardRuleSetProvider().getRuleProviders(),
    )

fun FileSpec.writeFormattedTo(
    codeGenerator: CodeGenerator,
    dependencies: Dependencies,
) {
    val formattedCode =
        ktLintRuleEngine.format(Code.fromSnippet(toString())) { lintError ->
            if (lintError.canBeAutoCorrected) {
                AutocorrectDecision.ALLOW_AUTOCORRECT
            } else {
                AutocorrectDecision.NO_AUTOCORRECT
            }
        }

    codeGenerator
        .createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = name,
            extensionName = "kt",
        ).use { output ->
            output.write(formattedCode.toByteArray(StandardCharsets.UTF_8))
        }
}
