package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
    fun build(
        ksSymbols: List<KSClassDeclaration>,
        symbols: List<SymbolContext>,
    ): List<GeneratedFile> {
        if (symbols.isEmpty()) return emptyList()

        val generatedFiles = mutableListOf<GeneratedFile>()
        val allDescriptorProperties = mutableListOf<PropertySpec>()
        val allDescriptorNames = mutableListOf<String>()

        symbols
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
                        dependencies = Dependencies(true, *group.map { it.symbol.containingFile!! }.toTypedArray()),
                    )

                group.forEach { context ->
                    val generatedProps = generateFeatureDescriptorAndExtensions(context, implName)
                    allDescriptorProperties += generatedProps
                    allDescriptorNames += generatedProps.first().name
                }
            }

        generatedFiles +=
            descriptorFiles(
                descriptorPackage = symbols.first().implName.packageName,
                descriptorProperties = allDescriptorProperties,
                descriptorNames = allDescriptorNames,
                dependencies = Dependencies(true, *ksSymbols.map { it.containingFile!! }.toTypedArray()),
            )

        symbols
            .flatMap { it.nestedCommands }
            .groupBy { it.signature }
            .forEach { (signature, group) ->
                val firstCommand = group.first()
                val implName = ClassName(firstCommand.parentContext.implName.packageName + ".commands", signature.implName)
                val superInterfaces = group.map { it.superInterface }.distinct()

                generatedFiles +=
                    GeneratedFile(
                        fileSpec = generateCommandImplementation(implName, superInterfaces, firstCommand),
                        dependencies = Dependencies(true, *group.map { it.command.containingFile!! }.toTypedArray()),
                    )
            }

        generatedFiles +=
            GeneratedFile(
                fileSpec = generateValidationRules(symbols.first().ruleRegistry),
                dependencies = Dependencies(true, *ksSymbols.map { it.containingFile!! }.toTypedArray()),
            )

        return generatedFiles
    }

    private fun descriptorFiles(
        descriptorPackage: String,
        descriptorProperties: List<PropertySpec>,
        descriptorNames: List<String>,
        dependencies: Dependencies,
    ): List<GeneratedFile> {
        if (descriptorProperties.isEmpty()) return emptyList()

        val descriptorPropertyChunks = descriptorProperties.chunkedByConfiguredSize()
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
                        .addProperties(chunk)
                        .build(),
                dependencies = dependencies,
            )
        } +
            GeneratedFile(
                fileSpec =
                    FileSpec
                        .builder(descriptorPackage, "Descriptors")
                        .addType(descriptorsObject(descriptorNameChunks))
                        .build(),
                dependencies = dependencies,
            )
    }

    private fun <T> List<T>.chunkedByConfiguredSize(): List<List<T>> =
        if (descriptorsChunkSize > 0) {
            chunked(descriptorsChunkSize)
        } else {
            listOf(this)
        }

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
