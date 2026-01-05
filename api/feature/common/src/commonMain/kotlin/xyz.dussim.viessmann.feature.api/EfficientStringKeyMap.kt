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
    private val key1: String,
    private val value1: T,
    private val precomputedHash1: Long,
    override val size: Int,
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
    private val key1: String,
    private val value1: T,
    private val key2: String,
    private val value2: T,
    private val precomputedHash1: Long,
    private val precomputedHash2: Long,
    override val size: Int,
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
    private val key1: String,
    private val value1: T,
    private val key2: String,
    private val value2: T,
    private val key3: String,
    private val value3: T,
    private val precomputedHash1: Long,
    private val precomputedHash2: Long,
    private val precomputedHash3: Long,
    override val size: Int,
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
    private val key1: String,
    private val value1: T,
    private val key2: String,
    private val value2: T,
    private val key3: String,
    private val value3: T,
    private val key4: String,
    private val value4: T,
    private val precomputedHash1: Long,
    private val precomputedHash2: Long,
    private val precomputedHash3: Long,
    private val precomputedHash4: Long,
    override val size: Int,
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
    private val arrayKeys: Array<String>,
    private val arrayValues: Array<Any?>,
    private val arrayHashes: LongArray,
    private val startIndexJumpTable: IntArray,
    private val endIndexJumpTable: IntArray,
    private val lengthBitset: Int,
    override val size: Int,
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

private fun <T> OneElement(originalMap: Map<String, T>): OneElement<T> {
    val (key1, value1) = originalMap.entries.first()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    return OneElement(originalMap, key1, value1, precomputedHash1, 1)
}

private fun <T> TwoElements(originalMap: Map<String, T>): TwoElements<T> {
    val entries = originalMap.entries.iterator()
    val (key1, value1) = entries.next()
    val (key2, value2) = entries.next()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    val precomputedHash2 = propertyHash(key2.hashCode(), key2.length)
    return TwoElements(originalMap, key1, value1, key2, value2, precomputedHash1, precomputedHash2, 2)
}

private fun <T> ThreeElements(originalMap: Map<String, T>): ThreeElements<T> {
    val entries = originalMap.entries.iterator()
    val (key1, value1) = entries.next()
    val (key2, value2) = entries.next()
    val (key3, value3) = entries.next()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    val precomputedHash2 = propertyHash(key2.hashCode(), key2.length)
    val precomputedHash3 = propertyHash(key3.hashCode(), key3.length)
    return ThreeElements(originalMap, key1, value1, key2, value2, key3, value3, precomputedHash1, precomputedHash2, precomputedHash3, 3)
}

private fun <T> FourElements(originalMap: Map<String, T>): FourElements<T> {
    val entries = originalMap.entries.iterator()
    val (key1, value1) = entries.next()
    val (key2, value2) = entries.next()
    val (key3, value3) = entries.next()
    val (key4, value4) = entries.next()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    val precomputedHash2 = propertyHash(key2.hashCode(), key2.length)
    val precomputedHash3 = propertyHash(key3.hashCode(), key3.length)
    val precomputedHash4 = propertyHash(key4.hashCode(), key4.length)
    return FourElements(originalMap, key1, value1, key2, value2, key3, value3, key4, value4, precomputedHash1, precomputedHash2, precomputedHash3, precomputedHash4, 4)
}

private fun <T> NElements(originalMap: Map<String, T>): NElements<T> {
    val sortedEntries = originalMap.entries.sortedBy { it.key.length }
    val arrayKeys = Array(sortedEntries.size) { sortedEntries[it].key }
    val arrayValues = Array<Any?>(sortedEntries.size) { sortedEntries[it].value }
    val arrayHashes = LongArray(sortedEntries.size) { propertyHash(arrayKeys[it].hashCode(), arrayKeys[it].length) }
    val startIndexJumpTable =
        IntArray(arrayKeys.maxOf { it.length } + 1) {
            arrayKeys.indexOfFirst { key -> key.length == it }
        }
    val endIndexJumpTable =
        IntArray(startIndexJumpTable.size) {
            val jump = startIndexJumpTable[it]
            if (jump == -1) {
                -1
            } else {
                val length = arrayKeys[jump].length
                val index = arrayKeys.indexOfFirst { key -> key.length > length }
                if (index == -1) arrayKeys.size else index
            }
        }
    val lengthBitset = arrayKeys.fold(0) { acc, key -> acc or (1 shl key.length) }
    return NElements(originalMap, arrayKeys, arrayValues, arrayHashes, startIndexJumpTable, endIndexJumpTable, lengthBitset, originalMap.size)
}
