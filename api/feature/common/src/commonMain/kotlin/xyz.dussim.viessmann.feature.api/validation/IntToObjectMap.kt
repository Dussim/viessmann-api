package xyz.dussim.viessmann.feature.api.validation

sealed interface IntToObjectMap<T> {
    operator fun get(key: Int): T?

    private data object Empty : IntToObjectMap<Nothing> {
        override fun get(key: Int): Nothing? = null
    }

    private class IntToObjectMap1<T>(
        private val key1: Int,
        private val value1: T,
    ) : IntToObjectMap<T> {
        override fun get(key: Int): T? = if (key == key1) value1 else null

        companion object {
            fun <T> toTwoElements(
                map: IntToObjectMap1<T>,
                key2: Int,
                value2: T,
            ) = IntToObjectMap2(map.key1, map.value1, key2, value2)
        }
    }

    private class IntToObjectMap2<T>(
        private val key1: Int,
        private val value1: T,
        private val key2: Int,
        private val value2: T,
    ) : IntToObjectMap<T> {
        override fun get(key: Int): T? =
            when (key) {
                key1 -> value1
                key2 -> value2
                else -> null
            }

        companion object {
            fun <T> toNElements(
                map: IntToObjectMap2<T>,
                key3: Int,
                value3: T,
            ) = IntToObjectMapN(
                mutableMapOf(
                    map.key1 to map.value1,
                    map.key2 to map.value2,
                    key3 to value3,
                ),
            )
        }
    }

    private class IntToObjectMapN<T>(
        private val map: MutableMap<Int, T>,
    ) : IntToObjectMap<T>,
        MutableMap<Int, T> by map {
        override fun get(key: Int): T? = map[key]
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> of(): IntToObjectMap<T> = Empty as IntToObjectMap<T>

        fun <T> of(
            key1: Int,
            value1: T,
            previous: IntToObjectMap<T>,
        ): IntToObjectMap<T> =
            when (previous) {
                is Empty ->
                    IntToObjectMap1(
                        key1 = key1,
                        value1 = value1,
                    )

                is IntToObjectMap1<T> ->
                    IntToObjectMap1.toTwoElements(
                        map = previous,
                        key2 = key1,
                        value2 = value1,
                    )

                is IntToObjectMap2<T> ->
                    IntToObjectMap2.toNElements(
                        map = previous,
                        key3 = key1,
                        value3 = value1,
                    )

                is IntToObjectMapN<T> -> previous.also { it[key1] = value1 }
            }
    }
}
