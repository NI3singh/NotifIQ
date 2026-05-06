package com.notifiq.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `sha256 produces consistent hashes for same input`() {
        val input = "com.whatsapp"
        val hash1 = HashUtils.sha256(input)
        val hash2 = HashUtils.sha256(input)

        assertEquals("Same input should produce same hash", hash1, hash2)
    }

    @Test
    fun `sha256 handles empty string`() {
        val hash = HashUtils.sha256("")

        // SHA-256 of empty string is a well-known value
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            hash
        )
    }

    @Test
    fun `sha256 produces different hashes for different inputs`() {
        val hash1 = HashUtils.sha256("com.whatsapp")
        val hash2 = HashUtils.sha256("com.instagram.android")

        assertNotEquals("Different inputs should produce different hashes", hash1, hash2)
    }

    @Test
    fun `sha256 produces 64 character hex string`() {
        val hash = HashUtils.sha256("test input")

        assertEquals("SHA-256 hash should be 64 characters", 64, hash.length)
        assertEquals("Hash should only contain hex characters", hash, hash.lowercase())
    }
}