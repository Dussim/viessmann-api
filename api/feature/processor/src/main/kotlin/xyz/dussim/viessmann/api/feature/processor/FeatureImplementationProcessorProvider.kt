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
        const val FORMAT_GENERATED_SOURCES_OPTION = "formatGeneratedSources"
        const val RENDER_PARALLELISM_OPTION = "renderParallelism"

        private const val DEFAULT_DESCRIPTORS_CHUNK_SIZE = 600
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val options = FeatureProcessorOptions.from(environment.options)
        return FeatureImplementationProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            descriptorsChunkSize = options.descriptorsChunkSize,
            formatGeneratedSources = options.formatGeneratedSources,
            renderParallelism = options.renderParallelism,
        )
    }

    private data class FeatureProcessorOptions(
        val descriptorsChunkSize: Int,
        val formatGeneratedSources: Boolean,
        val renderParallelism: Int,
    ) {
        companion object {
            fun from(options: Map<String, String>): FeatureProcessorOptions {
                val descriptorsChunkSize =
                    options[DESCRIPTORS_CHUNK_SIZE_OPTION]
                        ?.toIntOrNull()
                        ?: DEFAULT_DESCRIPTORS_CHUNK_SIZE
                val renderParallelism =
                    options[RENDER_PARALLELISM_OPTION]
                        ?.toIntOrNull()
                        ?.coerceAtLeast(1)
                        ?: defaultRenderParallelism()

                return FeatureProcessorOptions(
                    descriptorsChunkSize = descriptorsChunkSize * 2,
                    formatGeneratedSources = options[FORMAT_GENERATED_SOURCES_OPTION]?.toBooleanStrictOrNull() ?: true,
                    renderParallelism = renderParallelism,
                )
            }

            private fun defaultRenderParallelism(): Int {
                val processors =
                    Runtime
                        .getRuntime()
                        .availableProcessors()

                return processors
                    .coerceAtMost(processors - 1)
                    .coerceAtLeast(1)
            }
        }
    }
}
