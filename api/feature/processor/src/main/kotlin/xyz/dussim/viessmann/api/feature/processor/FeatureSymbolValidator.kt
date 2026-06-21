@file:OptIn(KspExperimental::class)

package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
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
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
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
    ) = rule(resolver, enumValueRegistry)
        .validateAll(symbols)
        .onError {
            logger.error(it.toString())
            throw IllegalStateException(it.toString())
        }

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
)

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
