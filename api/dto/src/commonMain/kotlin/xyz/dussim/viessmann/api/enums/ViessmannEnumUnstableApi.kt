package xyz.dussim.viessmann.api.enums

@RequiresOptIn(
    message = "Factory methods of ViessmannEnum may be removed or changed in case new enum values will not be uniquely identified by their string representation.",
    level = RequiresOptIn.Level.WARNING,
)
annotation class ViessmannEnumUnstableApi
