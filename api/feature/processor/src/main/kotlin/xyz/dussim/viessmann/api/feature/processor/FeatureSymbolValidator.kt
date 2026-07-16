@file:OptIn(KspExperimental::class)

package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.CompanionNotImplementingFeatureEnumFactory
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.InvalidFeatureEnumFactoryTypeArgument
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.IsUnsupportedType
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingCompanionObject
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingFeatureEnumAnnotation
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingFeatureEnumFactoryAnnotation
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingPublicCompanionObject
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.NotImplementingCorrectInterface
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.NotInterface
import xyz.dussim.viessmann.feature.api.Command0
import xyz.dussim.viessmann.feature.api.Command1
import xyz.dussim.viessmann.feature.api.Command2
import xyz.dussim.viessmann.feature.api.Command3
import xyz.dussim.viessmann.feature.api.Command4
import xyz.dussim.viessmann.feature.api.Command5
import xyz.dussim.viessmann.feature.api.Command6
import xyz.dussim.viessmann.feature.api.Command7
import xyz.dussim.viessmann.feature.api.Command8
import xyz.dussim.viessmann.feature.api.Constraints
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.OfCommand
import xyz.dussim.viessmann.feature.api.validation.Valid
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import xyz.dussim.viessmann.feature.api.validation.invoke
import xyz.dussim.viessmann.feature.api.validation.onError
import xyz.dussim.viessmann.feature.api.validation.transform
import xyz.dussim.viessmann.feature.api.validation.validateAll
import kotlin.reflect.KClass

typealias SymbolRule = ValidationRule<KSClassDeclaration, SymbolErrorMetadata>

internal class FeatureSymbolValidator(
    private val logger: KSPLogger,
) {
    fun validate(
        resolver: Resolver,
        symbols: List<KSClassDeclaration>,
        enumValueRegistry: EnumValueRegistry,
    ): Boolean {
        val shapeValidSymbols = symbols.filter(::validateFeatureShape)
        val result = rule(resolver, enumValueRegistry).validateAll(shapeValidSymbols)
        result.onError { metadata -> logger.error(metadata.toString(), metadata.symbol) }

        val commandsValid = validateCommandShapes(resolver, shapeValidSymbols)
        return shapeValidSymbols.size == symbols.size && !result.isInvalid && commandsValid
    }

    private fun validateFeatureShape(feature: KSClassDeclaration): Boolean {
        var valid = true
        val displayName = feature.qualifiedName?.asString() ?: feature.simpleName.asString()

        fun error(
            message: String,
            symbol: KSAnnotated = feature,
        ) {
            valid = false
            logger.error("Unsupported feature declaration '$displayName': $message", symbol)
        }

        if (feature.classKind != ClassKind.INTERFACE) {
            error("expected an interface, but found ${feature.classKind.name.lowercase()}")
            return false
        }
        if (!feature.isPublic()) error("the interface must be public")
        if (feature.parentDeclaration != null) error("nested feature interfaces are not supported; declare it at top level")
        if (Modifier.SEALED in feature.modifiers) error("sealed feature interfaces are not supported")
        if (feature.typeParameters.isNotEmpty()) error("generic feature interfaces are not supported")

        val directSupertypes = feature.superTypes.map { it.resolve() }.toList()
        val featureSupertypeCount = directSupertypes.count { it.declaration.qualifiedName?.asString() == Feature::class.requireName() }
        if (directSupertypes.size != 1 || featureSupertypeCount != 1) {
            error("expected exactly one direct superinterface, ${Feature::class.requireName()}, but found ${directSupertypes.joinToString { it.toString() }}")
        }

        feature.getDeclaredProperties().forEach { property ->
            if (property.isMutable) error("mutable property '${property.simpleName.asString()}' is not supported; use val", property)
            if (!property.isPublic()) error("property '${property.simpleName.asString()}' must be public", property)
            if (property.type.resolve().declaration !is KSClassDeclaration) {
                error("property '${property.simpleName.asString()}' must have a concrete, non-type-variable type", property)
            }
        }
        feature
            .getDeclaredFunctions()
            .filter { function -> Modifier.ABSTRACT in function.modifiers }
            .forEach { function -> error("abstract function '${function.simpleName.asString()}' is not supported", function) }

        return valid
    }

    private fun validateCommandShapes(
        resolver: Resolver,
        features: List<KSClassDeclaration>,
    ): Boolean {
        val commands =
            features
                .flatMap { feature -> feature.getDeclaredProperties().toList() }
                .filter { property -> property.type.implementsInterface(OfCommand::class) }
                .mapNotNull { property ->
                    property.type
                        .resolve()
                        .makeNotNullable()
                        .declaration as? KSClassDeclaration
                }.distinctBy { command -> command.qualifiedName?.asString() }

        val constraintsDeclaration = resolver.declaration(Constraints::class)
        return commands.fold(true) { valid, command ->
            validateCommandShape(command, constraintsDeclaration) && valid
        }
    }

    private fun validateCommandShape(
        command: KSClassDeclaration,
        constraintsDeclaration: KSClassDeclaration,
    ): Boolean {
        var valid = true

        fun error(
            message: String,
            symbol: KSAnnotated = command,
        ) {
            valid = false
            logger.error("Invalid command '${command.qualifiedName?.asString() ?: command.simpleName.asString()}': $message", symbol)
        }

        if (command.classKind != ClassKind.INTERFACE) {
            error("commands must be interfaces")
            return false
        }
        if (!command.isPublic()) {
            error("commands must be public")
        }
        if (Modifier.SEALED in command.modifiers) {
            error("sealed command interfaces are not supported")
        }
        if (command.typeParameters.isNotEmpty()) {
            error("generic command interfaces are not supported")
        }

        val directSupertypes = command.superTypes.map { it.resolve() }.toList()
        val commandSupertypes = directSupertypes.mapNotNull { type -> commandArity(type)?.let { arity -> type to arity } }
        if (commandSupertypes.size != 1 || directSupertypes.size != 1) {
            error("must directly extend exactly one Command0 through Command8 interface")
            return false
        }

        val (commandSupertype, arity) = commandSupertypes.single()
        if (commandSupertype.arguments.size != arity) {
            error("Command$arity must declare exactly $arity type argument(s)")
            return false
        }

        val properties = command.getDeclaredProperties().toList()
        if (properties.size != arity) {
            error("Command$arity requires exactly $arity declared semantic constraint property/properties, but found ${properties.size}")
        }

        properties.forEachIndexed { index, property ->
            validateConstraintProperty(
                property = property,
                index = index,
                expectedType =
                    commandSupertype.arguments
                        .getOrNull(index)
                        ?.type
                        ?.resolve(),
                constraintsDeclaration = constraintsDeclaration,
                report = ::error,
            )
        }

        command
            .getDeclaredFunctions()
            .filter { function -> Modifier.ABSTRACT in function.modifiers }
            .forEach { function -> error("abstract functions are not supported", function) }

        return valid
    }

    private fun validateConstraintProperty(
        property: KSPropertyDeclaration,
        index: Int,
        expectedType: KSType?,
        constraintsDeclaration: KSClassDeclaration,
        report: (String, KSAnnotated) -> Unit,
    ) {
        val name = property.simpleName.asString()
        if (name in DEFAULT_CONSTRAINTS) {
            report("property '$name' is reserved for CommandN indexed accessors; declare a semantic property instead", property)
        }
        if (property.isMutable) {
            report("constraint property '$name' must be a val", property)
        }
        if (!property.isPublic()) {
            report("constraint property '$name' must be public", property)
        }

        val type = property.type.resolve()
        if (type.isMarkedNullable) {
            report("constraint property '$name' must not be nullable", property)
            return
        }

        val declaration = type.declaration as? KSClassDeclaration
        if (declaration == null || declaration.toClassName() !in CONSTRAINTS_VALIDATION_FUNCTIONS) {
            report("constraint property '$name' has unsupported type ${type.toTypeName()}", property)
            return
        }

        val actualType =
            declaration
                .superTypes
                .map { it.resolve() }
                .firstOrNull { it.declaration == constraintsDeclaration }
                ?.arguments
                ?.singleOrNull()
                ?.type
                ?.resolve()
        if (expectedType == null || actualType == null || expectedType.toTypeName() != actualType.toTypeName()) {
            report(
                "constraint property '$name' at position ${index + 1} must be Constraints<${expectedType?.toTypeName() ?: "<unresolved>"}>",
                property,
            )
        }
    }

    private fun commandArity(type: KSType): Int? = COMMAND_TYPE_ARITIES[type.declaration.qualifiedName?.asString()]

    private fun rule(
        resolver: Resolver,
        enumValueRegistry: EnumValueRegistry,
    ): SymbolRule {
        val featureDeclarations = FEATURE_SUBTYPES.map { resolver.declaration(it) }
        val propertyValidation =
            ValidationRule<KSPropertyDeclaration, SymbolErrorMetadata> { value ->
                listOf(
                    isPropertyDirectSubtypeOf(PROPERTY_COMMAND_TYPES.map { resolver.declaration(it) }),
                    featureEnumValidation(resolver.declaration(FeatureEnumFactory::class), enumValueRegistry),
                ).fold(
                    isPropertyTypeOf(resolver.supportedPropertyValueDeclarations())(value),
                ) { acc, currentRule ->
                    if (!acc.isInvalid) {
                        return@ValidationRule acc
                    }
                    val next = currentRule(value)
                    if (!next.isInvalid) {
                        return@ValidationRule next
                    }
                    ValidationResult.of(acc, next)
                }
            }

        return ValidationRule { value ->
            listOf(
                HAS_PUBLIC_COMPANION_OBJECT_RULE,
                isDirectSubtypeOf(featureDeclarations),
                ValidationRule { symbol: KSClassDeclaration -> propertyValidation.validateAll(symbol.getDeclaredProperties()) },
            ).fold(
                IS_INTERFACE_RULE(value),
            ) { acc, currentRule ->
                ValidationResult.of(acc, currentRule(value))
            }
        }
    }

    private fun Resolver.supportedPropertyValueDeclarations(): List<KSClassDeclaration> =
        PROPERTY_VALIDATION_FUNCTIONS.keys.mapNotNull { typeName ->
            val className = typeName as? ClassName ?: return@mapNotNull null
            getClassDeclarationByName("${className.packageName}.${className.simpleName}")
        }

    private fun Resolver.declaration(type: KClass<*>): KSClassDeclaration {
        val qualifiedName = type.requireName()
        return requireNotNull(getClassDeclarationByName(qualifiedName)) {
            "$qualifiedName class not found on classpath. Did you forget to add it as dependency?"
        }
    }

    private companion object {
        private val FEATURE_SUBTYPES = listOf(Feature::class)

        private val PROPERTY_COMMAND_TYPES =
            listOf(
                Command0::class,
                Command1::class,
                Command2::class,
                Command3::class,
                Command4::class,
                Command5::class,
                Command6::class,
                Command7::class,
                Command8::class,
            )

        private val COMMAND_TYPE_ARITIES =
            PROPERTY_COMMAND_TYPES
                .mapIndexed { arity, type -> type.requireName() to arity }
                .toMap()

        private val PROPERTY_TO_KS_CLASS_DECLARATION = { property: KSPropertyDeclaration ->
            property.type.resolve().declaration as KSClassDeclaration
        }

        private val IS_INTERFACE_RULE: SymbolRule =
            booleanRule(NotInterface) { it.classKind == ClassKind.INTERFACE }

        private val HAS_PUBLIC_COMPANION_OBJECT_RULE: SymbolRule =
            booleanRule(MissingPublicCompanionObject) { symbol ->
                symbol.declarations.any { it is KSClassDeclaration && it.isCompanionObject && it.isPublic() }
            }

        private fun isDirectSubtypeOf(declarations: List<KSClassDeclaration>): SymbolRule =
            booleanRule(NotImplementingCorrectInterface) { symbol ->
                symbol.superTypes.any { type -> type.resolve().declaration in declarations }
            }

        private fun isTypeOf(declarations: List<KSClassDeclaration>): SymbolRule = booleanRule(IsUnsupportedType) { symbol -> symbol in declarations }

        private fun isPropertyTypeOf(declarations: List<KSClassDeclaration>) = isTypeOf(declarations).transform(PROPERTY_TO_KS_CLASS_DECLARATION)

        private fun isPropertyDirectSubtypeOf(declarations: List<KSClassDeclaration>) = isDirectSubtypeOf(declarations).transform(PROPERTY_TO_KS_CLASS_DECLARATION)
    }
}

sealed interface SymbolValidationError : (KSAnnotated) -> SymbolErrorMetadata {
    data object NotInterface : SymbolValidationError

    data object MissingPublicCompanionObject : SymbolValidationError

    data object NotImplementingCorrectInterface : SymbolValidationError

    data object IsUnsupportedType : SymbolValidationError

    data object MissingFeatureEnumAnnotation : SymbolValidationError

    data object MissingCompanionObject : SymbolValidationError

    data object MissingFeatureEnumFactoryAnnotation : SymbolValidationError

    data object CompanionNotImplementingFeatureEnumFactory : SymbolValidationError

    data object InvalidFeatureEnumFactoryTypeArgument : SymbolValidationError

    override fun invoke(symbol: KSAnnotated) = SymbolErrorMetadata(symbol, this)
}

data class SymbolErrorMetadata(
    val symbol: KSAnnotated,
    val error: SymbolValidationError,
) {
    override fun toString(): String =
        when (error) {
            SymbolValidationError.NotInterface -> "Expected an interface annotated for feature implementation generation"
            SymbolValidationError.MissingPublicCompanionObject -> "Expected the feature interface to declare a public companion object"
            SymbolValidationError.NotImplementingCorrectInterface -> "Expected the declaration to directly extend Feature or Command0 through Command8, as applicable"
            SymbolValidationError.IsUnsupportedType -> "Unsupported feature property type; expected a supported property value, feature enum, or command interface"
            SymbolValidationError.MissingFeatureEnumAnnotation -> "Enum-valued feature property type must be annotated with @FeatureEnum"
            SymbolValidationError.MissingCompanionObject -> "Feature enum must declare a companion object"
            SymbolValidationError.MissingFeatureEnumFactoryAnnotation -> "Feature enum companion object must be annotated with @FeatureEnum.Factory"
            SymbolValidationError.CompanionNotImplementingFeatureEnumFactory -> "Feature enum companion must implement FeatureEnumFactory"
            SymbolValidationError.InvalidFeatureEnumFactoryTypeArgument -> "FeatureEnumFactory must declare supported property-value and matching enum type arguments"
        }
}

fun KClass<*>.requireName() = qualifiedName ?: error("Class $this has no qualified name")

fun KSTypeReference.implementsInterface(interfaceClass: KClass<*>): Boolean {
    val classDeclaration =
        this.resolve().declaration as? KSClassDeclaration
            ?: return false

    val interfaceQualifiedName = interfaceClass.qualifiedName ?: return false

    return classDeclaration
        .getAllSuperTypes()
        .any { superType ->
            superType.declaration.qualifiedName?.asString() == interfaceQualifiedName
        }
}

private fun featureEnumValidation(
    featureEnumFactory: KSClassDeclaration,
    enumValueRegistry: EnumValueRegistry,
): ValidationRule<KSPropertyDeclaration, SymbolErrorMetadata> =
    ValidationRule { property ->
        val declaration =
            property.type.resolve().declaration as? KSClassDeclaration
                ?: return@ValidationRule Invalid(IsUnsupportedType(property))

        if (!declaration.isAnnotationPresent(FeatureEnum::class)) {
            return@ValidationRule Invalid(MissingFeatureEnumAnnotation(property))
        }

        val companion =
            declaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { it.isCompanionObject }
                ?: return@ValidationRule Invalid(MissingCompanionObject(property))

        if (!companion.isAnnotationPresent(FeatureEnum.Factory::class)) {
            return@ValidationRule Invalid(MissingFeatureEnumFactoryAnnotation(property))
        }

        val companionImplementsInterface =
            companion.superTypes
                .map { it.resolve() }
                .firstOrNull { it.declaration == featureEnumFactory }
                ?: return@ValidationRule Invalid(CompanionNotImplementingFeatureEnumFactory(property))

        val propertyValueType =
            companionImplementsInterface.arguments
                .firstOrNull()
                ?.type
                ?.resolve()
                ?.declaration as? KSClassDeclaration
                ?: return@ValidationRule Invalid(InvalidFeatureEnumFactoryTypeArgument(property))

        enumValueRegistry.register(
            enumType = declaration.toClassName(),
            valueType = propertyValueType.toClassName(),
        )

        Valid()
    }
