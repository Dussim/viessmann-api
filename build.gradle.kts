plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.jvm).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization).apply(false)
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "latest"
}

tasks.dependencyUpdates {
    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}