package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

data class PropertyModel(
    val name: String,
    val type: TypeName,
)

data class CommandModel(
    val name: String,
    val signature: CommandSignature,
    val isRequired: Boolean = true,
)

data class FeatureModel(
    val featureName: String,
    val className: String,
    val properties: List<PropertyModel>,
    val commands: List<CommandModel>,
    val isDeprecated: Boolean = false,
    val deprecationMessage: String? = null,
)

fun buildFeatureInterface(
    model: FeatureModel,
    packageName: String,
    sharedCommands: Map<CommandSignature, ClassName>,
): FileSpec {
    val typeSpec =
        TypeSpec
            .interfaceBuilder(model.className)
            .addSuperinterface(ClassName("xyz.dussim.viessmann.feature.api", "Feature"))
            .addAnnotation(
                AnnotationSpec
                    .builder(ClassName("xyz.dussim.viessmann.api.feature.annotations", "GenerateFeatureImplementation"))
                    .addMember("%S", model.featureName)
                    .build(),
            )

    if (model.isDeprecated) {
        val message =
            model.deprecationMessage?.replace("\"", "\\\"")
                ?: "This feature is deprecated."
        typeSpec.addAnnotation(
            AnnotationSpec
                .builder(Deprecated::class)
                .addMember("%S", message)
                .build(),
        )
    }

    // Add properties
    for (property in model.properties) {
        typeSpec.addProperty(
            PropertySpec
                .builder(escapeIfKeyword(property.name), property.type)
                .build(),
        )
    }

    // Add commands
    for (command in model.commands) {
        val sharedClassName = sharedCommands[command.signature]
        val propertyName = escapeIfKeyword(command.name)

        if (sharedClassName != null) {
            val commandType = if (command.isRequired) sharedClassName else sharedClassName.copy(nullable = true)
            typeSpec.addProperty(
                PropertySpec
                    .builder(propertyName, commandType)
                    .build(),
            )
        } else {
            val interfaceName = command.name.replaceFirstChar { it.uppercaseChar() }
            val commandInterface =
                CommandInterfaceGenerator.generateCommandInterface(
                    interfaceName,
                    command.signature.name,
                    command.signature.parameters,
                )
            typeSpec.addType(commandInterface)

            val nestedType = ClassName("", model.className, interfaceName)
            val commandType = if (command.isRequired) nestedType else nestedType.copy(nullable = true)
            typeSpec.addProperty(
                PropertySpec
                    .builder(propertyName, commandType)
                    .build(),
            )
        }
    }

    return FileSpec
        .builder(packageName, model.className)
        .addType(
            typeSpec
                .addType(TypeSpec.companionObjectBuilder().build())
                .build(),
        ).build()
}

private val KOTLIN_KEYWORDS =
    setOf(
        "value",
        "data",
        "type",
        "class",
        "object",
        "interface",
        "package",
        "import",
        "fun",
        "val",
        "var",
        "if",
        "else",
        "when",
        "for",
        "while",
        "do",
        "return",
    )

private fun escapeIfKeyword(name: String): String = if (name in KOTLIN_KEYWORDS) "`$name`" else name
