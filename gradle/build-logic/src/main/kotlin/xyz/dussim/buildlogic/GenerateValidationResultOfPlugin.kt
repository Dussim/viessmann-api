package xyz.dussim.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.work.DisableCachingByDefault
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

abstract class GenerateValidationResultOfPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        target.run {
            val extension = extensions.create<GenerateValidationResultOfExtension>("generateValidationResultOf")
            extension.maxArity.convention(16)
            val generateTask =
                tasks.register(
                    "generateValidationResultOf",
                    GenerateValidationResultOfTask::class.java,
                ) {
                    maxArity = extension.maxArity
                    validationFile = extension.validationFile
                }
            tasks.withType(FormatTask::class.java).configureEach {
                mustRunAfter(generateTask)
            }
            tasks.withType(LintTask::class.java).configureEach {
                mustRunAfter(generateTask)
            }
        }
}

abstract class GenerateValidationResultOfExtension {
    abstract val maxArity: Property<Int>

    abstract val validationFile: RegularFileProperty
}

@DisableCachingByDefault
abstract class GenerateValidationResultOfTask : DefaultTask() {
    @get:Input
    abstract val maxArity: Property<Int>

    @get:Internal
    abstract val validationFile: RegularFileProperty

    init {
        group = "build"
        description = "Generates ValidationResult.of overloads for combining multiple results"
    }

    @TaskAction
    fun generate() {
        val max = maxArity.get()
        require(max in 2..32) { "maxArity must be between 2 and 32, got $max" }

        val file = validationFile.get().asFile
        val content = file.readText()

        val regionStart = "// region generated-validation-result-of"
        val regionEnd = "// endregion generated-validation-result-of"

        val startIndex = content.indexOf(regionStart)
        val endIndex = content.indexOf(regionEnd)

        require(startIndex >= 0) { "Could not find '$regionStart' in ${file.name}" }
        require(endIndex >= 0) { "Could not find '$regionEnd' in ${file.name}" }
        require(endIndex > startIndex) { "Region end marker must come after start marker" }

        val generated = generateOverloads(max)

        val before = content.substring(0, startIndex + regionStart.length)
        val after = content.substring(endIndex)

        file.writeText(before + "\n" + generated + "            " + after)

        logger.lifecycle("Generated ValidationResult.of overloads for arities 2..$max")
    }

    private fun generateOverloads(maxArity: Int): String =
        buildString {
            for (n in 2..maxArity) {
                appendOverload(n)
                if (n < maxArity) appendLine()
            }
        }

    private fun StringBuilder.appendOverload(n: Int) {
        val indent = "            "
        val bodyIndent = "$indent    "

        // Function signature
        appendLine("${indent}fun <E> of(")
        for (i in 1..n) {
            appendLine("${bodyIndent}result$i: ValidationResult<E>,")
        }
        appendLine("$indent): ValidationResult<E> {")

        // Bitmask
        appendLine("${bodyIndent}val mask =")
        appendLine("$bodyIndent    result1.invalidFlag or")
        for (i in 2..n) {
            val trailing = if (i < n) " or" else ""
            appendLine("$bodyIndent        (result$i.invalidFlag shl ${i - 1})$trailing")
        }
        appendLine()

        // Early return if all valid
        appendLine("${bodyIndent}if (mask == 0) return Valid")
        appendLine()

        // Single-invalid short-circuit
        appendLine("${bodyIndent}if (mask.countOneBits() == 1) {")
        appendLine("$bodyIndent    when (mask.countTrailingZeroBits()) {")
        for (i in 1..n) {
            appendLine("$bodyIndent        ${i - 1} -> return result$i")
        }
        appendLine("$bodyIndent    }")
        appendLine("$bodyIndent}")
        appendLine()

        // Array allocation
        appendLine("${bodyIndent}var current = 0")
        appendLine("${bodyIndent}val array =")
        appendLine("$bodyIndent    arrayOfNulls<Any>(")
        for (i in 1..n) {
            val trailing = if (i < n) " +" else ","
            val lineIndent = if (i == 1) "$bodyIndent        " else "$bodyIndent            "
            appendLine("${lineIndent}result$i.size$trailing")
        }
        appendLine("$bodyIndent    )")

        // Copy errors
        for (i in 1..n) {
            appendLine("${bodyIndent}result$i.forEach { array[current++] = it }")
        }

        // Return
        appendLine("${bodyIndent}return Invalid(array as Array<E>)")
        appendLine("$indent}")
    }
}
