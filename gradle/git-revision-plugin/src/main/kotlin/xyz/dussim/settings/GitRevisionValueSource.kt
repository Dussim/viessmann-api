@file:Suppress("unused")

package xyz.dussim.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class GitRevisionValueSource
    @Inject
    constructor(
        private val execOperations: ExecOperations,
    ) : ValueSource<String, ValueSourceParameters.None> {
        override fun obtain(): String {
            val output = ByteArrayOutputStream()
            execOperations.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = output
            }
            return output.toString()
        }
    }

class GitRevisionValuePlugin : Plugin<Settings> {
    override fun apply(target: Settings) = Unit
}
