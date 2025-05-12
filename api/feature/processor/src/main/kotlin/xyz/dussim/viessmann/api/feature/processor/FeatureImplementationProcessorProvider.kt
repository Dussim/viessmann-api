package xyz.dussim.viessmann.api.feature.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@Suppress("unused")
@AutoService(SymbolProcessorProvider::class)
class FeatureImplementationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = FeatureImplementationProcessor(environment.codeGenerator, environment.logger)
}
