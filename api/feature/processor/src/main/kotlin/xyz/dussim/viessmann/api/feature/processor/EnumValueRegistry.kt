package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import java.util.concurrent.ConcurrentHashMap

class EnumValueRegistry {
    private val validationFunctions = ConcurrentHashMap<TypeName, MemberName>()
    private val valueTypes = ConcurrentHashMap<TypeName, TypeName>()

    fun register(
        enumType: TypeName,
        valueType: TypeName,
    ) {
        validationFunctions[enumType] = PROPERTY_VALIDATION_FUNCTIONS.getValue(valueType)
        valueTypes[enumType] = valueType
    }

    fun validationFunction(enumType: TypeName): MemberName = validationFunctions.getValue(enumType)

    fun valueType(enumType: TypeName): TypeName = valueTypes.getValue(enumType)
}
