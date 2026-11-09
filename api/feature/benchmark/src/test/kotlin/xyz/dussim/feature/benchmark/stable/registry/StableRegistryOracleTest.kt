package xyz.dussim.feature.benchmark.stable.registry

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import xyz.dussim.feature.benchmark.stable.StableSize
import xyz.dussim.viessmann.feature.api.FeatureRegistry

val StableRegistryOracleTest by testSuite {
    test("stable registry fixture holds the valid stable features followed by the indexed features") {
        val features = StableRegistryFixture.features()

        features shouldHaveSize StableSize.entries.size + StableRegistryFixture.INDEXED_FEATURE_COUNT
        features.take(StableSize.entries.size).map { it.feature } shouldContainExactly StableSize.entries.map { "benchmark.stable.${it.id}" }

        val indexedFeatures = features.drop(StableSize.entries.size)
        indexedFeatures.map { it.feature } shouldContainExactly
            List(StableRegistryFixture.INDEXED_FEATURE_COUNT) { index -> "benchmark.stable.registry.$index" }
        indexedFeatures.forEach { it.wildcardFeature shouldBe StableRegistryFixture.INDEXED_WILDCARD_NAME }
    }

    test("stable registry lookups resolve to the features the benchmarks expect") {
        val registry = FeatureRegistry.indexed(StableRegistryFixture.features())
        val staticDescriptor = StableRegistryFixture.staticDescriptor
        val indexedDescriptor = StableRegistryFixture.indexedDescriptor
        val index = StableRegistryFixture.LOOKUP_INDEX

        registry[staticDescriptor].feature shouldBe StableRegistryFixture.STATIC_FEATURE_NAME
        registry[indexedDescriptor].feature shouldBe StableRegistryFixture.FIRST_INDEXED_FEATURE_NAME
        registry[indexedDescriptor, index].feature shouldBe StableRegistryFixture.INDEXED_LOOKUP_FEATURE_NAME
        registry.firstOf(indexedDescriptor, indexedDescriptor.byNameThenStructure(index)).feature shouldBe
            StableRegistryFixture.INDEXED_LOOKUP_FEATURE_NAME

        registry.allOf(indexedDescriptor, indexedDescriptor.byWildcardNameThenStructure) shouldHaveSize
            StableRegistryFixture.INDEXED_FEATURE_COUNT
        registry
            .findOf(indexedDescriptor, indexedDescriptor.byNameThenStructure(StableRegistryFixture.INDEXED_FEATURE_COUNT))
            .shouldBeNull()
    }

    test("stable registry lookups return the cached conversion on repeated calls") {
        val registry = FeatureRegistry.indexed(StableRegistryFixture.features())
        val staticDescriptor = StableRegistryFixture.staticDescriptor
        val indexedDescriptor = StableRegistryFixture.indexedDescriptor
        val index = StableRegistryFixture.LOOKUP_INDEX

        registry[staticDescriptor] shouldBeSameInstanceAs registry[staticDescriptor]
        registry[indexedDescriptor, index] shouldBeSameInstanceAs registry[indexedDescriptor, index]
        registry.firstOf(indexedDescriptor, indexedDescriptor.byNameThenStructure(index)) shouldBeSameInstanceAs
            registry[indexedDescriptor, index]
    }
}
