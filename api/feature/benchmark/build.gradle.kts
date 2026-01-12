plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.jvm.common)
    alias(libs.plugins.ksp)
    id("me.champeau.jmh") version "0.7.3"
    id("xyz.dussim.jmhreport") version "0.10.1"
}

dependencies {
    implementation(projects.api.feature.annotations)

    implementation(projects.api.feature.common)

    ksp(projects.api.feature.processor)
}

jmh {
    jmhVersion.set("1.37")
    // JVM arguments
    jvmArgs.addAll("-Xms2g", "-Xmx2g", "-XX:+UseCompactObjectHeaders", "-XX:+UnlockExperimentalVMOptions", "-XX:+TrustFinalNonStaticFields")

    // GC profiling
    profilers.addAll("gc")

    // Result file format: json, csv, text
    resultFormat.set("json")

    // Output file for results
    resultsFile.set(layout.buildDirectory.file("reports/jmh/results.json"))
//    humanOutputFile.set(layout.buildDirectory.file("reports/jmh/human.txt"))

    // Fail build on error
    failOnError.set(true)

    // Verbose output
    verbosity.set("NORMAL")

    // exclude map benchmark as I already established performance
//    excludes.addAll(".*MapBenchmark.*")
}

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}

group = "xyz.dussim.feature.benchmark"
