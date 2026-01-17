package xyz.dussim.viessmann.api.feature.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class CommandName(
    val name: String,
)
