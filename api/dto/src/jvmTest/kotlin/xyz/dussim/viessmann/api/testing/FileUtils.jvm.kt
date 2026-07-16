package xyz.dussim.viessmann.api.testing

import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

actual fun readFilesContentsIn(path: String): List<FileDetails> =
    Path("src/commonTest/resources/$path")
        .listDirectoryEntries()
        .map {
            FileDetails(it.fileName.toString(), it.readText())
        }
