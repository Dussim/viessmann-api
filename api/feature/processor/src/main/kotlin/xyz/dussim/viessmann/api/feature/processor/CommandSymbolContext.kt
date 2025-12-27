package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

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
    val property: KSClassDeclaration,
) : ConvertibleToPropertySpec,
    ConvertibleToConstraintPropertySpec {
    companion object {
        fun from(property: KSPropertyDeclaration) =
            ConstraintProperty(
                property.simpleName.asString(),
                property.type.resolve().toTypeName(),
                property.type.resolve().declaration as KSClassDeclaration,
            )
    }

    override fun asPropertySpec() =
        PropertySpec
            .builder(name, type)
            .addModifiers(KModifier.OVERRIDE)
            .build()

    override fun asConstraintPropertySpec(index: Int) =
        PropertySpec
            .builder("constraint${index + 1}", type)
            .addModifiers(KModifier.OVERRIDE)
            .getter(
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
    val name = command.simpleName.asString()
    val implName = "${name}Impl"

    val superInterface = command.toClassName()
    val implType = parentContext.implName.nestedClass(implName)

    val constraintsProperties by lazy {
        command
            .getDeclaredProperties()
            .map(ConstraintProperty::from)
            .toList()
    }

    val constraintsPropertiesImpl by lazy {
        constraintsProperties.map(ConstraintProperty::asPropertySpec)
    }

    val inheritedConstraintsProperties by lazy {
        constraintsProperties
            .mapIndexed { index, property -> property.asConstraintPropertySpec(index) }
    }

    val allPropertiesImpl by lazy { inheritedConstraintsProperties + constraintsPropertiesImpl }
}
