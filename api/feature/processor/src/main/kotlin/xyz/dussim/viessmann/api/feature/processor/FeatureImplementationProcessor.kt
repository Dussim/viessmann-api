package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val enumValueRegistry = EnumValueRegistry()
        val ksSymbols = resolver.annotatedFeatureSymbols()
        if (ksSymbols.isEmpty()) return emptyList()

        FeatureSymbolValidator(logger).validate(resolver, ksSymbols, enumValueRegistry)

        val ruleRegistry = RuleRegistry("${ksSymbols.first().packageName.asString()}.components.rules")
        val symbolContexts = ksSymbols.map { SymbolContext(it, ruleRegistry, enumValueRegistry) }
        val generatedFiles = GeneratedFeatureFilesBuilder(descriptorsChunkSize).build(ksSymbols, symbolContexts)

        GeneratedFileEmitter(
            codeGenerator = codeGenerator,
            formatGeneratedSources = formatGeneratedSources,
            renderParallelism = renderParallelism,
        ).emit(generatedFiles)

        return emptyList()
    }

    private fun Resolver.annotatedFeatureSymbols(): List<KSClassDeclaration> =
        getSymbolsWithAnnotation(ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
}
