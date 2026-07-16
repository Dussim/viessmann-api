package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import xyz.dussim.viessmann.feature.api.FeatureDescriptor

internal class GeneratedFeatureFilesBuilder(
    private val descriptorsChunkSize: Int,
    private val validationRulesChunkSize: Int,
) {
    private data class GeneratedDescriptor(
        val properties: List<PropertySpec>,
        val name: String,
        val originatingFile: KSFile,
    )

    fun build(symbols: List<SymbolContext>): List<GeneratedFile> =
        symbols
            .groupBy { it.implName.packageName }
            .toSortedMap()
            .values
            .flatMap(::buildPackage)

    private fun buildPackage(symbols: List<SymbolContext>): List<GeneratedFile> {
        val sortedSymbols = symbols.sortedBy { it.superInterface.canonicalName }
        val generatedFiles = mutableListOf<GeneratedFile>()
        val descriptors = mutableListOf<GeneratedDescriptor>()

        sortedSymbols
            .groupBy { it.featureSignature }
            .forEach { (signature, group) ->
                val firstContext = group.first()
                val implName =
                    ClassName(
                        firstContext.implName.packageName + ".implementations",
                        signature.implName,
                    )
                val superInterfaces = group.map { it.superInterface }.distinct()

                generatedFiles +=
                    GeneratedFile(
                        fileSpec = generateSharedFeatureImplementation(implName, superInterfaces, firstContext),
                        dependencies = dependenciesFor(group.map(SymbolContext::originatingFile)),
                    )

                group.forEach { context ->
                    val generatedProps = generateFeatureDescriptorAndExtensions(context, implName)
                    descriptors +=
                        GeneratedDescriptor(
                            properties = generatedProps,
                            name = generatedProps.first().name,
                            originatingFile = context.originatingFile,
                        )
                }
            }

        generatedFiles +=
            descriptorFiles(
                descriptorPackage = sortedSymbols.first().implName.packageName,
                descriptors = descriptors,
            )

        sortedSymbols
            .flatMap { it.nestedCommands }
            .groupBy { it.signature }
            .forEach { (signature, group) ->
                val firstCommand = group.first()
                val implName = ClassName(firstCommand.parentContext.implName.packageName + ".commands", signature.implName)
                val superInterfaces = group.map { it.superInterface }.distinct()

                generatedFiles +=
                    GeneratedFile(
                        fileSpec = generateCommandImplementation(implName, superInterfaces, firstCommand),
                        dependencies = dependenciesFor(group.map { it.parentContext.originatingFile }),
                    )
            }

        generatedFiles +=
            generateValidationRuleFiles(
                ruleRegistry = sortedSymbols.first().ruleRegistry,
                chunkSize = validationRulesChunkSize,
            ).map { fileSpec ->
                GeneratedFile(
                    fileSpec = fileSpec,
                    dependencies = dependenciesFor(sortedSymbols.map(SymbolContext::originatingFile), forceAggregating = true),
                )
            }

        return generatedFiles
    }

    private fun descriptorFiles(
        descriptorPackage: String,
        descriptors: List<GeneratedDescriptor>,
    ): List<GeneratedFile> {
        if (descriptors.isEmpty()) return emptyList()

        val descriptorChunks = descriptors.chunked(descriptorsChunkSize)
        val descriptorNameChunks = descriptorChunks.map { chunk -> chunk.map(GeneratedDescriptor::name) }

        return descriptorChunks.mapIndexed { index, chunk ->
            val fileName =
                if (descriptorChunks.size == 1) {
                    "GeneratedDescriptors"
                } else {
                    "GeneratedDescriptors${index + 1}"
                }
            GeneratedFile(
                fileSpec =
                    FileSpec
                        .builder(descriptorPackage, fileName)
                        .addAnnotation(FILE_DEPRECATION_SUPPRESSION)
                        .addProperties(chunk.flatMap(GeneratedDescriptor::properties))
                        .build(),
                dependencies = dependenciesFor(chunk.map(GeneratedDescriptor::originatingFile)),
            )
        } +
            GeneratedFile(
                fileSpec =
                    FileSpec
                        .builder(descriptorPackage, "Descriptors")
                        .addAnnotation(FILE_DEPRECATION_SUPPRESSION)
                        .addType(descriptorsObject(descriptorNameChunks))
                        .build(),
                dependencies =
                    dependenciesFor(
                        descriptors.map(GeneratedDescriptor::originatingFile),
                        forceAggregating = true,
                    ),
            )
    }

    private fun dependenciesFor(
        files: List<KSFile>,
        forceAggregating: Boolean = false,
    ): Dependencies {
        val distinctFiles = files.distinctBy { it.filePath }
        return Dependencies(
            aggregating = forceAggregating || distinctFiles.size != 1,
            *distinctFiles.toTypedArray(),
        )
    }

    private fun descriptorsObject(descriptorNameChunks: List<List<String>>): TypeSpec =
        TypeSpec
            .objectBuilder("Descriptors")
            .addProperty(
                allDescriptorsProperty(
                    descriptorCount = descriptorNameChunks.sumOf { it.size },
                    numberOfChunks = descriptorNameChunks.size,
                ),
            ).apply {
                descriptorNameChunks.forEachIndexed { index, nameChunk ->
                    addFunction(descriptorChunkFunction(index + 1, nameChunk))
                }
            }.build()

    private fun allDescriptorsProperty(
        descriptorCount: Int,
        numberOfChunks: Int,
    ): PropertySpec =
        PropertySpec
            .builder(
                "all",
                FeatureDescriptor::class
                    .asTypeName()
                    .parameterizedBy(STAR)
                    .let { Set::class.asTypeName().parameterizedBy(it) },
            ).initializer(
                CodeBlock
                    .builder()
                    .beginControlFlow("buildSet(%L)", descriptorCount)
                    .apply {
                        repeat(numberOfChunks) { index ->
                            addStatement("addChunk%L()", index + 1)
                        }
                    }.endControlFlow()
                    .build(),
            ).build()

    private fun descriptorChunkFunction(
        number: Int,
        descriptorNames: List<String>,
    ): FunSpec =
        FunSpec
            .builder("addChunk$number")
            .addModifiers(KModifier.PRIVATE)
            .receiver(
                FeatureDescriptor::class
                    .asTypeName()
                    .parameterizedBy(STAR)
                    .let { ClassName("kotlin.collections", "MutableSet").parameterizedBy(it) },
            ).addCode(
                CodeBlock
                    .builder()
                    .apply {
                        descriptorNames.forEach { name ->
                            addStatement("add(%L)", name)
                        }
                    }.build(),
            ).build()
}
