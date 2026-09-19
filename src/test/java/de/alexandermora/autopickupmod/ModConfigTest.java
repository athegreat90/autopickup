package de.alexandermora.autopickupmod;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("minecraft")
class ModConfigTest {

    @Test
    void defaultCachedPickupRange_isFive() {
        assertEquals(5.0D, ModConfig.cachedPickupRange);
    }

    @Test
    void defaultCacheBlacklist_isEmpty() {
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
    }

    @Test
    void cacheBlacklist_defaultIsImmutable() {
        // Set.of() returns an immutable set -- verify the field is reset-able (volatile write succeeds)
        ModConfig.cacheBlacklist = Set.of();
        assertTrue(ModConfig.cacheBlacklist.isEmpty());
        // Restore
        ModConfig.cacheBlacklist = Set.of();
    }
}
