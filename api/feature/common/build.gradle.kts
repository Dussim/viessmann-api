plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.validation.result.of)
}

generateValidationResultOf {
    maxArity = 16
    validationFile =
        layout.projectDirectory.file(
            "src/commonMain/kotlin/xyz.dussim.viessmann.feature.api/validation/Validation.kt",
        )
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.serialization.json)
    }
}
