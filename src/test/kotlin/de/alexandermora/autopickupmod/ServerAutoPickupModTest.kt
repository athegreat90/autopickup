package de.alexandermora.autopickupmod

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ServerAutoPickupModTest {

    @Test
    fun `modid constant matches expected value`() {
        assertEquals("autopickupmod", ServerAutoPickupMod.MODID)
    }
}