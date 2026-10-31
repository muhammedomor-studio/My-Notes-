package com.example

import com.example.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test pin hashing and salt uniqueness`() {
        val salt1 = SecurityManager.generateSalt()
        val salt2 = SecurityManager.generateSalt()
        assertNotEquals(salt1, salt2)

        val hash1 = SecurityManager.hashPin("1234", salt1)
        val hash1Again = SecurityManager.hashPin("1234", salt1)
        assertEquals(hash1, hash1Again)

        val hash2 = SecurityManager.hashPin("1234", salt2)
        assertNotEquals(hash1, hash2)
    }
}
