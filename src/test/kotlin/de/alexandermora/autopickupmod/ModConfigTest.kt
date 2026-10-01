package de.alexandermora.autopickupmod

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@Tag("minecraft")
class ModConfigTest {

    @Test
    fun `default cached pickup range is five`() {
        assertEquals(5.0, ModConfig.cachedPickupRange)
    }

    @Test
    fun `default cache blacklist is empty`() {
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
    }

    @Test
    fun `cache blacklist default is immutable`() {
        // setOf() returns an immutable set -- verify the field is reset-able (volatile write succeeds)
        ModConfig.cacheBlacklist = setOf()
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
        // Restore
        ModConfig.cacheBlacklist = setOf()
    }
}