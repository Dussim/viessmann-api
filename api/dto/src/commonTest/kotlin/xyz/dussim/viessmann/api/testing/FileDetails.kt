package xyz.dussim.viessmann.api.testing

import io.kotest.engine.names.WithDataTestName

data class FileDetails(
    val name: String,
    val content: String,
) : WithDataTestName {
    override fun dataTestName(): String = name
}

expect fun readFilesContentsIn(path: String): List<FileDetails>
