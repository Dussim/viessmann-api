// package xyz.dussim.viessmann.feature
//
// import io.kotest.assertions.throwables.shouldThrow
// import io.kotest.core.spec.style.StringSpec
// import io.kotest.matchers.shouldBe
// import io.kotest.matchers.shouldNotBe
// import io.kotest.matchers.types.shouldBeInstanceOf
// import xyz.dussim.viessmann.feature.api.BooleanConstraints
// import xyz.dussim.viessmann.feature.api.Command
// import xyz.dussim.viessmann.feature.api.Command1
// import xyz.dussim.viessmann.feature.api.Command2
// import xyz.dussim.viessmann.feature.api.Command3
// import xyz.dussim.viessmann.feature.api.Command4
// import xyz.dussim.viessmann.feature.api.Command5
// import xyz.dussim.viessmann.feature.api.Command6
// import xyz.dussim.viessmann.feature.api.Feature
// import xyz.dussim.viessmann.feature.api.NumberConstraints
// import xyz.dussim.viessmann.feature.api.Parameter
// import xyz.dussim.viessmann.feature.api.Property
// import xyz.dussim.viessmann.feature.api.StringConstraints
// import xyz.dussim.viessmann.feature.api.command
// import xyz.dussim.viessmann.feature.api.ofBoolean
// import xyz.dussim.viessmann.feature.api.ofNumber
// import xyz.dussim.viessmann.feature.api.ofString
// import kotlin.time.Instant
//
// class Command1ExtTest :
//    StringSpec({
//
//        "command with 1 parameter should create Command1" {
//            val feature = createTestFeature(1)
//
//            val testCommand by feature.command<String>(
//                constraintName1 = "param1",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<Command1<String>>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//        }
//
//        "command with 1 parameter and producer should create custom command" {
//            val feature = createTestFeature(1)
//
//            val testCommand by feature.command(
//                producer = ::CustomCommand1,
//                constraintName1 = "param1",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<CustomCommand1>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//        }
//
//        "command with 2 parameters should create Command2" {
//            val feature = createTestFeature(2)
//
//            val testCommand by feature.command<String, Double>(
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<Command2<String, Double>>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//        }
//
//        "command with 2 parameters and producer should create custom command" {
//            val feature = createTestFeature(2)
//
//            val testCommand by feature.command<String, Double, CustomCommand2>(
//                producer = ::CustomCommand2,
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<CustomCommand2>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//        }
//
//        "command with 3 parameters should create Command3" {
//            val feature = createTestFeature(3)
//
//            val testCommand by feature.command<String, Double, Boolean>(
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<Command3<String, Double, Boolean>>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//        }
//
//        "command with 3 parameters and producer should create custom command" {
//            val feature = createTestFeature(3)
//
//            val testCommand by feature.command<String, Double, Boolean, CustomCommand3>(
//                producer = ::CustomCommand3,
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<CustomCommand3>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//        }
//
//        "command with 4 parameters should create Command4" {
//            val feature = createTestFeature(4)
//
//            val testCommand by feature.command<String, Double, Boolean, String>(
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//                constraintName4 = "param4",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<Command4<String, Double, Boolean, String>>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//            testCommand.constraint4.shouldBeInstanceOf<StringConstraints>()
//        }
//
//        "command with 4 parameters and producer should create custom command" {
//            val feature = createTestFeature(4)
//
//            val testCommand by feature.command<String, Double, Boolean, String, CustomCommand4>(
//                producer = ::CustomCommand4,
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//                constraintName4 = "param4",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<CustomCommand4>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//            testCommand.constraint4.shouldBeInstanceOf<StringConstraints>()
//        }
//
//        "command with 5 parameters should create Command5" {
//            val feature = createTestFeature(5)
//
//            val testCommand by feature.command<String, Double, Boolean, String, Double>(
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//                constraintName4 = "param4",
//                constraintName5 = "param5",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<Command5<String, Double, Boolean, String, Double>>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//            testCommand.constraint4.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint5.shouldBeInstanceOf<NumberConstraints>()
//        }
//
//        "command with 5 parameters and producer should create custom command" {
//            val feature = createTestFeature(5)
//
//            val testCommand by feature.command(
//                producer = ::CustomCommand5,
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//                constraintName4 = "param4",
//                constraintName5 = "param5",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<CustomCommand5>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//            testCommand.constraint4.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint5.shouldBeInstanceOf<NumberConstraints>()
//        }
//
//        "command with 6 parameters should create Command6" {
//            val feature = createTestFeature(6)
//
//            val testCommand by feature.command<String, Double, Boolean, String, Double, Boolean>(
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//                constraintName4 = "param4",
//                constraintName5 = "param5",
//                constraintName6 = "param6",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<Command6<String, Double, Boolean, String, Double, Boolean>>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//            testCommand.constraint4.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint5.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint6.shouldBeInstanceOf<BooleanConstraints>()
//        }
//
//        "command with 6 parameters and producer should create custom command" {
//            val feature = createTestFeature(6)
//
//            val testCommand by feature.command(
//                producer = ::CustomCommand6,
//                constraintName1 = "param1",
//                constraintName2 = "param2",
//                constraintName3 = "param3",
//                constraintName4 = "param4",
//                constraintName5 = "param5",
//                constraintName6 = "param6",
//            )
//
//            testCommand.shouldNotBe(null)
//            testCommand.shouldBeInstanceOf<CustomCommand6>()
//            testCommand.command.name shouldBe "testCommand"
//            testCommand.constraint1.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint2.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint3.shouldBeInstanceOf<BooleanConstraints>()
//            testCommand.constraint4.shouldBeInstanceOf<StringConstraints>()
//            testCommand.constraint5.shouldBeInstanceOf<NumberConstraints>()
//            testCommand.constraint6.shouldBeInstanceOf<BooleanConstraints>()
//        }
//
//        "command should throw when command name not found" {
//            val feature = createTestFeature(1)
//
//            shouldThrow<IllegalArgumentException> {
//                val invalidCommand by feature.command<String>("param1")
//            }
//        }
//
//        "command should throw when parameter not found" {
//            val feature = createTestFeature(1)
//
//            shouldThrow<IllegalArgumentException> {
//                val testCommand by feature.command<String>("nonExistentParam")
//            }
//        }
//
//        "command should throw when parameter type mismatch" {
//            val feature = createTestFeature(1)
//
//            shouldThrow<IllegalArgumentException> {
//                val testCommand by feature.command<Double>("param1")
//            }
//        }
//
//        "command should throw when parameter count mismatch" {
//            val feature = createTestFeature(2)
//
//            shouldThrow<IllegalArgumentException> {
//                val testCommand by feature.command<String>("param1")
//            }
//        }
//    })
//
// // Test helper functions and classes
// private fun createTestFeature(params: Int) =
//    TestFeature(
//        commands =
//            mapOf(
//                "testCommand" to
//                    Command(
//                        uri = "test/uri",
//                        name = "testCommand",
//                        isExecutable = true,
//                        params =
//                            listOf(
//                                "param1" to Parameter.ofString(StringConstraints()),
//                                "param2" to Parameter.ofNumber(NumberConstraints()),
//                                "param3" to Parameter.ofBoolean(BooleanConstraints),
//                                "param4" to Parameter.ofString(StringConstraints()),
//                                "param5" to Parameter.ofNumber(NumberConstraints()),
//                                "param6" to Parameter.ofBoolean(BooleanConstraints),
//                            ).take(params)
//                                .toMap(),
//                    ),
//            ),
//    )
//
// private data class TestFeature(
//    override val feature: String = "test",
//    override val isEnabled: Boolean = true,
//    override val isReady: Boolean = true,
//    override val apiVersion: Int = 1,
//    override val timestamp: Instant = Instant.fromEpochMilliseconds(0),
//    override val uri: String = "test/uri",
//    override val properties: Map<String, Property> = emptyMap(),
//    override val commands: Map<String, Command>,
//    override val deviceId: String? = null,
//    override val gatewayId: String? = null,
//    override val isActive: Boolean? = null,
// ) : Feature
//
// // Custom command classes for testing producers
// private data class CustomCommand1(
//    private val wrapped: Command1<String>,
// ) : Command1<String> by wrapped
//
// private data class CustomCommand2(
//    private val wrapped: Command2<String, Double>,
// ) : Command2<String, Double> by wrapped
//
// private data class CustomCommand3(
//    private val wrapped: Command3<String, Double, Boolean>,
// ) : Command3<String, Double, Boolean> by wrapped
//
// private data class CustomCommand4(
//    private val wrapped: Command4<String, Double, Boolean, String>,
// ) : Command4<String, Double, Boolean, String> by wrapped
//
// private data class CustomCommand5(
//    private val wrapped: Command5<String, Double, Boolean, String, Double>,
// ) : Command5<String, Double, Boolean, String, Double> by wrapped
//
// private data class CustomCommand6(
//    private val wrapped: Command6<String, Double, Boolean, String, Double, Boolean>,
// ) : Command6<String, Double, Boolean, String, Double, Boolean> by wrapped
