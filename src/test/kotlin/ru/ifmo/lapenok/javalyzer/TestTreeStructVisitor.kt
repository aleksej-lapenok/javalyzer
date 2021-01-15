package ru.ifmo.lapenok.javalyzer

import com.github.javaparser.Range
import org.junit.jupiter.api.Test
import com.github.javaparser.utils.CodeGenerationUtils
import com.github.javaparser.utils.Log
import com.google.common.io.Resources
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll

import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path


class TestTreeStructVisitor {
    companion object {
        @BeforeAll
        fun init() {
            Log.setAdapter(Log.StandardOutStandardErrorAdapter())
        }
    }

    @Test
    fun test1() {

        assertEquals(
            Main().processFiles(getFile("/test1/Test.java"), ""),
            listOf<Range>(
                Range.range(24,28,26,9),
                Range.range(38,28,40,9),
                Range.range(11,21,14,9)
            )
        )

    }

    @Test
    fun test2() {
        try {
            assertEquals(
                Main().processFiles(getFile("/test2/Blabla.java"), ""),
                listOf<Range>()
            )
            fail<Nothing>()
        } catch (e:IllegalArgumentException) {
            //ok
        }
    }

    @Test
    fun test3() {
        assertEquals(
            Main().processFiles(getFile("/test3/Test.java"), ""),
            listOf<Range>(
                Range.range(14,16,16,9)
            )
        )
    }

    @Test
    fun test4() {
        assertEquals(
        Main().processFiles(getFile("/test4/Test.java"), ""),
            listOf<Range>()
        )
    }

    private fun getFile(name:String):Path {
        val tmpDirectory = Files.createTempDirectory("test")

        val imageResource = javaClass.getResource(name)
        val imageFile = File.createTempFile(
            FilenameUtils.getBaseName(imageResource.getFile()),
            "." + FilenameUtils.getExtension(imageResource.getFile()),
            tmpDirectory.toFile()
        )
        IOUtils.copy(
            imageResource.openStream(),
            FileUtils.openOutputStream(imageFile)
        )
        return tmpDirectory
    }
}
