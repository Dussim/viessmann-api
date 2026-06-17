package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import xyz.dussim.viessmann.api.feature.annotations.CommandName

/**
 * Marker interface for types that can be converted to constraint property specs.
 */
interface ConvertibleToConstraintPropertySpec {
    fun asConstraintPropertySpec(index: Int): PropertySpec
}

/**
 * Represents a constraint property on a command.
 * Constraints define valid parameter values (e.g., min/max, allowed values).
 */
data class ConstraintProperty(
    val name: String,
    val type: TypeName,
) : ConvertibleToPropertySpec,
    ConvertibleToConstraintPropertySpec {
    companion object {
        fun from(property: KSPropertyDeclaration) =
            ConstraintProperty(
                property.simpleName.asString(),
                property.type.resolve().toTypeName(),
            )
    }

    override fun asPropertySpec() = overrideProperty(name, type)

    override fun asConstraintPropertySpec(index: Int) =
        PropertySpec
            .builder("constraint${index + 1}", type)
            .addModifiers(KModifier.OVERRIDE)
            .addAnnotation(
                AnnotationSpec
                    .builder(ClassName("xyz.dussim.viessmann.feature.api", "CommandIndexedConstraintUsage"))
                    .addMember("replacementProperty = %S", name)
                    .build(),
            ).getter(
                FunSpec
                    .getterBuilder()
                    .addCode("return $name")
                    .build(),
            ).build()
}

/**
 * Context for generating command implementations.
 * Contains information about command constraints and properties.
 *
 * @property parentContext The parent feature context
 * @property command The command interface declaration
 */
data class CommandSymbolContext(
    val parentContext: SymbolContext,
    val command: KSClassDeclaration,
) {
    @OptIn(com.google.devtools.ksp.KspExperimental::class)
    val realName =
        command.getAnnotationsByType(CommandName::class).firstOrNull()?.name
            ?: command.simpleName.asString().replaceFirstChar(Char::lowercaseChar)

    val lowerCaseName = realName

    val superInterface = command.toClassName()

    val constraintsProperties =
        command
            .getDeclaredProperties()
            .filterNot { it.simpleName.asString() in DEFAULT_CONSTRAINTS }
            .map(ConstraintProperty::from)
            .toList()

    val signature =
        CommandSignature(
            name = realName.replaceFirstChar { it.uppercase() },
            parameters = constraintsProperties.map { it.name to it.type },
        )

    val implName = signature.implName
    val implType = ClassName(parentContext.implName.packageName + ".commands", implName)

    val inheritedConstraintsProperties =
        constraintsProperties
            .mapIndexed { index, property -> property.asConstraintPropertySpec(index) }
}
