package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.ClassKind.INTERFACE
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation

// NOTES for myself
// if you already have the KClass, then you can get the KSClassDeclaration by resolver.getClassDeclarationByName(KClass.qualifiedName)

val CLASS_TEMPLATE =
    """
    package {PACKAGE}
    
    import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleMap
    
    internal class {CLASS}Impl(
        feature: ViessmannFeature.{TYPE}
    ) : {CLASS},
        ViessmannFeature.{TYPE} by feature {
    {PROPERTIES}{COMMANDS}
    }
    """.trimIndent()

const val COMMAND_TEMPLATE = """
    internal class {CLASS}Impl(
        private val delegate: {COMMAND}
    ) : {OUTER}{CLASS},
        {COMMAND} by delegate {
{CONSTRAINTS}
    }"""

val FACTORY_TEMPLATE =
    """
    internal data object {CLASS}Factory : {TYPE}FeatureFactory<{CLASS}> {
        override val featureName = {FEATURE}
    
        override operator fun invoke(feature: ViessmannFeature.{TYPE}) = {CLASS}Impl(feature)
    }
    """.trimIndent()

val COMPANION_EXTENSION_TEMPLATE =
    """
    val {CLASS}.Companion.factory: {TYPE}FeatureFactory<{CLASS}> get() = {CLASS}Factory
    """.trimIndent()

val COMPANION_EXTENSION_TEMPLATE_2 =
    """
    operator fun {CLASS}.Companion.invoke(feature: ViessmannFeature.{TYPE}): {CLASS} = factory(feature)
    """.trimIndent()

val SUPPORTED_PROPERTIES =
    listOf(
        "kotlin.Int",
        "kotlin.Float",
        "kotlin.Long",
        "kotlin.Double",
        "kotlin.String",
        "kotlin.collections.List<String>",
        "kotlin.collections.List<xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.DeviceError>",
        "kotlin.collections.List<xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.ZigbeeDeviceStatus>",
        "kotlin.collections.List<xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.DeviceListProperty.Device>",
    )

class FeatureImplementationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val problems = mutableMapOf<KSClassDeclaration, List<String>>()

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotated =
            resolver
                .getSymbolsWithAnnotation(GenerateFeatureImplementation::class.qualifiedName!!)
                .mapNotNull { symbol ->
                    (symbol as KSClassDeclaration).takeIf { it.isInterface() and it.isSubclassOfViessmannFeature() and it.hasPublicCompanionObject() }
                }.map { it.mapToResolvedInfo() }
                .toList()

        logErrors()

        annotated.forEach { (symbol, superType, declaredCommands, properties) ->
            val file =
                codeGenerator.createNewFile(
                    dependencies = Dependencies(false, symbol.containingFile!!),
                    packageName = symbol.packageName.asString(),
                    fileName = symbol.simpleName.asString() + "Impl",
                )

            resolver.getClassDeclarationByName<Int>()

            val annotation =
                symbol
                    .getAnnotationsByType(GenerateFeatureImplementation::class)
                    .first()

            val properties =
                properties
                    .joinToString("\n") { property -> "    override val ${property.name} by ${property.delegate}" }

            val commandBodies =
                declaredCommands.map { (commandSymbol, commandSuperType, genericTypes) ->
                    val properties =
                        commandSymbol
                            .getDeclaredProperties()
                            .take(genericTypes.size)
                            .mapIndexed { i, property -> "        override val ${property.simpleName.asString()} = constraint${i + 1}" }
                            .joinToString("\n")

                    COMMAND_TEMPLATE
                        .replace("{CLASS}", commandSymbol.simpleName.asString())
                        .replace("{OUTER}", symbol.simpleName.asString() + ".")
                        .replace("{COMMAND}", commandSuperType.toString())
                        .replace("{CONSTRAINTS}", properties)
                }

            val factoryBody =
                FACTORY_TEMPLATE
                    .replace("{CLASS}", symbol.simpleName.asString())
                    .replace("{FEATURE}", "\"${annotation.featureName}\"")
                    .replace("{TYPE}", superType.simpleName.asString())

            val companionExtensionBody =
                COMPANION_EXTENSION_TEMPLATE
                    .replace("{CLASS}", symbol.simpleName.asString())
                    .replace("{TYPE}", superType.simpleName.asString())
                    .replace("{FEATURE}", "\"${annotation.featureName}\"")

            val companionExtensionBody2 =
                COMPANION_EXTENSION_TEMPLATE_2
                    .replace("{CLASS}", symbol.simpleName.asString())
                    .replace("{TYPE}", superType.simpleName.asString())
                    .replace("{FEATURE}", "\"${annotation.featureName}\"")

            val classBody =
                CLASS_TEMPLATE
                    .replace("{PACKAGE}", symbol.packageName.asString())
                    .replace("{CLASS}", symbol.simpleName.asString())
                    .replace("{TYPE}", superType.simpleName.asString())
                    .replace("{PROPERTIES}", properties)
                    .run {
                        if (commandBodies.isNotEmpty()) {
                            replace("{COMMANDS}", commandBodies.joinToString("\n", prefix = "\n"))
                        } else {
                            replace("{COMMANDS}", "")
                        }
                    }

            file.writer().use { writer ->
                writer.write(classBody)
                writer.write("\n\n$factoryBody")
                writer.write("\n\n$companionExtensionBody")
                writer.write("\n\n$companionExtensionBody2")
                writer.flush()
            }
        }

        return emptyList()
    }

    private fun KSClassDeclaration.isInterface(): Boolean =
        when (classKind == INTERFACE) {
            true -> true
            false -> {
                problems.compute(this) { _, prev ->
                    prev.orEmpty().plus("Annotated symbol must be an interface")
                }
                false
            }
        }

    private fun KSClassDeclaration.isSubclassOfViessmannFeature(): Boolean {
        val allowedSuperTypes =
            listOf(
                "xyz.dussim.viessmann.api.features.ViessmannFeature.Device",
                "xyz.dussim.viessmann.api.features.ViessmannFeature.Gateway",
                "xyz.dussim.viessmann.api.features.ViessmannFeature.Geofencing",
            )

        val directlyImplementsFeature =
            superTypes.any { superType ->
                val superTypeDeclaration = superType.resolve().declaration

                superTypeDeclaration.qualifiedName?.asString() in allowedSuperTypes
            }

        if (directlyImplementsFeature) {
            return true
        } else {
            problems.compute(this) { _, prev ->
                prev.orEmpty().plus(
                    "Annotated symbol must directly implement one of the following interfaces: $allowedSuperTypes",
                )
            }
            return false
        }
    }

    private fun KSClassDeclaration.hasPublicCompanionObject(): Boolean =
        when (declarations.any { it is KSClassDeclaration && it.isCompanionObject && it.isPublic() }) {
            true -> true
            else -> {
                problems.compute(this) { _, prev ->
                    prev.orEmpty().plus("Annotated symbol must declare a public companion object")
                }
                false
            }
        }

    private fun logErrors() {
        if (problems.isNotEmpty()) {
            val errorMessage =
                buildString {
                    appendLine("Processor encountered problems when processing the annotated symbols:\n")
                    problems.entries.forEachIndexed { i, (symbol, messages) ->
                        appendLine("For symbol: ${symbol.qualifiedName?.asString()}")
                        messages.forEach { message ->
                            appendLine(" - $message")
                        }
                        if (i != problems.size - 1) {
                            appendLine("")
                        }
                    }
                    appendLine("")
                }

            logger.error(errorMessage)
        }
    }

    private fun KSClassDeclaration.mapToResolvedInfo(): ResolvedInfo {
        val allowedSuperTypes =
            listOf(
                "xyz.dussim.viessmann.api.features.ViessmannFeature.Device",
                "xyz.dussim.viessmann.api.features.ViessmannFeature.Gateway",
                "xyz.dussim.viessmann.api.features.ViessmannFeature.Geofencing",
            )

        val superType =
            superTypes.firstNotNullOf { superType ->
                superType.resolve().declaration.takeIf {
                    it is KSClassDeclaration && it.qualifiedName?.asString() in allowedSuperTypes
                } as? KSClassDeclaration
            }

        return ResolvedInfo(
            symbol = this,
            superType = superType,
            declaredCommands =
                declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .mapNotNull { it.asStarProjectedType().isSubclassOfCommand(this) }
                    .toList(),
            propertiesToImplement =
                getDeclaredProperties()
                    .mapNotNull { property ->
                        Property(
                            name = property.simpleName.asString(),
                            propertyType =
                                when (
                                    val typeName =
                                        property.type
                                            .resolve()
                                            .declaration.qualifiedName
                                            ?.asString()
                                ) {
                                    "kotlin.Boolean" -> PropertyType.BOOLEAN
                                    "kotlin.String" -> PropertyType.STRING
                                    "kotlin.Int" -> PropertyType.INT
                                    "kotlin.Float" -> PropertyType.FLOAT
                                    "kotlin.Long" -> PropertyType.LONG
                                    "kotlin.Double" -> PropertyType.DOUBLE
                                    "kotlin.Nothing" -> PropertyType.NOTHING
                                    "xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleMap" -> PropertyType.SCHEDULE
                                    "kotlin.collections.List" -> {
                                        val type = property.findListActualType()
                                        when (val listType = type.qualifiedName?.asString()) {
                                            "xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.DeviceError" -> PropertyType.LIST_DEVICE_ERRORS
                                            "xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.ZigbeeDeviceStatus" -> PropertyType.LIST_ZIGBEE_DEVICE_STATUS
                                            "xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.DeviceListProperty.Device" -> PropertyType.LIST_DEVICE_LIST
                                            "kotlin.String" -> PropertyType.LIST_STRING
                                            else -> {
                                                problems.compute(this) { _, prev ->
                                                    prev.orEmpty().plus(
                                                        "Property '$property' of type '$typeName<$listType>' is not supported.  Supported types are $SUPPORTED_PROPERTIES",
                                                    )
                                                }
                                                return@mapNotNull null
                                            }
                                        }
                                    }

                                    else ->
                                        when (val command = property.hasTypeOfCommand(this)) {
                                            null ->
                                                when (property.isSubClassOfFeatureEnum(this)) {
                                                    true ->
                                                        PropertyType.ENUM(
                                                            "$this.${property.type}",
                                                        )

                                                    false -> {
                                                        problems.compute(this) { _, prev ->
                                                            prev.orEmpty().plus("Property '$property' of type '$typeName' is not supported.")
                                                        }
                                                        return@mapNotNull null
                                                    }
                                                }

                                            else ->
                                                when (command.isCommand0) {
                                                    true -> PropertyType.EMPTY_COMMAND
                                                    false ->
                                                        PropertyType.COMMAND(
                                                            type = command.name.plus("Impl"),
                                                            properties = command.constraints.joinToString(", ") { it.delegate },
                                                        )
                                                }
                                        }
                                },
                        )
                    }.toList(),
        )
    }

    private fun KSPropertyDeclaration.hasTypeOfCommand(symbol: KSClassDeclaration): DeclaredCommand? = type.resolve().isSubclassOfCommand(symbol, this)

    private fun KSType.isSubclassOfCommand(
        symbol: KSClassDeclaration,
        property: KSPropertyDeclaration? = null,
    ): DeclaredCommand? {
        val declaration = declaration as? KSClassDeclaration ?: return null

        val allowedSuperTypes =
            listOf(
                "xyz.dussim.viessmann.api.features.Command0",
                "xyz.dussim.viessmann.api.features.Command1",
                "xyz.dussim.viessmann.api.features.Command2",
                "xyz.dussim.viessmann.api.features.Command3",
                "xyz.dussim.viessmann.api.features.Command4",
                "xyz.dussim.viessmann.api.features.Command5",
                "xyz.dussim.viessmann.api.features.Command6",
            )

        val directTypeException =
            listOf(
                "xyz.dussim.viessmann.api.features.Command0",
            )

        if (declaration.qualifiedName?.asString() in allowedSuperTypes.minus(directTypeException)) {
            problems.compute(symbol) { _, prev ->
                prev
                    .orEmpty()
                    .plus(
                        "Property '$property' of type '$declaration' is not allowed directly. The type needs to be interface that declares properties of type 'ViessmannFeatureCommandParamConstraints<T>' as they provide naming for constraints and the interface need to implement directly one of $allowedSuperTypes",
                    )
            }
            return null
        }

        val superType =
            takeIf {
                declaration.qualifiedName?.asString() in directTypeException
            } ?: declaration.superTypes.firstNotNullOfOrNull { superType ->
                superType.resolve().takeIf {
                    it.declaration is KSClassDeclaration && it.declaration.qualifiedName?.asString() in allowedSuperTypes
                }
            }

        return superType?.let {
            DeclaredCommand(
                symbol = declaration,
                superType = it,
                genericTypes = it.arguments,
                constraints =
                    declaration
                        .getDeclaredProperties()
                        .mapIndexed { i, property ->
                            val constraintType =
                                when (
                                    val typeName =
                                        property.type
                                            .resolve()
                                            .arguments
                                            .first()
                                            .type
                                            ?.resolve()
                                            ?.declaration
                                            ?.qualifiedName
                                            ?.asString()
                                ) {
                                    "kotlin.Boolean" -> ConstraintType.BOOLEAN
                                    "kotlin.String" -> ConstraintType.STRING
                                    "kotlin.Double" -> ConstraintType.DOUBLE
                                    "xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleMap" -> ConstraintType.SCHEDULE
                                    else -> error("Unsupported constraint type: $typeName")
                                }

                            Constraint(
                                name = property.simpleName.asString(),
                                constraintType = constraintType,
                            )
                        }.toList(),
            )
        }
    }

    fun KSPropertyDeclaration.findListActualType(): KSClassDeclaration {
        val resolvedType = type.resolve()
        val genericType =
            resolvedType.arguments
                .first()
                .type!!
                .resolve()

        return when (val declaration = genericType.declaration) {
            is KSTypeAlias -> declaration.findActualType()
            else -> declaration as KSClassDeclaration
        }
    }

    fun KSPropertyDeclaration.isSubClassOfFeatureEnum(symbol: KSClassDeclaration): Boolean {
        val type = type.resolve()
        val declaration = type.declaration as? KSClassDeclaration ?: return false

        val allowedSuperTypes =
            listOf(
                "xyz.dussim.viessmann.api.features.FeatureEnum",
            )

        return declaration.superTypes
            .any {
                val superTypeDeclaration = it.resolve().declaration

                superTypeDeclaration.qualifiedName?.asString() in allowedSuperTypes &&
                    superTypeDeclaration is KSClassDeclaration &&
                    declaration.classKind == ClassKind.ENUM_CLASS
            }.also {
                if (!it) {
                    problems.compute(symbol) { _, prev ->
                        prev.orEmpty().plus(
                            listOf(
                                "Property can be concrete enum which implements $allowedSuperTypes otherwise see next message",
                                "Property '$this' of type '$type' is not supported. Supported types are $SUPPORTED_PROPERTIES",
                            ),
                        )
                    }
                }
            }
    }
}

sealed class PropertyType(
    open val type: String,
    open val delegate: String,
) {
    data object STRING : PropertyType("String", "string()")

    data object INT : PropertyType("Int", "int()")

    data object FLOAT : PropertyType("Float", "float()")

    data object LONG : PropertyType("Long", "long()")

    data object DOUBLE : PropertyType("Double", "double()")

    data object BOOLEAN : PropertyType("Boolean", "boolean()")

    data object NOTHING : PropertyType("Nothing", "lazy { TODO(\"Not yet implemented\") }")

    data object LIST_DEVICE_ERRORS : PropertyType("List<DeviceError>", "deviceErrors()")

    data object LIST_DEVICE_LIST : PropertyType("List<DeviceInfo>", "deviceList()")

    data object LIST_ZIGBEE_DEVICE_STATUS : PropertyType("List<ZigbeeDeviceStatus>", "zigbeeDeviceStatuses()")

    data object LIST_STRING : PropertyType("List<String>", "strings()")

    data object EMPTY_COMMAND : PropertyType("", "command()")

    data object SCHEDULE : PropertyType("", "schedule()")

    data class COMMAND(
        override val type: String,
        val properties: String,
    ) : PropertyType(type, "command(::$type, $properties)")

    data class ENUM(
        override val type: String,
    ) : PropertyType(type, "enum<$type>()")
}

sealed class ConstraintType(
    open val type: String,
    open val delegate: (String) -> String,
) {
    data object STRING : ConstraintType("String", { "stringConstraints(\"$it\")" })

    data object DOUBLE : ConstraintType("Double", { "numberConstraints(\"$it\")" })

    data object BOOLEAN : ConstraintType("Boolean", { "booleanConstraints(\"$it\")" })

    data object SCHEDULE : ConstraintType("ScheduleMap", { "scheduleConstraints(\"$it\")" })
}

data class Property(
    val name: String,
    val propertyType: PropertyType,
) {
    val type = propertyType.type
    val delegate = propertyType.delegate
}

data class Constraint(
    val name: String,
    val constraintType: ConstraintType,
) {
    val type = constraintType.type
    val delegate = constraintType.delegate(name)
}

data class DeclaredCommand(
    val symbol: KSClassDeclaration,
    val superType: KSType,
    val genericTypes: List<KSTypeArgument>,
    val constraints: List<Constraint>,
) {
    val isCommand0 = genericTypes.isEmpty()
    val name = symbol.simpleName.asString()
}

data class ResolvedInfo(
    val symbol: KSClassDeclaration,
    val superType: KSClassDeclaration,
    val declaredCommands: List<DeclaredCommand>,
    val propertiesToImplement: List<Property>,
)
