package kfd

import java.io.File
import java.nio.file.Files

data class FileStats(val path: String, val extension: String, val lines: Int)

data class ExtensionStats(val extension: String, val files: Int, val lines: Int)

data class RepoReport(val extensions: List<ExtensionStats>, val longestFiles: List<FileStats>)

fun analyzeRepo(root: File): RepoReport {
    require(root.isDirectory) { "Путь не существует или не является каталогом: ${root.path}" }

    val extensions = setOf("kt", "kts", "md")
    val excludedDirectories = setOf(".git", ".gradle", "build")
    val files = root.walkTopDown()
        .onEnter { directory ->
            directory.name !in excludedDirectories && !Files.isSymbolicLink(directory.toPath())
        }
        .filter { file ->
            !Files.isSymbolicLink(file.toPath()) && file.isFile && file.extension in extensions
        }
        .map { file ->
            FileStats(
                file.relativeTo(root).invariantSeparatorsPath,
                file.extension,
                file.useLines { lines -> lines.count() }
            )
        }
        .toList()

    val byExtension = files.groupBy { it.extension }
        .map { (extension, group) ->
            var totalLines = 0
            for (file in group) {
                totalLines += file.lines
            }
            ExtensionStats(extension, group.size, totalLines)
        }
        .sortedBy { it.extension }
    val longestFiles = files.sortedWith(
        compareByDescending<FileStats> { it.lines }.thenBy { it.path }
    ).take(3)

    return RepoReport(byExtension, longestFiles)
}
