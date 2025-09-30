@file:OptIn(KspExperimental::class)

package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.CompanionNotImplementingFeatureEnumFactory
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.InvalidFeatureEnumFactoryTypeArgument
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.IsUnsupportedType
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingCompanionObject
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingFeatureEnumAnnotation
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingFeatureEnumFactoryAnnotation
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.MissingPublicCompanionObject
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.NotImplementingCorrectInterface
import xyz.dussim.viessmann.api.feature.processor.SymbolValidationError.NotInterface
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command0
import xyz.dussim.viessmann.feature.api.Command1
import xyz.dussim.viessmann.feature.api.Command2
import xyz.dussim.viessmann.feature.api.Command3
import xyz.dussim.viessmann.feature.api.Command4
import xyz.dussim.viessmann.feature.api.Command5
import xyz.dussim.viessmann.feature.api.Command6
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.UnknownValue
import xyz.dussim.viessmann.feature.api.validation.Valid
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import xyz.dussim.viessmann.feature.api.validation.onError
import xyz.dussim.viessmann.feature.api.validation.transform
import xyz.dussim.viessmann.feature.api.validation.validateAll
import kotlin.reflect.KClass

typealias SymbolRule = ValidationRule<KSClassDeclaration, SymbolErrorMetadata>

class FeatureImplementationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    companion object {
        private val annotationName = GenerateFeatureImplementation::class.qualifiedName!!

        private val propertyToKSClassDeclaration = { property: KSPropertyDeclaration ->
            property.type.resolve().declaration as KSClassDeclaration
        }

        private val isInterface: SymbolRule =
            ValidationRule<KSClassDeclaration, SymbolErrorMetadata> {
                when (it.classKind == ClassKind.INTERFACE) {
                    true -> Valid<SymbolErrorMetadata>()
                    false -> ValidationResult.of<SymbolErrorMetadata>(NotInterface(it))
                }
            }

        private val hasPublicCompanionObject: SymbolRule =
            ValidationRule<KSClassDeclaration, SymbolErrorMetadata> { symbol ->
                when (symbol.declarations.any<KSDeclaration> { it is KSClassDeclaration && it.isCompanionObject && it.isPublic() }) {
                    true -> Valid<SymbolErrorMetadata>()
                    false -> ValidationResult.of<SymbolErrorMetadata>(MissingPublicCompanionObject(symbol))
                }
            }

        private fun isDirectSubtypeOf(declarations: List<KSClassDeclaration>): SymbolRule =
            ValidationRule {
                when (it.superTypes.any { type -> type.resolve().declaration in declarations }) {
                    true -> Valid()
                    false -> ValidationResult.of(NotImplementingCorrectInterface(it))
                }
            }

        private fun isTypeOf(declarations: List<KSClassDeclaration>): SymbolRule =
            ValidationRule {
                when (it in declarations) {
                    true -> Valid()
                    false -> ValidationResult.of(IsUnsupportedType(it))
                }
            }

        private fun isPropertyTypeOf(declarations: List<KSClassDeclaration>) =
            isTypeOf(declarations)
                .transform(propertyToKSClassDeclaration)

        private fun isPropertyDirectSubtypeOf(declarations: List<KSClassDeclaration>) =
            isDirectSubtypeOf(declarations)
                .transform(propertyToKSClassDeclaration)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> =
        context(resolver, logger) {
            val featureDeclarations =
                listOf(
                    Feature.Device::class.declaration,
                    Feature.Gateway::class.declaration,
                    Feature.Geofencing::class.declaration,
                )

            val propertiesSupportedPrimitiveTypes =
                listOf(
                    UnknownValue::class.declaration,
                    BooleanValue::class.declaration,
                    DoubleValue::class.declaration,
                    StringValue::class.declaration,
                    ListDoubleValue::class.declaration,
                    ListStringValue::class.declaration,
                    ListDeviceErrorValue::class.declaration,
                    ListZigbeeDeviceStatusValue::class.declaration,
                    ListRoomActorValue::class.declaration,
                    ListDeviceValue::class.declaration,
                    ObjectOtherRoomConfigurationValue::class.declaration,
                    ScheduleValue::class.declaration,
                )

            val propertiesSupportedSuperTypes =
                listOf(
                    Command0::class.declaration,
                    Command1::class.declaration,
                    Command2::class.declaration,
                    Command3::class.declaration,
                    Command4::class.declaration,
                    Command5::class.declaration,
                    Command6::class.declaration,
                )

            val featureEnumFactory = FeatureEnumFactory::class.declaration

            val propertyValidation =
                ValidationRule.or(
                    isPropertyTypeOf(propertiesSupportedPrimitiveTypes),
                    isPropertyDirectSubtypeOf(propertiesSupportedSuperTypes),
                    { property ->
                        val declaration = property.type.resolve().declaration as? KSClassDeclaration ?: return@or Invalid(IsUnsupportedType(property))

                        declaration.isAnnotationPresent(FeatureEnum::class) || return@or Invalid(MissingFeatureEnumAnnotation(property))

                        val companion =
                            declaration
                                .declarations
                                .filterIsInstance<KSClassDeclaration>()
                                .firstOrNull { it.isCompanionObject } ?: return@or Invalid(MissingCompanionObject(property))

                        companion
                            .getAnnotationsByType(FeatureEnum.Factory::class)
                            .toList()
                            .takeIf { it.isNotEmpty() } ?: return@or Invalid(MissingFeatureEnumFactoryAnnotation(property))

                        val companionImplementsInterface =
                            companion
                                .superTypes
                                .map { it.resolve() }
                                .firstOrNull { superType -> superType.declaration == featureEnumFactory } ?: return@or Invalid(CompanionNotImplementingFeatureEnumFactory(property))

                        val propertyValueType =
                            companionImplementsInterface
                                .arguments
                                .first()
                                .type
                                ?.resolve()
                                ?.declaration as? KSClassDeclaration ?: return@or Invalid(InvalidFeatureEnumFactoryTypeArgument(property))

                        SymbolContext.ENUM_VALUES_TO_VALIDATION_RULE[declaration.toClassName()] = PROPERTY_VALIDATION_FUNCTIONS.getValue(propertyValueType.toClassName())
                        SymbolContext.ENUM_VALUES_TO_TYPE[declaration.toClassName()] = propertyValueType.toClassName()

                        Valid()
                    },
                )

            val symbolValidationRule: SymbolRule =
                ValidationRule.and(
                    isInterface,
                    hasPublicCompanionObject,
                    isDirectSubtypeOf(featureDeclarations),
                    { symbol ->
                        propertyValidation.validateAll(symbol.getDeclaredProperties())
                    },
                )

            val symbols =
                resolver
                    .getSymbolsWithAnnotation(annotationName)
                    .filterIsInstance<KSClassDeclaration>()
                    .toList()
                    .let { symbols ->
                        symbolValidationRule
                            .validateAll(symbols)
                            .onError {
                                logger.error(it.toString())
                                throw IllegalStateException(it.toString())
                            }

                        symbols
                    }.map(::SymbolContext)

            symbols.forEach { context ->
                val symbol = context.symbol
                generateFeatureImplementation(context)
                    .writeTo(
                        codeGenerator,
                        Dependencies(true, symbol.containingFile!!),
                    )
            }

            return emptyList()
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

context(resolver: Resolver)
val KClass<*>.declaration: KSClassDeclaration
    get() {
        val qualifiedName = requireName()
        return requireNotNull(resolver.getClassDeclarationByName(qualifiedName)) {
            "$qualifiedName class not found on classpath. Did you forget to add it as dependency?"
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
