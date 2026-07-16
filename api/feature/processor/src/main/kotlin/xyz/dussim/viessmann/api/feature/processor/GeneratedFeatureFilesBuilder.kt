package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.Dependencies
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
) {
    fun build(symbols: List<SymbolContext>): List<GeneratedFile> =
        symbols
            .groupBy { it.implName.packageName }
            .toSortedMap()
            .values
            .flatMap(::buildPackage)

    private fun buildPackage(symbols: List<SymbolContext>): List<GeneratedFile> {
        val sortedSymbols = symbols.sortedBy { it.superInterface.canonicalName }
        val generatedFiles = mutableListOf<GeneratedFile>()
        val allDescriptorProperties = mutableListOf<List<PropertySpec>>()
        val allDescriptorNames = mutableListOf<String>()

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
                        dependencies = Dependencies(true),
                    )

                group.forEach { context ->
                    val generatedProps = generateFeatureDescriptorAndExtensions(context, implName)
                    allDescriptorProperties += listOf(generatedProps)
                    allDescriptorNames += generatedProps.first().name
                }
            }

        generatedFiles +=
            descriptorFiles(
                descriptorPackage = sortedSymbols.first().implName.packageName,
                descriptorProperties = allDescriptorProperties,
                descriptorNames = allDescriptorNames,
                dependencies = Dependencies(true),
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
                        dependencies = Dependencies(true),
                    )
            }

        generatedFiles +=
            GeneratedFile(
                fileSpec = generateValidationRules(sortedSymbols.first().ruleRegistry),
                dependencies = Dependencies(true),
            )

        return generatedFiles
    }

    private fun descriptorFiles(
        descriptorPackage: String,
        descriptorProperties: List<List<PropertySpec>>,
        descriptorNames: List<String>,
        dependencies: Dependencies,
    ): List<GeneratedFile> {
        if (descriptorProperties.isEmpty()) return emptyList()

        val descriptorPropertyChunks = descriptorProperties.chunked(descriptorsChunkSize).map { it.flatten() }
        val descriptorNameChunks = descriptorNames.chunkedByConfiguredSize()

        return descriptorPropertyChunks.mapIndexed { index, chunk ->
            val fileName =
                if (descriptorPropertyChunks.size == 1) {
                    "GeneratedDescriptors"
                } else {
                    "GeneratedDescriptors${index + 1}"
                }
            GeneratedFile(
                fileSpec =
                    FileSpec
                        .builder(descriptorPackage, fileName)
                        .addAnnotation(FILE_DEPRECATION_SUPPRESSION)
                        .addProperties(chunk)
                        .build(),
                dependencies = dependencies,
            )
        } +
            GeneratedFile(
                fileSpec =
                    FileSpec
                        .builder(descriptorPackage, "Descriptors")
                        .addAnnotation(FILE_DEPRECATION_SUPPRESSION)
                        .addType(descriptorsObject(descriptorNameChunks))
                        .build(),
                dependencies = dependencies,
            )
    }

    private fun <T> List<T>.chunkedByConfiguredSize(): List<List<T>> = chunked(descriptorsChunkSize)

    private fun descriptorsObject(descriptorNameChunks: List<List<String>>): TypeSpec =
        TypeSpec
            .objectBuilder("Descriptors")
            .addProperty(allDescriptorsProperty(descriptorNameChunks))
            .apply {
                descriptorNameChunks.forEachIndexed { index, nameChunk ->
                    addFunction(descriptorChunkFunction(index + 1, nameChunk))
                }
            }.build()

    private fun allDescriptorsProperty(descriptorNameChunks: List<List<String>>): PropertySpec =
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
                    .beginControlFlow("buildSet")
                    .apply {
                        descriptorNameChunks.forEachIndexed { index, _ ->
                            addStatement("addAll(chunk%L())", index + 1)
                        }
                    }.endControlFlow()
                    .build(),
            ).build()

    private fun descriptorChunkFunction(
        number: Int,
        descriptorNames: List<String>,
    ): FunSpec =
        FunSpec
            .builder("chunk$number")
            .addModifiers(KModifier.PRIVATE)
            .returns(
                FeatureDescriptor::class
                    .asTypeName()
                    .parameterizedBy(STAR)
                    .let { List::class.asTypeName().parameterizedBy(it) },
            ).addCode(
                CodeBlock
                    .builder()
                    .add("return listOf(\n")
                    .apply {
                        descriptorNames.forEach { name ->
                            add("%L,\n", name)
                        }
                    }.add(")\n")
                    .build(),
            ).build()
}
