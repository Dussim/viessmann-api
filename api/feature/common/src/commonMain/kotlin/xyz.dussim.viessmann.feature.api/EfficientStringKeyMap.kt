package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.feature.api.validation.combineToLong

@Suppress("NOTHING_TO_INLINE")
abstract class EfficientStringKeyMap<T> : Map<String, T> {
    abstract operator fun get(
        key: String,
        combined: Long,
    ): T?

    final override fun containsKey(key: String): Boolean = keys.contains(key)

    final override fun containsValue(value: T): Boolean = values.contains(value)

    data object CommandsSerializer : KSerializer<EfficientStringKeyMap<Command>> {
        private val delegateSerializer = MapSerializer(String.Companion.serializer(), Command.serializer())
        override val descriptor = delegateSerializer.descriptor

        override fun serialize(
            encoder: Encoder,
            value: EfficientStringKeyMap<Command>,
        ) {
            delegateSerializer.serialize(encoder, value)
        }

        override fun deserialize(decoder: Decoder): EfficientStringKeyMap<Command> = createFrom(decoder.decodeSerializableValue(delegateSerializer))
    }

    data object ParametersSerializer : KSerializer<EfficientStringKeyMap<Parameter>> {
        private val delegateSerializer = MapSerializer(String.serializer(), Parameter.serializer())
        override val descriptor = delegateSerializer.descriptor

        override fun serialize(
            encoder: Encoder,
            value: EfficientStringKeyMap<Parameter>,
        ) {
            delegateSerializer.serialize(encoder, value)
        }

        override fun deserialize(decoder: Decoder): EfficientStringKeyMap<Parameter> = createFrom(decoder.decodeSerializableValue(delegateSerializer))
    }

    data object PropertiesSerializer : KSerializer<EfficientStringKeyMap<Property>> {
        private val delegateSerializer = MapSerializer(String.serializer(), Property.serializer())
        override val descriptor = delegateSerializer.descriptor

        override fun serialize(
            encoder: Encoder,
            value: EfficientStringKeyMap<Property>,
        ) {
            delegateSerializer.serialize(encoder, value)
        }

        override fun deserialize(decoder: Decoder): EfficientStringKeyMap<Property> = createFrom(decoder.decodeSerializableValue(delegateSerializer))
    }

    companion object {
        private val EMPTY =
            object : EfficientStringKeyMap<Nothing>() {
                override val size = 0
                override val keys = emptySet<String>()
                override val values = emptyList<Nothing>()
                override val entries = emptySet<Map.Entry<String, Nothing>>()

                override fun isEmpty(): Boolean = true

                override fun get(key: String): Nothing? = null

                override fun get(
                    key: String,
                    combined: Long,
                ): Nothing? = null
            }

        @Suppress("UNCHECKED_CAST")
        fun <T> createFrom(map: Map<String, T>): EfficientStringKeyMap<T> =
            when (map.size) {
                0 -> EMPTY as EfficientStringKeyMap<T>
                1 -> {
                    val entry = map.entries.first()
                    OneElement(
                        key1 = entry.key,
                        value1 = entry.value,
                    )
                }

                2 -> {
                    val iterator = map.entries.iterator()
                    val entry1 = iterator.next()
                    val entry2 = iterator.next()
                    TwoElements(
                        key1 = entry1.key,
                        value1 = entry1.value,
                        key2 = entry2.key,
                        value2 = entry2.value,
                    )
                }

                3 -> {
                    val iterator = map.entries.iterator()
                    val entry1 = iterator.next()
                    val entry2 = iterator.next()
                    val entry3 = iterator.next()
                    ThreeElements(
                        key1 = entry1.key,
                        value1 = entry1.value,
                        key2 = entry2.key,
                        value2 = entry2.value,
                        key3 = entry3.key,
                        value3 = entry3.value,
                    )
                }

                4 -> {
                    val iterator = map.entries.iterator()
                    val entry1 = iterator.next()
                    val entry2 = iterator.next()
                    val entry3 = iterator.next()
                    val entry4 = iterator.next()
                    FourElements(
                        key1 = entry1.key,
                        value1 = entry1.value,
                        key2 = entry2.key,
                        value2 = entry2.value,
                        key3 = entry3.key,
                        value3 = entry3.value,
                        key4 = entry4.key,
                        value4 = entry4.value,
                    )
                }

                else -> NElements(map)
            }
    }

    private class OneElement<T>(
        private val key1: String,
        private val value1: T,
    ) : EfficientStringKeyMap<T>() {
        private val hash1 = key1.hashCode()
        private val length1 = key1.length
        private val combined = combineToLong(hash1, length1)

        override val size: Int = 1
        override val keys: Set<String> = setOf(key1)
        override val values: Collection<T> = listOf(value1)
        override val entries: Set<Map.Entry<String, T>> =
            setOf(
                object : Map.Entry<String, T> {
                    override val key: String = key1
                    override val value: T = value1
                },
            )

        override fun get(key: String): T? = getImpl(key, combineToLong(key.hashCode(), key.length))

        override fun get(
            key: String,
            combined: Long,
        ): T? = getImpl(key, combined)

        override fun isEmpty(): Boolean = false

        private inline fun getImpl(
            key: String,
            combined: Long,
        ): T? =
            when (combined) {
                this.combined if (key1.compareTo(key) == 0) -> value1
                else -> null
            }
    }

    private class TwoElements<T>(
        private val key1: String,
        private val value1: T,
        private val key2: String,
        private val value2: T,
    ) : EfficientStringKeyMap<T>() {
        private val combined1 = combineToLong(key1.hashCode(), key1.length)
        private val combined2 = combineToLong(key2.hashCode(), key2.length)

        override val size: Int = 2
        override val keys: Set<String> = setOf(key1, key2)
        override val values: Collection<T> = listOf(value1, value2)
        override val entries: Set<Map.Entry<String, T>> =
            setOf(
                object : Map.Entry<String, T> {
                    override val key: String = key1
                    override val value: T = value1
                },
                object : Map.Entry<String, T> {
                    override val key: String = key2
                    override val value: T = value2
                },
            )

        override fun get(key: String): T? = getImpl(key, combineToLong(key.hashCode(), key.length))

        override fun get(
            key: String,
            combined: Long,
        ): T? = getImpl(key, combined)

        override fun isEmpty(): Boolean = false

        private fun getImpl(
            key: String,
            combined: Long,
        ): T? =
            when (combined) {
                combined1 if (key1.compareTo(key) == 0) -> value1
                combined2 if (key2.compareTo(key) == 0) -> value2
                else -> null
            }
    }

    private class ThreeElements<T>(
        private val key1: String,
        private val value1: T,
        private val key2: String,
        private val value2: T,
        private val key3: String,
        private val value3: T,
    ) : EfficientStringKeyMap<T>() {
        private val combined1 = combineToLong(key1.hashCode(), key1.length)
        private val combined2 = combineToLong(key2.hashCode(), key2.length)
        private val combined3 = combineToLong(key3.hashCode(), key3.length)

        override val size: Int = 3
        override val keys: Set<String> = setOf(key1, key2, key3)
        override val values: Collection<T> = listOf(value1, value2, value3)
        override val entries: Set<Map.Entry<String, T>> =
            setOf(
                object : Map.Entry<String, T> {
                    override val key: String = key1
                    override val value: T = value1
                },
                object : Map.Entry<String, T> {
                    override val key: String = key2
                    override val value: T = value2
                },
                object : Map.Entry<String, T> {
                    override val key: String = key3
                    override val value: T = value3
                },
            )

        override fun get(key: String): T? = getImpl(key, combineToLong(key.hashCode(), key.length))

        override fun get(
            key: String,
            combined: Long,
        ): T? = getImpl(key, combined)

        override fun isEmpty(): Boolean = false

        private fun getImpl(
            key: String,
            combined: Long,
        ): T? =
            when (combined) {
                combined1 if (key1.compareTo(key) == 0) -> value1
                combined2 if (key2.compareTo(key) == 0) -> value2
                combined3 if (key3.compareTo(key) == 0) -> value3
                else -> null
            }
    }

    private class FourElements<T>(
        private val key1: String,
        private val value1: T,
        private val key2: String,
        private val value2: T,
        private val key3: String,
        private val value3: T,
        private val key4: String,
        private val value4: T,
    ) : EfficientStringKeyMap<T>() {
        private val combined1 = combineToLong(key1.hashCode(), key1.length)
        private val combined2 = combineToLong(key2.hashCode(), key2.length)
        private val combined3 = combineToLong(key3.hashCode(), key3.length)
        private val combined4 = combineToLong(key4.hashCode(), key4.length)

        override val size: Int = 4
        override val keys: Set<String> = setOf(key1, key2, key3, key4)
        override val values: Collection<T> = listOf(value1, value2, value3, value4)
        override val entries: Set<Map.Entry<String, T>> =
            setOf(
                object : Map.Entry<String, T> {
                    override val key: String = key1
                    override val value: T = value1
                },
                object : Map.Entry<String, T> {
                    override val key: String = key2
                    override val value: T = value2
                },
                object : Map.Entry<String, T> {
                    override val key: String = key3
                    override val value: T = value3
                },
                object : Map.Entry<String, T> {
                    override val key: String = key4
                    override val value: T = value4
                },
            )

        override fun get(key: String): T? = getImpl(key, combineToLong(key.hashCode(), key.length))

        override fun get(
            key: String,
            combined: Long,
        ): T? = getImpl(key, combined)

        override fun isEmpty(): Boolean = false

        private fun getImpl(
            key: String,
            combined: Long,
        ): T? =
            when (combined) {
                combined1 if (key1.compareTo(key) == 0) -> value1
                combined2 if (key2.compareTo(key) == 0) -> value2
                combined3 if (key3.compareTo(key) == 0) -> value3
                combined4 if (key4.compareTo(key) == 0) -> value4
                else -> null
            }
    }

    class NElements<T>(
        private val map: Map<String, T>,
    ) : EfficientStringKeyMap<T>() {
        private val keyArray = map.keys.sortedBy { it.length }.toTypedArray()
        private val combinedArray = keyArray.map { combineToLong(it.hashCode(), it.length) }.toLongArray()
        private val valuesArray = keyArray.map { map[it] }.toTypedArray<Any?>()
        private val jumpTable =
            IntArray(keyArray.maxOf { it.length } + 1) {
                keyArray.indexOfFirst { key -> key.length == it }
            }
        private val endIndex =
            IntArray(jumpTable.size) {
                val jump = jumpTable[it]
                if (jump == -1) {
                    -1
                } else {
                    val length = keyArray[jump].length
                    val index = keyArray.indexOfFirst { key -> key.length > length }
                    if (index == -1) keyArray.size else index
                }
            }
        private var bitset = 0

        override val size = map.size
        override val keys = map.keys
        override val values = map.values
        override val entries = map.entries

        init {
            for (key in keyArray) {
                bitset = bitset or (1 shl key.length)
            }
        }

        override fun get(key: String): T? = getImpl(key, combineToLong(key.hashCode(), key.length))

        override fun get(
            key: String,
            combined: Long,
        ): T? = getImpl(key, combined)

        override fun isEmpty() = false

        private inline fun extractLow(value: Long): Int = (value and 0xFFFFFFFFL).toInt()

        @Suppress("UNCHECKED_CAST")
        private inline fun getImpl(
            key: String,
            combined: Long,
        ): T? {
            val length = extractLow(combined)
            if (bitset and (1 shl length) == 0) return null
            var current = jumpTable[length]
            val end = endIndex[length]
            do {
                if (combined == combinedArray[current] && key.compareTo(keyArray[current]) == 0) {
                    return valuesArray[current] as T?
                }
                current++
            } while (current < end)
            return null
        }
    }
}
