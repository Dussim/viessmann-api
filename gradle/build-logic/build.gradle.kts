plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("androidx.lint:lint-gradle:1.0.0-alpha05")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.2.21")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.1.0")
    implementation("org.jmailen.gradle:kotlinter-gradle:5.3.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:6.0.7")
}

gradlePlugin {
    plugins {
        register("kotlinCommon") {
            id = "xyz.dussim.kotlin.common"
            implementationClass = "xyz.dussim.buildlogic.KotlinCommonPlugin"
        }
    }
}
