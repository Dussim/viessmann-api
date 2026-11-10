package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.feature.api.validation.propertyHash

/**
 * Returns the canonical instance of this key. Keys are canonicalized once when a map is built so that lookups with
 * canonical keys (e.g. string literals in generated rules) match by reference in [sameKey].
 */
@PublishedApi
internal expect fun String.canonicalKey(): String

/**
 * [stored] is canonical; the reference check is the fast path, content comparison the fallback for any other key.
 */
@Suppress("NOTHING_TO_INLINE")
private inline fun sameKey(
    stored: String,
    key: String,
): Boolean = stored === key || stored == key

private const val KIND_EMPTY = 0
private const val KIND_ONE = 1
private const val KIND_TWO = 2
private const val KIND_THREE = 3
private const val KIND_FOUR = 4
private const val KIND_N = 5
private const val KIND_PASS_THROUGH = 6

/**
 * Lookups dispatch on [kind] in the final [get] instead of a virtual call: rule lambdas are shared by every feature,
 * so a virtual call site would see every representation and stay megamorphic (not inlinable). A `when` over an `Int`
 * field plus casts to final classes lets the JIT inline the per-kind bodies into the caller.
 */
sealed class EfficientStringKeyMap<out T>(
    private val originalMap: Map<String, T>,
    private val kind: Int,
) : Map<String, T> by originalMap {
    companion object {
        operator fun <T> invoke(from: Map<String, T>): EfficientStringKeyMap<T> =
            when (from.size) {
                0 -> Empty
                1 -> OneElement(from)
                2 -> TwoElements(from)
                3 -> ThreeElements(from)
                4 -> FourElements(from)
                else if (from.keys.maxOfOrNull { it.length } ?: 0) < 64 -> NElements(from)
                else -> PassThrough(from)
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

    final override fun toString(): String = originalMap.toString()

    // A final field instead of a per-representation override: call sites that see every representation would
    // otherwise make `size` a megamorphic virtual call.
    final override val size: Int = originalMap.size

    final override fun get(key: String): T? = get(key, propertyHash(key.hashCode(), key.length))

    @Suppress("UNCHECKED_CAST")
    operator fun get(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (kind) {
            KIND_EMPTY -> null
            KIND_ONE -> (this as OneElement<T>).lookup(key, precomputedHash)
            KIND_TWO -> (this as TwoElements<T>).lookup(key, precomputedHash)
            KIND_THREE -> (this as ThreeElements<T>).lookup(key, precomputedHash)
            KIND_FOUR -> (this as FourElements<T>).lookup(key, precomputedHash)
            KIND_N -> (this as NElements<T>).lookup(key, precomputedHash)
            else -> originalMap[key]
        }
}

private data object Empty : EfficientStringKeyMap<Nothing>(emptyMap(), KIND_EMPTY) {
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: String): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
}

private class OneElement<T>(
    originalMap: Map<String, T>,
    private val key1: String,
    private val value1: T,
    private val precomputedHash1: Long,
) : EfficientStringKeyMap<T>(originalMap, KIND_ONE) {
    fun lookup(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (sameKey(key1, key)) -> value1
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private class TwoElements<T>(
    originalMap: Map<String, T>,
    private val key1: String,
    private val value1: T,
    private val key2: String,
    private val value2: T,
    private val precomputedHash1: Long,
    private val precomputedHash2: Long,
) : EfficientStringKeyMap<T>(originalMap, KIND_TWO) {
    fun lookup(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (sameKey(key1, key)) -> value1
            precomputedHash2 if (sameKey(key2, key)) -> value2
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private class ThreeElements<T>(
    originalMap: Map<String, T>,
    private val key1: String,
    private val value1: T,
    private val key2: String,
    private val value2: T,
    private val key3: String,
    private val value3: T,
    private val precomputedHash1: Long,
    private val precomputedHash2: Long,
    private val precomputedHash3: Long,
) : EfficientStringKeyMap<T>(originalMap, KIND_THREE) {
    fun lookup(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (sameKey(key1, key)) -> value1
            precomputedHash2 if (sameKey(key2, key)) -> value2
            precomputedHash3 if (sameKey(key3, key)) -> value3
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private class FourElements<T>(
    originalMap: Map<String, T>,
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
) : EfficientStringKeyMap<T>(originalMap, KIND_FOUR) {
    fun lookup(
        key: String,
        precomputedHash: Long,
    ): T? =
        when (precomputedHash) {
            precomputedHash1 if (sameKey(key1, key)) -> value1
            precomputedHash2 if (sameKey(key2, key)) -> value2
            precomputedHash3 if (sameKey(key3, key)) -> value3
            precomputedHash4 if (sameKey(key4, key)) -> value4
            else -> null
        }

    override fun isEmpty(): Boolean = false
}

private class NElements<T>(
    originalMap: Map<String, T>,
    private val arrayKeys: Array<String>,
    private val arrayValues: Array<Any?>,
    private val arrayHashes: LongArray,
    private val startIndexByLength: IntArray,
    private val lengthBitset: Long,
) : EfficientStringKeyMap<T>(originalMap, KIND_N) {
    fun lookup(
        key: String,
        precomputedHash: Long,
    ): T? {
        // Stored keys are shorter than 64 (longer maps use PassThrough), so a lookup key of 64+ characters can never
        // be present. Without this check `1L shl length` wraps around and the jump table is indexed out of range.
        val length = extractLow(precomputedHash)
        if (length >= Long.SIZE_BITS) return null
        if (lengthBitset and (1L shl length) == 0L) return null
        var current = startIndexByLength[length]
        val end = startIndexByLength[length + 1]
        do {
            if (precomputedHash == arrayHashes[current] && sameKey(arrayKeys[current], key)) {
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

private class PassThrough<T>(
    originalMap: Map<String, T>,
) : EfficientStringKeyMap<T>(originalMap, KIND_PASS_THROUGH)

data object ParametersSerializer : EfficientStringKeyMap.Serializer<Parameter>(Parameter.serializer())

data object PropertiesSerializer : EfficientStringKeyMap.Serializer<Property>(Property.serializer())

data object CommandsSerializer : EfficientStringKeyMap.Serializer<Command>(Command.serializer())

private fun <T> OneElement(originalMap: Map<String, T>): OneElement<T> {
    val (key1, value1) = originalMap.entries.first()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    return OneElement(originalMap, key1.canonicalKey(), value1, precomputedHash1)
}

private fun <T> TwoElements(originalMap: Map<String, T>): TwoElements<T> {
    val entries = originalMap.entries.iterator()
    val (key1, value1) = entries.next()
    val (key2, value2) = entries.next()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    val precomputedHash2 = propertyHash(key2.hashCode(), key2.length)
    return TwoElements(originalMap, key1.canonicalKey(), value1, key2.canonicalKey(), value2, precomputedHash1, precomputedHash2)
}

private fun <T> ThreeElements(originalMap: Map<String, T>): ThreeElements<T> {
    val entries = originalMap.entries.iterator()
    val (key1, value1) = entries.next()
    val (key2, value2) = entries.next()
    val (key3, value3) = entries.next()
    val precomputedHash1 = propertyHash(key1.hashCode(), key1.length)
    val precomputedHash2 = propertyHash(key2.hashCode(), key2.length)
    val precomputedHash3 = propertyHash(key3.hashCode(), key3.length)
    return ThreeElements(originalMap, key1.canonicalKey(), value1, key2.canonicalKey(), value2, key3.canonicalKey(), value3, precomputedHash1, precomputedHash2, precomputedHash3)
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
    return FourElements(originalMap, key1.canonicalKey(), value1, key2.canonicalKey(), value2, key3.canonicalKey(), value3, key4.canonicalKey(), value4, precomputedHash1, precomputedHash2, precomputedHash3, precomputedHash4)
}

private fun <T> NElements(originalMap: Map<String, T>): NElements<T> {
    val sortedEntries = originalMap.entries.sortedBy { it.key.length }
    val arrayKeys = Array(sortedEntries.size) { sortedEntries[it].key.canonicalKey() }
    val arrayValues = Array<Any?>(sortedEntries.size) { sortedEntries[it].value }
    val arrayHashes = LongArray(sortedEntries.size) { propertyHash(arrayKeys[it].hashCode(), arrayKeys[it].length) }
    // startIndexByLength[length] is the index of the first key of that length. Keys are sorted by length, so the
    // keys of one length end where the next length starts: count keys per length, then accumulate.
    val maxLength = arrayKeys.last().length
    val startIndexByLength = IntArray(maxLength + 2)
    for (key in arrayKeys) startIndexByLength[key.length + 1]++
    for (length in 1..maxLength + 1) startIndexByLength[length] += startIndexByLength[length - 1]
    val lengthBitset = arrayKeys.fold(0L) { acc, key -> acc or (1L shl key.length) }
    return NElements(originalMap, arrayKeys, arrayValues, arrayHashes, startIndexByLength, lengthBitset)
}
