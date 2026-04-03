@file:OptIn(KspExperimental::class)

package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
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
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
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
import xyz.dussim.viessmann.feature.api.EnergyMatrixValue
import xyz.dussim.viessmann.feature.api.FactoryResetInfoValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.ListBusTypeValue
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceInformationValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListEebusDeviceValue
import xyz.dussim.viessmann.feature.api.ListEebusServicePartnerValue
import xyz.dussim.viessmann.feature.api.ListElectricalEnergyMatrixValue
import xyz.dussim.viessmann.feature.api.ListEnergyChargedDeviceValue
import xyz.dussim.viessmann.feature.api.ListFuelCellErrorValue
import xyz.dussim.viessmann.feature.api.ListLogBookEntryValue
import xyz.dussim.viessmann.feature.api.ListOperatingDataCellsDetailValue
import xyz.dussim.viessmann.feature.api.ListPowerBalanceEntryValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListSensorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListVentilationMessageValue
import xyz.dussim.viessmann.feature.api.ListWifiNetworkValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.LogsValue
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.ProductInfoValue
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.UnknownValue
import xyz.dussim.viessmann.feature.api.validation.Valid
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
        private val ANNOTATION_NAME = GenerateFeatureImplementation::class.qualifiedName!!

        private val FEATURE_SUBTYPES =
            listOf(
                Feature.Device::class,
                Feature.Gateway::class,
                Feature.Geofencing::class,
            )

        private val PROPERTY_COMMAND_TYPES =
            listOf(
                Command0::class,
                Command1::class,
                Command2::class,
                Command3::class,
                Command4::class,
                Command5::class,
                Command6::class,
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

        private fun isTypeOf(declarations: List<KSClassDeclaration>): SymbolRule =
            booleanRule(IsUnsupportedType) { symbol ->
                symbol in declarations
            }

        private fun isPropertyTypeOf(declarations: List<KSClassDeclaration>) =
            isTypeOf(declarations)
                .transform(PROPERTY_TO_KS_CLASS_DECLARATION)

        private fun isPropertyDirectSubtypeOf(declarations: List<KSClassDeclaration>) =
            isDirectSubtypeOf(declarations)
                .transform(PROPERTY_TO_KS_CLASS_DECLARATION)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        fun KClass<*>.declaration(): KSClassDeclaration {
            val qualifiedName = requireName()
            return requireNotNull(resolver.getClassDeclarationByName(qualifiedName)) {
                "$qualifiedName class not found on classpath. Did you forget to add it as dependency?"
            }
        }
        return run {
            val featureDeclarations = FEATURE_SUBTYPES.map { it.declaration() }
            val propertiesSupportedPrimitiveTypes =
                PROPERTY_VALIDATION_FUNCTIONS.keys
                    .mapNotNull { typeName ->
                        val className = typeName as? ClassName ?: return@mapNotNull null
                        val fqName = "${className.packageName}.${className.simpleName}"
                        resolver.getClassDeclarationByName(fqName)
                    }
            val propertiesSupportedSuperTypes = PROPERTY_COMMAND_TYPES.map { it.declaration() }

            val featureEnumFactory = FeatureEnumFactory::class.declaration()

            val propertyValidation =
                ValidationRule.or(
                    isPropertyTypeOf(propertiesSupportedPrimitiveTypes),
                    isPropertyDirectSubtypeOf(propertiesSupportedSuperTypes),
                    featureEnumValidation(featureEnumFactory),
                )

            val symbolValidationRule: SymbolRule =
                ValidationRule.and(
                    IS_INTERFACE_RULE,
                    HAS_PUBLIC_COMPANION_OBJECT_RULE,
                    isDirectSubtypeOf(featureDeclarations),
                    { symbol ->
                        propertyValidation.validateAll(symbol.getDeclaredProperties())
                    },
                )

            val symbols =
                resolver
                    .getSymbolsWithAnnotation(ANNOTATION_NAME)
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
                    }.let { ksSymbols ->
                        if (ksSymbols.isEmpty()) return emptyList()

                        val basePackage = ksSymbols.first().packageName.asString()
                        val rulesPackage = "$basePackage.components.rules"
                        val ruleRegistry = RuleRegistry(rulesPackage)

                        val symbols = ksSymbols.map { SymbolContext(it, ruleRegistry) }

                        // Group features by signature for deduplication
                        symbols.groupBy { it.featureSignature }.forEach { (signature, group) ->
                            val firstContext = group.first()
                            val implName =
                                ClassName(
                                    firstContext.implName.packageName + ".implementations",
                                    signature.implName,
                                )
                            val superInterfaces = group.map { it.superInterface }.distinct()

                            // Generate shared implementation
                            generateSharedFeatureImplementation(implName, superInterfaces, firstContext)
                                .writeTo(
                                    codeGenerator,
                                    Dependencies(true, *group.map { it.symbol.containingFile!! }.toTypedArray()),
                                )

                            // Generate descriptor and extensions for each feature in the group
                            group.forEach { context ->
                                generateFeatureDescriptorAndExtensions(context, implName)
                                    .writeTo(
                                        codeGenerator,
                                        Dependencies(true, context.symbol.containingFile!!),
                                    )
                            }
                        }

                        symbols
                            .flatMap { it.nestedCommands }
                            .groupBy { it.signature }
                            .forEach { (signature, group) ->
                                val firstCommand = group.first()
                                val implName = ClassName(firstCommand.parentContext.implName.packageName + ".commands", signature.implName)
                                val superInterfaces = group.map { it.superInterface }.distinct()

                                generateCommandImplementation(implName, superInterfaces, firstCommand)
                                    .writeTo(
                                        codeGenerator,
                                        Dependencies(true, *group.map { it.command.containingFile!! }.toTypedArray()),
                                    )
                            }

                        generateValidationRules(ruleRegistry)
                            .writeTo(
                                codeGenerator,
                                Dependencies(true, *ksSymbols.map { it.containingFile!! }.toTypedArray()),
                            )

                        symbols
                    }

            return emptyList()
        }
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

private fun featureEnumValidation(featureEnumFactory: KSClassDeclaration): ValidationRule<KSPropertyDeclaration, SymbolErrorMetadata> =
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

        SymbolContext.ENUM_VALUES_TO_VALIDATION_RULE[declaration.toClassName()] =
            PROPERTY_VALIDATION_FUNCTIONS.getValue(propertyValueType.toClassName())
        SymbolContext.ENUM_VALUES_TO_TYPE[declaration.toClassName()] =
            propertyValueType.toClassName()

        Valid()
    }
