package xyz.dussim.viessmann.feature.api

@RequiresOptIn(
    message = "GeneratedAccessException is specific stack less, singleton exception with intended internal usage but public so that JVM can optimize as much as possible its usage",
    level = RequiresOptIn.Level.ERROR,
)
annotation class ViessmannApiInternalExceptionUsage
