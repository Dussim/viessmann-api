package xyz.dussim.viessmann.api.feature.processor

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.squareup.kotlinpoet.FileSpec

private val ktLintRuleEngine =
    ThreadLocal.withInitial {
        createKtLintRuleEngine()
    }

private fun createKtLintRuleEngine() =
    KtLintRuleEngine(
        ruleProviders = StandardRuleSetProvider().getRuleProviders(),
    )

internal fun FileSpec.renderGenerated(format: Boolean) =
    when {
        format -> format()
        else -> toString()
    }

private fun FileSpec.format(): String =
    ktLintRuleEngine.get().format(
        code = Code.fromSnippet(toString()),
        rerunAfterAutocorrect = false,
        defaultAutocorrect = true,
        callback = { AutocorrectDecision.ALLOW_AUTOCORRECT },
    )
