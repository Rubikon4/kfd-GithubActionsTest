package kfd

import java.io.File
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class RepoAnalyzerTest {
    @TempDir
    lateinit var root: File

    @Test
    fun countsFilesAndLinesByExtension() {
        createSample(root)

        assertEquals(
            listOf(ExtensionStats("kt", 2, 4), ExtensionStats("md", 1, 0)),
            analyzeRepo(root).extensions
        )
    }

    @Test
    fun findsLongestFilesInSample() {
        createSample(root)

        assertEquals(
            listOf(
                FileStats("src/A.kt", "kt", 2),
                FileStats("src/B.kt", "kt", 2),
                FileStats("README.md", "md", 0)
            ),
            analyzeRepo(root).longestFiles
        )
    }

    @Test
    fun handlesEmptyDirectory() {
        val report = analyzeRepo(root)

        assertEquals(emptyList<ExtensionStats>(), report.extensions)
        assertEquals(emptyList<FileStats>(), report.longestFiles)
    }

    @Test
    fun rejectsMissingPath() {
        assertThrows(IllegalArgumentException::class.java) {
            analyzeRepo(File(root, "missing"))
        }
    }

    @Test
    fun rejectsFileInsteadOfDirectory() {
        val file = File(root, "A.kt")
        file.writeText("class A")

        assertThrows(IllegalArgumentException::class.java) {
            analyzeRepo(file)
        }
    }

    @Test
    fun includesKotlinScriptsAndSortsExtensions() {
        File(root, "README.md").writeText("Description")
        File(root, "build.gradle.kts").writeText("plugins {}")
        File(root, "A.kt").writeText("class A")

        assertEquals(
            listOf(
                ExtensionStats("kt", 1, 1),
                ExtensionStats("kts", 1, 1),
                ExtensionStats("md", 1, 1)
            ),
            analyzeRepo(root).extensions
        )
    }

    @Test
    fun limitsLongestFilesToThree() {
        File(root, "b.kt").writeText("one")
        File(root, "c.kts").writeText("one\ntwo")
        File(root, "a.md").writeText("one\ntwo\nthree")
        File(root, "z.kt").writeText("one\ntwo\nthree\nfour")

        assertEquals(
            listOf(
                FileStats("z.kt", "kt", 4),
                FileStats("a.md", "md", 3),
                FileStats("c.kts", "kts", 2)
            ),
            analyzeRepo(root).longestFiles
        )
    }

    @Test
    fun sortsEqualLengthFilesByPath() {
        File(root, "z.kt").writeText("class Z")
        File(root, "a.kt").writeText("class A")

        assertEquals(
            listOf(FileStats("a.kt", "kt", 1), FileStats("z.kt", "kt", 1)),
            analyzeRepo(root).longestFiles
        )
    }

    @Test
    fun returnsAllFilesWhenThereAreFewerThanThree() {
        File(root, "A.kt").writeText("class A")

        assertEquals(listOf(FileStats("A.kt", "kt", 1)), analyzeRepo(root).longestFiles)
    }

    @Test
    fun skipsExcludedDirectoriesAndOtherExtensions() {
        for (name in listOf(".git", ".gradle", "build")) {
            val directory = File(root, "nested/$name")
            directory.mkdirs()
            File(directory, "ignored.kt").writeText("ignored")
        }
        File(root, "notes.txt").writeText("ignored")
        File(root, "A.kt").writeText("class A")

        val report = analyzeRepo(root)

        assertEquals(listOf(ExtensionStats("kt", 1, 1)), report.extensions)
        assertEquals(listOf(FileStats("A.kt", "kt", 1)), report.longestFiles)
    }

    @Test
    fun skipsSymbolicLinkToFile() {
        val file = File(root, "A.kt")
        file.writeText("class A")
        Files.createSymbolicLink(File(root, "link.kt").toPath(), file.toPath())

        val report = analyzeRepo(root)

        assertEquals(listOf(ExtensionStats("kt", 1, 1)), report.extensions)
        assertEquals(listOf(FileStats("A.kt", "kt", 1)), report.longestFiles)
    }

    @Test
    fun skipsSymbolicLinkToDirectory() {
        val directory = File(root, "src")
        directory.mkdirs()
        File(directory, "A.kt").writeText("class A")
        Files.createSymbolicLink(File(root, "linked").toPath(), directory.toPath())

        val report = analyzeRepo(root)

        assertEquals(listOf(ExtensionStats("kt", 1, 1)), report.extensions)
        assertEquals(listOf(FileStats("src/A.kt", "kt", 1)), report.longestFiles)
    }
}

internal fun createSample(root: File) {
    val source = File(root, "src")
    source.mkdirs()
    File(source, "A.kt").writeText("package sample\nclass A")
    File(source, "B.kt").writeText("package sample\nclass B\n")
    File(root, "README.md").writeText("")
    File(root, "notes.txt").writeText("ignored")
}
