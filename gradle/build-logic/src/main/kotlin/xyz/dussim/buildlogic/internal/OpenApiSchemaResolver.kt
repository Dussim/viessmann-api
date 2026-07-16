package xyz.dussim.buildlogic.internal

import io.swagger.v3.oas.models.media.Schema

internal object OpenApiSchemaResolver {
    fun resolve(schema: Schema<*>): Schema<*> {
        val compositionParts = schema.allOf ?: schema.oneOf ?: schema.anyOf
        if (compositionParts != null) {
            return compositionParts
                .map(::resolve)
                .fold(Schema<Any>()) { acc, next -> merge(acc, next) }
        }

        val result = Schema<Any>()
        var hasContent = false

        schema.type?.let {
            result.type = it
            hasContent = true
        }
        schema.const?.let {
            result.const = it
            hasContent = true
        }
        schema.example?.let {
            result.example = it
            hasContent = true
        }
        schema.nullable?.let {
            result.nullable = it
            hasContent = true
        }

        @Suppress("UNCHECKED_CAST")
        (schema.enum as? MutableList<Any>)?.let {
            result.enum = it.toMutableList()
            hasContent = true
        }

        schema.properties?.let { props ->
            val mergedProps = mutableMapOf<String, Schema<*>>()
            for ((name, prop) in props) {
                val resolved = resolve(prop)
                val existing = mergedProps[name]
                mergedProps[name] = if (existing != null) merge(existing, resolved) else resolved
            }
            result.properties = mergedProps
            if (result.type == null) result.type = "object"
            hasContent = true
        }

        schema.required?.let {
            result.required = it
            hasContent = true
        }

        schema.items?.let { items ->
            result.items = resolve(items)
            if (result.type == null) result.type = "array"
            hasContent = true
        }

        val additional = schema.additionalProperties
        if (additional is Schema<*>) {
            @Suppress("UNCHECKED_CAST")
            result.additionalProperties = resolve(additional) as Schema<Any>
            if (result.type == null) result.type = "object"
            hasContent = true
        } else if (additional != null) {
            result.additionalProperties = additional
            hasContent = true
        }

        return if (hasContent) result else schema
    }

    fun merge(
        a: Schema<*>,
        b: Schema<*>,
    ): Schema<Any> {
        val left = resolve(a)
        val right = resolve(b)
        val merged = Schema<Any>()

        merged.type = mergeTypes(left.type, right.type)
        mergeNullable(left.nullable, right.nullable)?.let { merged.nullable = it }
        mergeProperties(left, right)?.let { merged.properties = it }
        mergeEnums(left, right)?.let { merged.enum = it }
        merged.example = mergeExample(left.example, right.example)
        mergeConst(left.const, right.const)?.let { merged.const = it }
        mergeItems(left.items, right.items)?.let { merged.items = it }
        mergeRequired(left.required, right.required)?.let { merged.required = it }
        mergeAdditionalProperties(left.additionalProperties, right.additionalProperties)?.let {
            merged.additionalProperties = it
        }

        if (merged.type == null) {
            when {
                merged.properties != null -> merged.type = "object"
                merged.items != null -> merged.type = "array"
                merged.additionalProperties is Schema<*> -> merged.type = "object"
            }
        }

        return merged
    }

    private fun mergeTypes(
        leftType: String?,
        rightType: String?,
    ): String? =
        when {
            leftType == "object" || rightType == "object" -> "object"
            leftType != null -> leftType
            else -> rightType
        }

    private fun mergeNullable(
        leftNullable: Boolean?,
        rightNullable: Boolean?,
    ): Boolean? =
        when {
            leftNullable == null && rightNullable == null -> null
            else -> leftNullable == true || rightNullable == true
        }

    private fun mergeProperties(
        left: Schema<*>,
        right: Schema<*>,
    ): MutableMap<String, Schema<*>>? {
        val leftProps = left.properties ?: emptyMap()
        val rightProps = right.properties ?: emptyMap()
        if (leftProps.isEmpty() && rightProps.isEmpty()) return null

        val merged = LinkedHashMap<String, Schema<*>>(leftProps)
        for ((key, value) in rightProps) {
            val existing = merged[key]
            merged[key] = if (existing != null) merge(existing, value) else value
        }
        return merged
    }

    private fun mergeEnums(
        left: Schema<*>,
        right: Schema<*>,
    ): MutableList<Any>? {
        val leftEnum = left.enum
        val rightEnum = right.enum
        if (leftEnum == null && rightEnum == null) return null

        val union =
            buildList {
                if (leftEnum != null) addAll(leftEnum)
                if (rightEnum != null) addAll(rightEnum)
            }.distinct()

        return if (union.isNotEmpty()) union.toMutableList() else null
    }

    private fun mergeExample(
        left: Any?,
        right: Any?,
    ): Any? =
        when {
            left != null && right != null && left == right -> left
            left != null -> left
            else -> right
        }

    private fun mergeConst(
        left: Any?,
        right: Any?,
    ): Any? =
        when {
            left != null && right != null -> if (left == right) left else null
            left != null -> left
            right != null -> right
            else -> null
        }

    private fun mergeItems(
        left: Schema<*>?,
        right: Schema<*>?,
    ): Schema<*>? =
        when {
            left != null && right != null -> merge(left, right)
            left != null -> left
            right != null -> right
            else -> null
        }

    private fun mergeRequired(
        left: List<String>?,
        right: List<String>?,
    ): List<String>? =
        when {
            left != null && right != null -> left.intersect(right.toSet()).toList()
            left != null -> left
            right != null -> right
            else -> null
        }

    private fun mergeAdditionalProperties(
        left: Any?,
        right: Any?,
    ): Any? =
        when {
            left is Schema<*> && right is Schema<*> -> merge(left, right)
            left is Schema<*> -> left
            right is Schema<*> -> right
            left != null -> left
            right != null -> right
            else -> null
        }
}
