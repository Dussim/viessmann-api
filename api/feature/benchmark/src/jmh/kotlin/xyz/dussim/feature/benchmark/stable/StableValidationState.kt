package xyz.dussim.feature.benchmark.stable

abstract class StableValidationState(
    size: StableSize,
) {
    private val fixtures = StableFixtureRepository.load(verifyManifest = true)

    val valid = fixtures[size, StableCase.VALID]
    val invalidFirst = fixtures[size, StableCase.INVALID_FIRST]
    val invalidMiddle = fixtures[size, StableCase.INVALID_MIDDLE]
    val invalidLast = fixtures[size, StableCase.INVALID_LAST]
    val invalidAll = fixtures[size, StableCase.INVALID_ALL]
    val missingProperty = fixtures[size, StableCase.MISSING_PROPERTY]
    val missingCommand = fixtures[size, StableCase.MISSING_COMMAND]
    val missingRequiredParam = fixtures[size, StableCase.MISSING_REQUIRED_PARAM]
    val wrongParamConstraintType = fixtures[size, StableCase.WRONG_PARAM_CONSTRAINT_TYPE]
    val optionalCommandAbsent = fixtures[size, StableCase.OPTIONAL_COMMAND_ABSENT]
    val optionalCommandMalformed = fixtures[size, StableCase.OPTIONAL_COMMAND_MALFORMED]
}
