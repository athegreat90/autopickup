package de.alexandermora.autopickupmod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class ConfigEvents {

    private ConfigEvents() {}

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        refreshCaches(event.getConfig().getSpec(), "loaded");
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        refreshCaches(event.getConfig().getSpec(), "reloaded");
    }

    static void refreshCaches(IConfigSpec spec, String action) {
        if (spec != ModConfig.SPEC) {
            return;
        }

        ModConfig.cachedPickupRange = ModConfig.PICKUP_RANGE.get();
        BlacklistHelper.rebuildCache();

        ServerAutoPickupMod.LOGGER.info("Pickup range {}: {} and blacklist: OK", action, ModConfig.cachedPickupRange);
    }
}