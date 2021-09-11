package me.jhan.file.toy.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PathUtilTest {
    val path = "hoho/test"
    val file = "test.txt"

    @Test
    fun splitFileTest() {
        val fullPath = "$path/$file"
        val result = PathUtil.splitFile(fullPath)
        assertEquals(path, result.first)
        assertEquals(file, result.second)
    }

    @Test
    fun splitFileDuplicatedSlashTest() {
        val fullPath = "$path//$file"
        val result = PathUtil.splitFile(fullPath)
        assertEquals(path, result.first)
        assertEquals(file, result.second)
    }

    @Test
    fun splitFileOnlyFileTest() {
        val fullPath = file
        val result = PathUtil.splitFile(fullPath)
        assertEquals("", result.first)
        assertEquals(file, result.second)
    }

    @Test
    fun splitFileNonPathTest() {
        val fullPath = ""

        assertThrows<IllegalArgumentException> { PathUtil.splitFile(fullPath) }
    }


}