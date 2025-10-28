package com.julianotalora.core.common.extensions

import org.junit.Test
import org.junit.Assert.*

class StringExtTest {

    @Test
    fun `toSearchNorm should convert to lowercase`() {
        assertEquals("hello", "HELLO".toSearchNorm())
    }

    @Test
    fun `toSearchNorm should remove diacritics`() {
        assertEquals("aeiou", "áéíóú".toSearchNorm())
        assertEquals("n", "ñ".toSearchNorm())
        assertEquals("aeiou", "äëïöü".toSearchNorm())
    }

    @Test
    fun `toSearchNorm should handle mixed case and diacritics`() {
        assertEquals("cancion", "CANCión".toSearchNorm())
    }

    @Test
    fun `toSearchNorm should handle empty string`() {
        assertEquals("", "".toSearchNorm())
    }

    @Test
    fun `toSearchNorm should handle string with no diacritics`() {
        assertEquals("hello world", "Hello World".toSearchNorm())
    }
}
