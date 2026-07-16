package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation

class FeatureImplementationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val descriptorsChunkSize: Int,
    private val formatGeneratedSources: Boolean = true,
    private val renderParallelism: Int = 1,
) : SymbolProcessor {
    companion object {
        private val ANNOTATION_NAME = GenerateFeatureImplementation::class.qualifiedName!!
    }

    private val enumValueRegistry = EnumValueRegistry()
    private val contextsByQualifiedName = linkedMapOf<String, SymbolContext>()
    private val ruleRegistriesByPackage = mutableMapOf<String, RuleRegistry>()
    private var hasErrors = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(ANNOTATION_NAME).toList()
        val deferredSymbols = annotatedSymbols.filterNot(KSAnnotated::validate)
        val featureSymbols = annotatedSymbols.filter(KSAnnotated::validate).filterIsInstance<KSClassDeclaration>()
        if (featureSymbols.isEmpty()) return deferredSymbols

        if (!FeatureSymbolValidator(logger).validate(resolver, featureSymbols, enumValueRegistry)) {
            hasErrors = true
            return deferredSymbols
        }

        featureSymbols.forEach { symbol ->
            val packageName = symbol.packageName.asString()
            val registry = ruleRegistriesByPackage.getOrPut(packageName) { RuleRegistry("$packageName.components.rules") }
            contextsByQualifiedName.putIfAbsent(symbol.qualifiedName!!.asString(), SymbolContext(symbol, registry, enumValueRegistry))
        }

        return deferredSymbols
    }

    override fun finish() {
        if (hasErrors || contextsByQualifiedName.isEmpty()) return

        val generatedFiles = GeneratedFeatureFilesBuilder(descriptorsChunkSize).build(contextsByQualifiedName.values.toList())

        GeneratedFileEmitter(
            codeGenerator = codeGenerator,
            formatGeneratedSources = formatGeneratedSources,
            renderParallelism = renderParallelism,
        ).emit(generatedFiles)
    }
}
