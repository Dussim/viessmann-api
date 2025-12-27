package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.feature.api.validation.propertyHash

sealed class EfficientStringKeyMap<out T>(
    private val originalMap: Map<String, T>,
) : Map<String, T> by originalMap {
    companion object {
        operator fun <T> invoke(from: Map<String, T>): EfficientStringKeyMap<T> =
            when (from.size) {
                0 -> Empty
                1 -> OneElement(from)
                2 -> TwoElements(from)
                3 -> ThreeElements(from)
                4 -> FourElements(from)
                else -> NElements(from)
            }
    }

    open class Serializer<T>(
        valueSerializer: KSerializer<T>,
    ) : KSerializer<EfficientStringKeyMap<T>> {
        private val delegateSerializer = MapSerializer(String.serializer(), valueSerializer)

        override val descriptor = delegateSerializer.descriptor

        override fun serialize(
            encoder: Encoder,
            value: EfficientStringKeyMap<T>,
        ) {
            delegateSerializer.serialize(encoder, value.originalMap)
        }

        override fun deserialize(decoder: Decoder): EfficientStringKeyMap<T> = invoke(delegateSerializer.deserialize(decoder))
    }

    private val precomputedHash = originalMap.hashCode()

    final override fun hashCode(): Int = precomputedHash

    final override fun equals(other: Any?): Boolean = originalMap == other

    final override fun get(key: String): T? = get(key, propertyHash(key.hashCode(), key.length))

    abstract operator fun get(
        key: String,
        precomputedHash: Long,
    ): T?
}

private data object Empty : EfficientStringKeyMap<Nothing>(emptyMap()) {
    override val size = 0

    override fun isEmpty(): Boolean = true

    override fun containsKey(key: String): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false

    override fun get(
        key: String,
        precomputedHash: Long,
    ): Nothing? = null
}

private data class OneElement<T>(
    private val originalMap: Map<String, T>,
    private val key1: String = originalMap.keys.elementAtOrNull(0) ?: error("One element map must have at least one element"),
    private val value1: T = originalMap.values.elementAtOrNull(0) ?: error("One element map must have at least one element"),
    private val precomputedHash1: Long = propertyHash(key1.hashCode(), key1.length),
    override val size: Int = 1,
) : EfficientStringKeyMap<T>(originalMap) {
    override fun get(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (key1.compareTo(key) == 0) -> value1
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private data class TwoElements<T>(
    private val originalMap: Map<String, T>,
    private val key1: String = originalMap.keys.elementAtOrNull(0) ?: error("Two elements map must have at least two elements"),
    private val value1: T = originalMap.values.elementAtOrNull(0) ?: error("Two elements map must have at least two elements"),
    private val key2: String = originalMap.keys.elementAtOrNull(1) ?: error("Two elements map must have at least two elements"),
    private val value2: T = originalMap.values.elementAtOrNull(1) ?: error("Two elements map must have at least two elements"),
    private val precomputedHash1: Long = propertyHash(key1.hashCode(), key1.length),
    private val precomputedHash2: Long = propertyHash(key2.hashCode(), key2.length),
    override val size: Int = 2,
) : EfficientStringKeyMap<T>(originalMap) {
    override fun get(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (key1.compareTo(key) == 0) -> value1
            precomputedHash2 if (key2.compareTo(key) == 0) -> value2
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private data class ThreeElements<T>(
    private val originalMap: Map<String, T>,
    private val key1: String = originalMap.keys.elementAtOrNull(0) ?: error("Three elements map must have at least three elements"),
    private val value1: T = originalMap.values.elementAtOrNull(0) ?: error("Three elements map must have at least three elements"),
    private val key2: String = originalMap.keys.elementAtOrNull(1) ?: error("Three elements map must have at least three elements"),
    private val value2: T = originalMap.values.elementAtOrNull(1) ?: error("Three elements map must have at least three elements"),
    private val key3: String = originalMap.keys.elementAtOrNull(2) ?: error("Three elements map must have at least three elements"),
    private val value3: T = originalMap.values.elementAtOrNull(2) ?: error("Three elements map must have at least three elements"),
    private val precomputedHash1: Long = propertyHash(key1.hashCode(), key1.length),
    private val precomputedHash2: Long = propertyHash(key2.hashCode(), key2.length),
    private val precomputedHash3: Long = propertyHash(key3.hashCode(), key3.length),
    override val size: Int = 3,
) : EfficientStringKeyMap<T>(originalMap) {
    override fun get(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (key1.compareTo(key) == 0) -> value1
            precomputedHash2 if (key2.compareTo(key) == 0) -> value2
            precomputedHash3 if (key3.compareTo(key) == 0) -> value3
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private data class FourElements<T>(
    private val originalMap: Map<String, T>,
    private val key1: String = originalMap.keys.elementAtOrNull(0) ?: error("Four elements map must have at least four elements"),
    private val value1: T = originalMap.values.elementAtOrNull(0) ?: error("Four elements map must have at least four elements"),
    private val key2: String = originalMap.keys.elementAtOrNull(1) ?: error("Four elements map must have at least four elements"),
    private val value2: T = originalMap.values.elementAtOrNull(1) ?: error("Four elements map must have at least four elements"),
    private val key3: String = originalMap.keys.elementAtOrNull(2) ?: error("Four elements map must have at least four elements"),
    private val value3: T = originalMap.values.elementAtOrNull(2) ?: error("Four elements map must have at least four elements"),
    private val key4: String = originalMap.keys.elementAtOrNull(3) ?: error("Four elements map must have at least four elements"),
    private val value4: T = originalMap.values.elementAtOrNull(3) ?: error("Four elements map must have at least four elements"),
    private val precomputedHash1: Long = propertyHash(key1.hashCode(), key1.length),
    private val precomputedHash2: Long = propertyHash(key2.hashCode(), key2.length),
    private val precomputedHash3: Long = propertyHash(key3.hashCode(), key3.length),
    private val precomputedHash4: Long = propertyHash(key4.hashCode(), key4.length),
    override val size: Int = 4,
) : EfficientStringKeyMap<T>(originalMap) {
    override fun get(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (key1.compareTo(key) == 0) -> value1
            precomputedHash2 if (key2.compareTo(key) == 0) -> value2
            precomputedHash3 if (key3.compareTo(key) == 0) -> value3
            precomputedHash4 if (key4.compareTo(key) == 0) -> value4
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

@Suppress("ArrayInDataClass")
private data class NElements<T>(
    private val originalMap: Map<String, T>,
    private val arrayKeys: Array<String> = originalMap.keys.sortedBy { it.length }.toTypedArray(),
    private val arrayValues: Array<Any?> = Array(arrayKeys.size) { originalMap[arrayKeys[it]] },
    private val arrayHashes: LongArray = LongArray(arrayKeys.size) { propertyHash(arrayKeys[it].hashCode(), arrayKeys[it].length) },
    private val startIndexJumpTable: IntArray =
        IntArray(arrayKeys.maxOf { it.length } + 1) {
            arrayKeys.indexOfFirst { key -> key.length == it }
        },
    private val endIndexJumpTable: IntArray =
        IntArray(startIndexJumpTable.size) {
            val jump = startIndexJumpTable[it]
            if (jump == -1) {
                -1
            } else {
                val length = arrayKeys[jump].length
                val index = arrayKeys.indexOfFirst { key -> key.length > length }
                if (index == -1) arrayKeys.size else index
            }
        },
    private val lengthBitset: Int = originalMap.keys.fold(0) { acc, key -> acc or (1 shl key.length) },
    override val size: Int = originalMap.size,
) : EfficientStringKeyMap<T>(originalMap) {
    override fun get(
        key: String,
        precomputedHash: Long,
    ): T? {
        val length = extractLow(precomputedHash)
        if (lengthBitset and (1 shl length) == 0) return null
        var current = startIndexJumpTable[length]
        val end = endIndexJumpTable[length]
        do {
            if (precomputedHash == arrayHashes[current] && key.compareTo(arrayKeys[current]) == 0) {
                @Suppress("UNCHECKED_CAST")
                return arrayValues[current] as T?
            }
            current++
        } while (current < end)
        return null
    }

    override fun isEmpty(): Boolean = false

    @Suppress("NOTHING_TO_INLINE")
    private inline fun extractLow(value: Long): Int = (value and 0xFFFFFFFFL).toInt()
}

data object ParametersSerializer : EfficientStringKeyMap.Serializer<Parameter>(Parameter.serializer())

data object PropertiesSerializer : EfficientStringKeyMap.Serializer<Property>(Property.serializer())

data object CommandsSerializer : EfficientStringKeyMap.Serializer<Command>(Command.serializer())
