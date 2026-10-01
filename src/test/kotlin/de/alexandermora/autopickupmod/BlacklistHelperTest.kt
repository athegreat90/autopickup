package de.alexandermora.autopickupmod

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@Tag("minecraft")
class BlacklistHelperTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @AfterEach
    fun resetCache() {
        ModConfig.cacheBlacklist = setOf()
    }

    // --- isBlacklisted ---

    @Test
    fun `isBlacklisted empty stack returns false`() {
        assertFalse(BlacklistHelper.isBlacklisted(ItemStack.EMPTY))
    }

    @Test
    fun `isBlacklisted empty blacklist returns false`() {
        ModConfig.cacheBlacklist = setOf()
        assertFalse(BlacklistHelper.isBlacklisted(Items.STONE))
    }

    @Test
    fun `isBlacklisted item in blacklist returns true`() {
        ModConfig.cacheBlacklist = setOf(Items.DIRT)
        assertTrue(BlacklistHelper.isBlacklisted(Items.DIRT))
    }

    @Test
    fun `isBlacklisted different item in blacklist returns false`() {
        ModConfig.cacheBlacklist = setOf(Items.DIRT)
        assertFalse(BlacklistHelper.isBlacklisted(Items.STONE))
    }

    // --- rebuildCacheFromList ---

    @Test
    fun `rebuildCacheFromList empty list cache is empty`() {
        BlacklistHelper.rebuildCacheFromList(listOf())
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
    }

    @Test
    fun `rebuildCacheFromList null entry is skipped`() {
        val list = mutableListOf<String?>()
        list.add(null)
        BlacklistHelper.rebuildCacheFromList(list)
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
    }

    @Test
    fun `rebuildCacheFromList blank entry is skipped`() {
        BlacklistHelper.rebuildCacheFromList(listOf("   "))
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
    }

    @Test
    fun `rebuildCacheFromList invalid id characters is skipped`() {
        BlacklistHelper.rebuildCacheFromList(listOf("not a valid:id!"))
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
    }

    @Test
    fun `rebuildCacheFromList unknown valid id is skipped`() {
        BlacklistHelper.rebuildCacheFromList(listOf("minecraft:unknown_item_xyz_test"))
        assertTrue(ModConfig.cacheBlacklist.isEmpty())
    }

    @Test
    fun `rebuildCacheFromList known item added to cache`() {
        BlacklistHelper.rebuildCacheFromList(listOf("minecraft:dirt"))
        assertTrue(ModConfig.cacheBlacklist.contains(Items.DIRT))
    }
}