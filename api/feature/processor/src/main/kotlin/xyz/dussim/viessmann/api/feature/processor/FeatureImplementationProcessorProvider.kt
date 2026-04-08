package xyz.dussim.viessmann.api.feature.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@Suppress("unused")
@AutoService(SymbolProcessorProvider::class)
class FeatureImplementationProcessorProvider : SymbolProcessorProvider {
    companion object {
        const val DESCRIPTORS_CHUNK_SIZE_OPTION = "descriptorsChunkSize"
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val descriptorsChunkSize = environment.options[DESCRIPTORS_CHUNK_SIZE_OPTION]?.toIntOrNull() ?: 600
        return FeatureImplementationProcessor(environment.codeGenerator, environment.logger, descriptorsChunkSize * 2)
    }
}
