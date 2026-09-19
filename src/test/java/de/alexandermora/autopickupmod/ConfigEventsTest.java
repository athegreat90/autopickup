package de.alexandermora.autopickupmod;

import net.neoforged.fml.config.IConfigSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ConfigEvents}.
 *
 * <p>The "correct spec updates caches" path calls {@code ModConfig.PICKUP_RANGE.get()} and
 * {@code ModConfig.BLACKLISTED_ITEMS.get()}, which require the NeoForge config system to be
 * initialised (i.e. the spec registered and a config file loaded). Those paths are covered by the
 * in-game {@code runGameTestServer} integration run instead.
 *
 * <p>The tests here verify that the identity-check guard in {@link ConfigEvents#refreshCaches}
 * prevents any work being done when the event belongs to a different mod's spec. They exercise the
 * package-private {@code refreshCaches} seam directly with a mocked {@link IConfigSpec}, so no
 * dereferencing of the {@code final} {@code net.neoforged.fml.config.ModConfig} type is required
 * (that type cannot be mocked by the subclass mock maker this project uses).
 */
@Tag("minecraft")
class ConfigEventsTest {

    @AfterEach
    void resetConfig() {
        ModConfig.cachedPickupRange = 5.0D;
        ModConfig.cacheBlacklist = Set.of();
    }

    @Test
    void refreshCaches_wrongSpec_doesNotUpdatePickupRange() {
        IConfigSpec otherSpec = mock(IConfigSpec.class);

        double before = ModConfig.cachedPickupRange;
        ConfigEvents.refreshCaches(otherSpec, "loaded");

        assertEquals(before, ModConfig.cachedPickupRange,
                "cachedPickupRange must not change when the spec targets a different config");
    }

    @Test
    void refreshCaches_wrongSpec_doesNotUpdateBlacklist() {
        IConfigSpec otherSpec = mock(IConfigSpec.class);

        ConfigEvents.refreshCaches(otherSpec, "loaded");

        assertTrue(ModConfig.cacheBlacklist.isEmpty(),
                "cacheBlacklist must not change when the spec targets a different config");
    }

    @Test
    void refreshCaches_wrongSpec_reload_doesNotUpdatePickupRange() {
        IConfigSpec otherSpec = mock(IConfigSpec.class);

        double before = ModConfig.cachedPickupRange;
        ConfigEvents.refreshCaches(otherSpec, "reloaded");

        assertEquals(before, ModConfig.cachedPickupRange,
                "cachedPickupRange must not change when the spec targets a different config");
    }

    @Test
    void refreshCaches_wrongSpec_reload_doesNotUpdateBlacklist() {
        IConfigSpec otherSpec = mock(IConfigSpec.class);

        ConfigEvents.refreshCaches(otherSpec, "reloaded");

        assertTrue(ModConfig.cacheBlacklist.isEmpty(),
                "cacheBlacklist must not change when the spec targets a different config");
    }
}
