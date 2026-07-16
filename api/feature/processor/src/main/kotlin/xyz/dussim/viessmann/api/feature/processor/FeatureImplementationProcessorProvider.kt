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
        const val VALIDATION_RULES_CHUNK_SIZE_OPTION = "validationRulesChunkSize"
        const val FORMAT_GENERATED_SOURCES_OPTION = "formatGeneratedSources"
        const val RENDER_PARALLELISM_OPTION = "renderParallelism"

        internal const val DEFAULT_DESCRIPTORS_CHUNK_SIZE = 64
        internal const val MAX_DESCRIPTORS_CHUNK_SIZE = 256
        internal const val DEFAULT_VALIDATION_RULES_CHUNK_SIZE = 64
        internal const val MAX_VALIDATION_RULES_CHUNK_SIZE = 128
        internal const val MAX_RENDER_PARALLELISM = 16
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val options =
            FeatureProcessorOptions.from(environment.options) { message ->
                environment.logger.error(message)
            }
        return FeatureImplementationProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            descriptorsChunkSize = options.descriptorsChunkSize,
            validationRulesChunkSize = options.validationRulesChunkSize,
            formatGeneratedSources = options.formatGeneratedSources,
            renderParallelism = options.renderParallelism,
        )
    }
}

internal data class FeatureProcessorOptions(
    /** Maximum number of logical feature descriptors emitted in one generated descriptor source file. */
    val descriptorsChunkSize: Int,
    /** Maximum number of generated validation-rule properties emitted in one source file. */
    val validationRulesChunkSize: Int,
    val formatGeneratedSources: Boolean,
    val renderParallelism: Int,
) {
    companion object {
        fun from(
            options: Map<String, String>,
            reportError: (String) -> Unit = {},
        ): FeatureProcessorOptions {
            val descriptorsChunkSize =
                parseBoundedInt(
                    options = options,
                    name = FeatureImplementationProcessorProvider.DESCRIPTORS_CHUNK_SIZE_OPTION,
                    default = FeatureImplementationProcessorProvider.DEFAULT_DESCRIPTORS_CHUNK_SIZE,
                    maximum = FeatureImplementationProcessorProvider.MAX_DESCRIPTORS_CHUNK_SIZE,
                    reportError = reportError,
                )
            val renderParallelism =
                parseBoundedInt(
                    options = options,
                    name = FeatureImplementationProcessorProvider.RENDER_PARALLELISM_OPTION,
                    default = defaultRenderParallelism(),
                    maximum = FeatureImplementationProcessorProvider.MAX_RENDER_PARALLELISM,
                    reportError = reportError,
                )
            val validationRulesChunkSize =
                parseBoundedInt(
                    options = options,
                    name = FeatureImplementationProcessorProvider.VALIDATION_RULES_CHUNK_SIZE_OPTION,
                    default = FeatureImplementationProcessorProvider.DEFAULT_VALIDATION_RULES_CHUNK_SIZE,
                    maximum = FeatureImplementationProcessorProvider.MAX_VALIDATION_RULES_CHUNK_SIZE,
                    reportError = reportError,
                )
            val formatGeneratedSources =
                options[FeatureImplementationProcessorProvider.FORMAT_GENERATED_SOURCES_OPTION]?.let { value ->
                    value.toBooleanStrictOrNull() ?: run {
                        reportError(
                            "Invalid KSP option '${FeatureImplementationProcessorProvider.FORMAT_GENERATED_SOURCES_OPTION}': " +
                                "expected 'true' or 'false', but was '$value'",
                        )
                        true
                    }
                } ?: true

            return FeatureProcessorOptions(
                descriptorsChunkSize = descriptorsChunkSize,
                validationRulesChunkSize = validationRulesChunkSize,
                formatGeneratedSources = formatGeneratedSources,
                renderParallelism = renderParallelism,
            )
        }

        private fun parseBoundedInt(
            options: Map<String, String>,
            name: String,
            default: Int,
            maximum: Int,
            reportError: (String) -> Unit,
        ): Int {
            val raw = options[name] ?: return default
            val value = raw.toIntOrNull()
            if (value == null || value !in 1..maximum) {
                reportError("Invalid KSP option '$name': expected an integer from 1 to $maximum, but was '$raw'")
                return default
            }
            return value
        }

        private fun defaultRenderParallelism(): Int =
            (Runtime.getRuntime().availableProcessors() - 1)
                .coerceIn(1, FeatureImplementationProcessorProvider.MAX_RENDER_PARALLELISM)
    }
}
