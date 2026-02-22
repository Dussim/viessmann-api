package xyz.dussim.viessmann.feature.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class EfficientStringKeyMapTest :
    FunSpec({
        context("Empty map") {
            test("returns null for any key") {
                val map = EfficientStringKeyMap<String>(emptyMap())
                map["any"].shouldBeNull()
                map[""].shouldBeNull()
            }

            test("isEmpty returns true") {
                val map = EfficientStringKeyMap<String>(emptyMap())
                map.isEmpty() shouldBe true
            }

            test("size returns 0") {
                val map = EfficientStringKeyMap<String>(emptyMap())
                map.size shouldBe 0
            }

            test("containsKey returns false") {
                val map = EfficientStringKeyMap<String>(emptyMap())
                map.containsKey("any") shouldBe false
            }
        }

        context("OneElement map") {
            test("returns value for matching key") {
                val map = EfficientStringKeyMap(mapOf("key" to "value"))
                map["key"] shouldBe "value"
            }

            test("returns null for non-matching key") {
                val map = EfficientStringKeyMap(mapOf("key" to "value"))
                map["other"].shouldBeNull()
                map["ke"].shouldBeNull()
                map["keys"].shouldBeNull()
            }

            test("isEmpty returns false") {
                val map = EfficientStringKeyMap(mapOf("key" to "value"))
                map.isEmpty() shouldBe false
            }

            test("size returns 1") {
                val map = EfficientStringKeyMap(mapOf("key" to "value"))
                map.size shouldBe 1
            }
        }

        context("TwoElements map") {
            test("returns correct values for both keys") {
                val map = EfficientStringKeyMap(mapOf("first" to "one", "second" to "two"))
                map["first"] shouldBe "one"
                map["second"] shouldBe "two"
            }

            test("returns null for non-matching keys") {
                val map = EfficientStringKeyMap(mapOf("first" to "one", "second" to "two"))
                map["third"].shouldBeNull()
                map[""].shouldBeNull()
            }

            test("size returns 2") {
                val map = EfficientStringKeyMap(mapOf("first" to "one", "second" to "two"))
                map.size shouldBe 2
            }
        }

        context("ThreeElements map") {
            test("returns correct values for all keys") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "bb" to 2, "ccc" to 3))
                map["a"] shouldBe 1
                map["bb"] shouldBe 2
                map["ccc"] shouldBe 3
            }

            test("returns null for non-matching keys") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "bb" to 2, "ccc" to 3))
                map["d"].shouldBeNull()
                map[""].shouldBeNull()
            }

            test("size returns 3") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "bb" to 2, "ccc" to 3))
                map.size shouldBe 3
            }
        }

        context("FourElements map") {
            test("returns correct values for all keys") {
                val map = EfficientStringKeyMap(mapOf("one" to 1, "two" to 2, "three" to 3, "four" to 4))
                map["one"] shouldBe 1
                map["two"] shouldBe 2
                map["three"] shouldBe 3
                map["four"] shouldBe 4
            }

            test("returns null for non-matching keys") {
                val map = EfficientStringKeyMap(mapOf("one" to 1, "two" to 2, "three" to 3, "four" to 4))
                map["five"].shouldBeNull()
            }

            test("size returns 4") {
                val map = EfficientStringKeyMap(mapOf("one" to 1, "two" to 2, "three" to 3, "four" to 4))
                map.size shouldBe 4
            }
        }

        context("NElements map (5+ elements)") {
            test("returns correct values for all keys") {
                val map =
                    EfficientStringKeyMap(
                        mapOf(
                            "a" to 1,
                            "bb" to 2,
                            "ccc" to 3,
                            "dddd" to 4,
                            "eeeee" to 5,
                        ),
                    )
                map["a"] shouldBe 1
                map["bb"] shouldBe 2
                map["ccc"] shouldBe 3
                map["dddd"] shouldBe 4
                map["eeeee"] shouldBe 5
            }

            test("returns null for non-matching keys") {
                val map =
                    EfficientStringKeyMap(
                        mapOf(
                            "a" to 1,
                            "bb" to 2,
                            "ccc" to 3,
                            "dddd" to 4,
                            "eeeee" to 5,
                        ),
                    )
                map["ffffff"].shouldBeNull()
                map[""].shouldBeNull()
            }

            test("returns null for key with same length but different content") {
                val map =
                    EfficientStringKeyMap(
                        mapOf(
                            "abc" to 1,
                            "def" to 2,
                            "ghi" to 3,
                            "jkl" to 4,
                            "mno" to 5,
                        ),
                    )
                map["xyz"].shouldBeNull()
            }

            test("handles many elements") {
                val entries = (1..100).associate { "key$it" to it }
                val map = EfficientStringKeyMap(entries)
                map.size shouldBe 100
                map["key1"] shouldBe 1
                map["key50"] shouldBe 50
                map["key100"] shouldBe 100
                map["key101"].shouldBeNull()
            }
        }

        context("Map interface methods") {
            test("keys returns all keys") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2, "c" to 3))
                map.keys shouldContainExactlyInAnyOrder listOf("a", "b", "c")
            }

            test("values returns all values") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2, "c" to 3))
                map.values shouldContainExactlyInAnyOrder listOf(1, 2, 3)
            }

            test("entries returns all entries") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2))
                map.entries.map { it.key to it.value } shouldContainExactlyInAnyOrder listOf("a" to 1, "b" to 2)
            }

            test("containsKey returns true for existing key") {
                val map = EfficientStringKeyMap(mapOf("exists" to "value"))
                map.containsKey("exists") shouldBe true
            }

            test("containsKey returns false for non-existing key") {
                val map = EfficientStringKeyMap(mapOf("exists" to "value"))
                map.containsKey("notexists") shouldBe false
            }

            test("containsValue returns true for existing value") {
                val map = EfficientStringKeyMap(mapOf("key" to "value"))
                map.containsValue("value") shouldBe true
            }

            test("containsValue returns false for non-existing value") {
                val map = EfficientStringKeyMap(mapOf("key" to "value"))
                map.containsValue("other") shouldBe false
            }
        }

        context("hashCode and equals") {
            test("hashCode is consistent") {
                val map1 = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2))
                val map2 = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2))
                map1.hashCode() shouldBe map2.hashCode()
            }

            test("equals returns true for equal maps") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2))
                val regularMap = mapOf("a" to 1, "b" to 2)
                (map == regularMap) shouldBe true
            }

            test("equals returns false for different maps") {
                val map = EfficientStringKeyMap(mapOf("a" to 1, "b" to 2))
                val differentMap = mapOf("a" to 1, "c" to 3)
                (map == differentMap) shouldBe false
            }
        }

        context("edge cases") {
            test("handles empty string key") {
                val map = EfficientStringKeyMap(mapOf("" to "empty"))
                map[""] shouldBe "empty"
            }

            test("handles keys with same length and different content") {
                val map = EfficientStringKeyMap(mapOf("abc" to 1, "xyz" to 2))
                map["abc"] shouldBe 1
                map["xyz"] shouldBe 2
                map["def"].shouldBeNull()
            }

            test("handles keys with special characters") {
                val map = EfficientStringKeyMap(mapOf("key.with.dots" to 1, "key-with-dashes" to 2))
                map["key.with.dots"] shouldBe 1
                map["key-with-dashes"] shouldBe 2
            }

            test("handles unicode keys") {
                val map = EfficientStringKeyMap(mapOf("ключ" to "значение", "鍵" to "値"))
                map["ключ"] shouldBe "значение"
                map["鍵"] shouldBe "値"
            }
        }

        context("Very long keys (>= 64 chars)") {
            test("falls back to PassThrough and returns correct values") {
                val longKey = "a".repeat(64)
                val map =
                    EfficientStringKeyMap(
                        mapOf(
                            longKey to "value",
                            "regular" to "regValue",
                            "other" to "otherValue",
                            "fourth" to "four",
                            "fifth" to "five",
                        ),
                    )
                map[longKey] shouldBe "value"
                map["regular"] shouldBe "regValue"
                map["other"] shouldBe "otherValue"
            }

            test("returns null for non-existing keys when long keys are present") {
                val longKey = "a".repeat(64)
                val map =
                    EfficientStringKeyMap(
                        mapOf(
                            longKey to "value",
                            "k1" to 1,
                            "k2" to 2,
                            "k3" to 3,
                            "k4" to 4,
                        ),
                    )
                map["nonexistent"].shouldBeNull()
                map["a".repeat(63)].shouldBeNull()
                map["a".repeat(65)].shouldBeNull()
            }
        }
    })
