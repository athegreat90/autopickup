package de.alexandermora.autopickupmod;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("minecraft")
class BlacklistHelperTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void resetCache() {
        ModConfig.cacheBlacklist = Set.of();
    }

    // --- isBlacklisted ---

    @Test
    void isBlacklisted_emptyStack_returnsFalse() {
        assertFalse(BlacklistHelper.isBlacklisted(ItemStack.EMPTY));
    }

    @Test
    void isBlacklisted_emptyBlacklist_returnsFalse() {
        ModConfig.cacheBlacklist = Set.of();
        assertFalse(BlacklistHelper.isBlacklisted(Items.STONE));
    }

    @Test
    void isBlacklisted_itemInBlacklist_returnsTrue() {
        ModConfig.cacheBlacklist = Set.of(Items.DIRT);
        assertTrue(BlacklistHelper.isBlacklisted(Items.DIRT));
    }

    @Test
    void isBlacklisted_differentItemInBlacklist_returnsFalse() {
        ModConfig.cacheBlacklist = Set.of(Items.DIRT);
        assertFalse(BlacklistHelper.isBlacklisted(Items.STONE));
    }

    // --- rebuildCacheFromList ---

    @Test
    void rebuildCacheFromList_emptyList_cacheIsEmpty() {
        BlacklistHelper.rebuildCacheFromList(List.of());
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
    }

    @Test
    void rebuildCacheFromList_nullEntry_isSkipped() {
        List<String> list = new ArrayList<>();
        list.add(null);
        BlacklistHelper.rebuildCacheFromList(list);
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
    }

    @Test
    void rebuildCacheFromList_blankEntry_isSkipped() {
        BlacklistHelper.rebuildCacheFromList(List.of("   "));
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
    }

    @Test
    void rebuildCacheFromList_invalidIdCharacters_isSkipped() {
        BlacklistHelper.rebuildCacheFromList(List.of("not a valid:id!"));
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
    }

    @Test
    void rebuildCacheFromList_unknownValidId_isSkipped() {
        BlacklistHelper.rebuildCacheFromList(List.of("minecraft:unknown_item_xyz_test"));
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
    }

    @Test
    void rebuildCacheFromList_knownItem_addedToCache() {
        BlacklistHelper.rebuildCacheFromList(List.of("minecraft:dirt"));
        assertTrue(ModConfig.cacheBlacklist.contains(Items.DIRT));
    }
}
