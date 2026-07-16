package xyz.dussim.viessmann.api.testing

data class FileDetails(
    val name: String,
    val content: String,
)

expect fun readFilesContentsIn(path: String): List<FileDetails>
