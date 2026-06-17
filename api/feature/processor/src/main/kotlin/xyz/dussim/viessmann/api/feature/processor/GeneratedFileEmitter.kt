package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import java.util.concurrent.Executors

internal data class GeneratedFile(
    val fileSpec: FileSpec,
    val dependencies: Dependencies,
)

private data class RenderedGeneratedFile(
    val packageName: String,
    val fileName: String,
    val dependencies: Dependencies,
    val content: ByteArray,
)

internal class GeneratedFileEmitter(
    private val codeGenerator: CodeGenerator,
    private val formatGeneratedSources: Boolean,
    private val renderParallelism: Int,
) {
    fun emit(files: List<GeneratedFile>) {
        render(files).forEach { file ->
            codeGenerator
                .createNewFile(
                    dependencies = file.dependencies,
                    packageName = file.packageName,
                    fileName = file.fileName,
                    extensionName = "kt",
                ).use { output ->
                    output.write(file.content)
                }
        }
    }

    private fun render(files: List<GeneratedFile>): List<RenderedGeneratedFile> {
        if (files.size < 2 || renderParallelism <= 1) {
            return files.map(::render)
        }

        val executor = Executors.newFixedThreadPool(renderParallelism)
        return try {
            files
                .map { file -> executor.submit<RenderedGeneratedFile> { render(file) } }
                .map { it.get() }
        } finally {
            executor.shutdown()
        }
    }

    private fun render(file: GeneratedFile): RenderedGeneratedFile =
        RenderedGeneratedFile(
            packageName = file.fileSpec.packageName,
            fileName = file.fileSpec.name,
            dependencies = file.dependencies,
            content = file.fileSpec.renderGenerated(formatGeneratedSources),
        )
}
