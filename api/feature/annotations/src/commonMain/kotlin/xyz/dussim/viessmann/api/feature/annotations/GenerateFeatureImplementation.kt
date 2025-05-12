package xyz.dussim.viessmann.api.feature.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateFeatureImplementation(
    val featureName: String,
)
