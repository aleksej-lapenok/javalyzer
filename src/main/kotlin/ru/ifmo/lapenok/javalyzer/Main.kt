package ru.ifmo.lapenok.javalyzer

import com.github.javaparser.Range
import com.github.javaparser.utils.Log
import com.github.javaparser.utils.SourceRoot
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class Main {
    fun main(args: Array<String>) {
        if (args.size != 2) {
            print("Use args: <path to root of project>, <base package>")
            exitProcess(1)
        }

        Log.setAdapter(Log.StandardOutStandardErrorAdapter())

        val errors = processFiles(Paths.get(args[0]).toAbsolutePath(), args[1])

        println("Errors fount in ${errors}")
    }

    fun processFiles(sourceRootPath: Path, basePackage: String): List<Range> {
        val sourceRoot = SourceRoot(sourceRootPath)

        val cu = sourceRoot.tryToParse(basePackage)

        if (cu.any { !it.isSuccessful }) {
            val withProblems = cu.filter { !it.isSuccessful }

            print(withProblems.map { it.problems })
            return emptyList()
        } else {
            val visitor = object : TreeStructVisitor() {}
            cu.forEach {
                it.result.get().accept(visitor, State())
            }
            return visitor.errors.toList()
        }
    }
}

fun main(args: Array<String>) = Main().main(args)


