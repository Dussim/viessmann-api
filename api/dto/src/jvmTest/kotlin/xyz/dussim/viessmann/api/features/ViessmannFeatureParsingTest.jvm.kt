package xyz.dussim.viessmann.api.features

import kotlin.io.path.Path
import kotlin.io.path.readText

actual fun readFileFromResources(path: String): String = Path("src/commonTest/resources/$path").readText()
