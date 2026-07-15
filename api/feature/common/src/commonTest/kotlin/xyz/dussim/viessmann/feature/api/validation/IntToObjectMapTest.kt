package xyz.dussim.viessmann.feature.api.validation

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val IntToObjectMapTest by testSuite {
    testSuite("Empty map") {
        test("returns null for any key") {
            val map = IntToObjectMap.of<String>()
            map[0].shouldBeNull()
            map[1].shouldBeNull()
            map[100].shouldBeNull()
            map[-1].shouldBeNull()
        }
    }

    testSuite("Map with 1 element") {
        test("returns value for matching key") {
            val map = IntToObjectMap.of(5, "five", IntToObjectMap.of())
            map[5] shouldBe "five"
        }

        test("returns null for non-matching key") {
            val map = IntToObjectMap.of(5, "five", IntToObjectMap.of())
            map[0].shouldBeNull()
            map[4].shouldBeNull()
            map[6].shouldBeNull()
        }
    }

    testSuite("Map with 2 elements") {
        test("returns correct values for both keys") {
            val map1 = IntToObjectMap.of(5, "five", IntToObjectMap.of<String>())
            val map2 = IntToObjectMap.of(10, "ten", map1)
            map2[5] shouldBe "five"
            map2[10] shouldBe "ten"
        }

        test("returns null for non-matching keys") {
            val map1 = IntToObjectMap.of(5, "five", IntToObjectMap.of<String>())
            val map2 = IntToObjectMap.of(10, "ten", map1)
            map2[0].shouldBeNull()
            map2[7].shouldBeNull()
            map2[15].shouldBeNull()
        }
    }

    testSuite("Map with 3+ elements (IntToObjectMapN)") {
        test("returns correct values for all keys") {
            val map1 = IntToObjectMap.of(1, "one", IntToObjectMap.of<String>())
            val map2 = IntToObjectMap.of(2, "two", map1)
            val map3 = IntToObjectMap.of(3, "three", map2)
            map3[1] shouldBe "one"
            map3[2] shouldBe "two"
            map3[3] shouldBe "three"
        }

        test("returns null for non-matching keys") {
            val map1 = IntToObjectMap.of(1, "one", IntToObjectMap.of<String>())
            val map2 = IntToObjectMap.of(2, "two", map1)
            val map3 = IntToObjectMap.of(3, "three", map2)
            map3[0].shouldBeNull()
            map3[4].shouldBeNull()
        }

        test("handles additional elements beyond 3") {
            var map: IntToObjectMap<String> = IntToObjectMap.of()
            for (i in 1..10) {
                map = IntToObjectMap.of(i, "value$i", map)
            }
            for (i in 1..10) {
                map[i] shouldBe "value$i"
            }
            map[0].shouldBeNull()
            map[11].shouldBeNull()
        }
    }

    testSuite("Map with different value types") {
        test("works with Int values") {
            val map = IntToObjectMap.of(1, 100, IntToObjectMap.of())
            map[1] shouldBe 100
        }

        test("works with nullable values") {
            val map1 = IntToObjectMap.of<String?>(1, null, IntToObjectMap.of())
            val map2 = IntToObjectMap.of<String?>(2, "value", map1)
            map1[1] shouldBe null
            map2[2] shouldBe "value"
        }

        test("works with complex objects") {
            data class Person(
                val name: String,
                val age: Int,
            )

            val map = IntToObjectMap.of(1, Person("Alice", 30), IntToObjectMap.of())
            map[1] shouldBe Person("Alice", 30)
        }
    }

    testSuite("edge cases") {
        test("handles negative keys") {
            val map = IntToObjectMap.of(-5, "negative", IntToObjectMap.of())
            map[-5] shouldBe "negative"
            map[5].shouldBeNull()
        }

        test("handles zero key") {
            val map = IntToObjectMap.of(0, "zero", IntToObjectMap.of())
            map[0] shouldBe "zero"
        }

        test("handles large keys") {
            val map = IntToObjectMap.of(Int.MAX_VALUE, "max", IntToObjectMap.of())
            map[Int.MAX_VALUE] shouldBe "max"
        }

        test("handles minimum Int key") {
            val map = IntToObjectMap.of(Int.MIN_VALUE, "min", IntToObjectMap.of())
            map[Int.MIN_VALUE] shouldBe "min"
        }
    }

    testSuite("updating existing keys in MapN") {
        test("can update value for existing key in MapN") {
            val map1 = IntToObjectMap.of(1, "one", IntToObjectMap.of<String>())
            val map2 = IntToObjectMap.of(2, "two", map1)
            val map3 = IntToObjectMap.of(3, "three", map2)
            // Now map3 is a MapN
            val map4 = IntToObjectMap.of(1, "ONE", map3) // Update key 1
            map4[1] shouldBe "ONE"
            map4[2] shouldBe "two"
            map4[3] shouldBe "three"
        }
    }
}
