plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.jvm.common)
    alias(libs.plugins.ksp)
}

dependencies {
    api(projects.api.feature.annotations)

    implementation(projects.api.feature.common)

    implementation(libs.ksp.symbol.processing.api)
    implementation(libs.auto.service.annotations)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ktlint.rule.engine)
    implementation(libs.ktlint.ruleset.standard)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.nop)

    ksp(libs.auto.service.ksp)
}
