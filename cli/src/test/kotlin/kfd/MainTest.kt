package kfd

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class MainTest {
    @TempDir
    lateinit var root: File

    private val output = ByteArrayOutputStream()
    private val errors = ByteArrayOutputStream()

    @Test
    fun printsBothReports() {
        createSample(root)

        assertEquals(0, run(root.path))
        assertEquals(
            """
            По расширениям:
            kt: 2 files, 4 lines
            md: 1 file, 0 lines

            Самые длинные файлы:
            src/A.kt: 2 lines
            src/B.kt: 2 lines
            README.md: 0 lines
            """.trimIndent() + "\n",
            output.toString(Charsets.UTF_8).replace("\r\n", "\n")
        )
        assertEquals("", errors.toString(Charsets.UTF_8))
    }

    @Test
    fun printsEmptyReports() {
        assertEquals(0, run(root.path))
        assertEquals(
            "По расширениям:\n\nСамые длинные файлы:\n",
            output.toString(Charsets.UTF_8).replace("\r\n", "\n")
        )
    }

    @Test
    fun rejectsMissingArgument() {
        assertEquals(1, run())
        assertTrue(errors.toString(Charsets.UTF_8).contains("Укажите один аргумент"))
        assertEquals("", output.toString(Charsets.UTF_8))
    }

    @Test
    fun rejectsExtraArgument() {
        assertEquals(1, run(root.path, root.path))
        assertTrue(errors.toString(Charsets.UTF_8).contains("Укажите один аргумент"))
    }

    @Test
    fun rejectsMissingPath() {
        val path = File(root, "missing").path

        assertEquals(1, run(path))
        assertTrue(errors.toString(Charsets.UTF_8).contains(path))
        assertEquals("", output.toString(Charsets.UTF_8))
    }

    @Test
    fun rejectsFileInsteadOfDirectory() {
        val file = File(root, "A.kt")
        file.writeText("class A")

        assertEquals(1, run(file.path))
        assertTrue(errors.toString(Charsets.UTF_8).contains(file.path))
        assertEquals("", output.toString(Charsets.UTF_8))
    }

    private fun createSample(root: File) {
        val source = File(root, "src")
        source.mkdirs()
        File(source, "A.kt").writeText("package sample\nclass A")
        File(source, "B.kt").writeText("package sample\nclass B\n")
        File(root, "README.md").writeText("")
        File(root, "notes.txt").writeText("ignored")
    }

    private fun run(vararg args: String): Int = runAnalyzer(
        arrayOf(*args),
        PrintStream(output, true, Charsets.UTF_8),
        PrintStream(errors, true, Charsets.UTF_8)
    )
}
