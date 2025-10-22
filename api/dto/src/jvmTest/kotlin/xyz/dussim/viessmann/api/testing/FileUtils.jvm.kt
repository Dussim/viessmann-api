package xyz.dussim.viessmann.api.testing

import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

actual fun forEachFileContentIn(
    path: String,
    block: (String) -> Unit,
) {
    Path("src/commonTest/resources/$path").listDirectoryEntries().forEach { block(it.readText()) }
}

actual fun readFilesContentsIn(path: String): List<FileDetails> =
    Path("src/commonTest/resources/$path")
        .listDirectoryEntries()
        .filter { it.name != "very_long.json" }
        .map {
            FileDetails(it.fileName.toString(), it.readText())
        }
