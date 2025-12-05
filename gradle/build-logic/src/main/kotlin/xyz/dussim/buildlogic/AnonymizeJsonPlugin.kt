package xyz.dussim.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AnonymizeJsonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.register("anonymizeJsonVerify", AnonymizeJsonTask::class.java) {
                group = "verification"
                description = "Verifies that all JSON files are anonymized; fails if any changes would be made."
            }

            tasks.register("anonymizeJsonApply", AnonymizeJsonTask::class.java) {
                group = "maintenance"
                description = "Applies anonymization to JSON files in-place."
                applyChanges.convention(true)
            }
        }
    }
}
