package xyz.dussim.feature.benchmark.maps

@PublishedApi
internal inline fun combineToLong(
    high: Int,
    low: Int,
): Long = (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)
