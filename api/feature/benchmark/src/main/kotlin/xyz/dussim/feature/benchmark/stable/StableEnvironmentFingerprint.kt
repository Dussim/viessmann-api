package xyz.dussim.feature.benchmark.stable

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.security.MessageDigest

object StableEnvironmentFingerprint {
    private val benchmarkJvmArgs =
        listOf(
            "-Xms2g",
            "-Xmx2g",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+UseCompactObjectHeaders",
            "-XX:+TrustFinalNonStaticFields",
            "--sun-misc-unsafe-memory-access=allow",
        )

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 2) { "Expected manifest and output paths" }
        val manifest = File(args[0])
        val output = File(args[1])
        val fingerprint =
            buildJsonObject {
                put("commit", gitCommit())
                put("fixtureVersion", "stable-validation-v1")
                put("fixtureManifestSha256", sha256(manifest.readBytes()))
                put("javaVersion", System.getProperty("java.version"))
                put("javaVendor", System.getProperty("java.vendor"))
                put("javaHome", System.getProperty("java.home"))
                put("osName", System.getProperty("os.name"))
                put("osVersion", System.getProperty("os.version"))
                put("osArch", System.getProperty("os.arch"))
                put("cpu", cpuModel())
                put("jvmArgs", buildJsonArray { benchmarkJvmArgs.forEach { add(JsonPrimitive(it)) } })
                put("forks", 3)
                put("warmupIterations", 3)
                put("measurementIterations", 5)
                put("iterationSeconds", 1)
                put("threads", 1)
                put("operationsPerInvocation", 1)
            }

        output.parentFile.mkdirs()
        output.writeText(
            Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), fingerprint) + "\n",
        )
    }

    private fun gitCommit(): String =
        runCatching {
            ProcessBuilder("git", "rev-parse", "HEAD")
                .redirectErrorStream(true)
                .start()
                .let { process ->
                    val output =
                        process.inputStream
                            .bufferedReader()
                            .use { it.readText() }
                            .trim()
                    check(process.waitFor() == 0) { output }
                    output
                }
        }.getOrElse { "unknown" }

    private fun cpuModel(): String =
        System.getenv("PROCESSOR_IDENTIFIER")
            ?: runCatching {
                File("/proc/cpuinfo")
                    .useLines { lines ->
                        lines.first { it.startsWith("model name") }.substringAfter(':').trim()
                    }
            }.getOrElse { "unknown" }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { byte -> "%02x".format(byte) }
}
