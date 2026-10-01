package de.alexandermora.autopickupmod

import net.neoforged.fml.config.IConfigSpec
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.mock

/**
 * Tests for [ConfigEvents].
 *
 * The "correct spec updates caches" path calls `ModConfig.PICKUP_RANGE.get()` and
 * `ModConfig.BLACKLISTED_ITEMS.get()`, which require the NeoForge config system to be
 * initialised (i.e. the spec registered and a config file loaded). Those paths are covered by the
 * in-game `runGameTestServer` integration run instead.
 *
 * The tests here verify that the identity-check guard in [ConfigEvents.refreshCaches]
 * prevents any work being done when the event belongs to a different mod's spec. They exercise the
 * internal `refreshCaches` seam directly with a mocked [IConfigSpec], so no
 * dereferencing of the `final` `net.neoforged.fml.config.ModConfig` type is required
 * (that type cannot be mocked by the subclass mock maker this project uses).
 */
@Tag("minecraft")
class ConfigEventsTest {

    @AfterEach
    fun resetConfig() {
        ModConfig.cachedPickupRange = 5.0
        ModConfig.cacheBlacklist = setOf()
    }

    @Test
    fun `refreshCaches wrong spec does not update pickup range`() {
        val otherSpec = mock(IConfigSpec::class.java)

        val before = ModConfig.cachedPickupRange
        ConfigEvents.refreshCaches(otherSpec, "loaded")

        assertEquals(before, ModConfig.cachedPickupRange,
            "cachedPickupRange must not change when the spec targets a different config")
    }

    @Test
    fun `refreshCaches wrong spec does not update blacklist`() {
        val otherSpec = mock(IConfigSpec::class.java)

        ConfigEvents.refreshCaches(otherSpec, "loaded")

        assertTrue(ModConfig.cacheBlacklist.isEmpty(),
            "cacheBlacklist must not change when the spec targets a different config")
    }

    @Test
    fun `refreshCaches wrong spec reload does not update pickup range`() {
        val otherSpec = mock(IConfigSpec::class.java)

        val before = ModConfig.cachedPickupRange
        ConfigEvents.refreshCaches(otherSpec, "reloaded")

        assertEquals(before, ModConfig.cachedPickupRange,
            "cachedPickupRange must not change when the spec targets a different config")
    }

    @Test
    fun `refreshCaches wrong spec reload does not update blacklist`() {
        val otherSpec = mock(IConfigSpec::class.java)

        ConfigEvents.refreshCaches(otherSpec, "reloaded")

        assertTrue(ModConfig.cacheBlacklist.isEmpty(),
            "cacheBlacklist must not change when the spec targets a different config")
    }
}