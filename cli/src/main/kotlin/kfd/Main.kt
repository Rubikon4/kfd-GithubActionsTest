package kfd

import java.io.File
import java.io.PrintStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(runAnalyzer(args))
}

fun runAnalyzer(
    args: Array<String>,
    output: PrintStream = System.out,
    errors: PrintStream = System.err
): Int {
    if (args.size != 1) {
        errors.println("Укажите один аргумент — путь к каталогу.")
        return 1
    }

    val report = try {
        analyzeRepo(File(args[0]))
    } catch (error: IllegalArgumentException) {
        errors.println(error.message)
        return 1
    }

    output.println("По расширениям:")
    for (extension in report.extensions) {
        val fileLabel = if (extension.files == 1) "file" else "files"
        val lineLabel = if (extension.lines == 1) "line" else "lines"
        output.println("${extension.extension}: ${extension.files} $fileLabel, ${extension.lines} $lineLabel")
    }

    output.println()
    output.println("Самые длинные файлы:")
    for (file in report.longestFiles) {
        val lineLabel = if (file.lines == 1) "line" else "lines"
        output.println("${file.path}: ${file.lines} $lineLabel")
    }

    return 0
}
