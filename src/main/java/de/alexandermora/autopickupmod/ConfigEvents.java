package de.alexandermora.autopickupmod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class ConfigEvents {

    private ConfigEvents() {}

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.cachedPickupRange = ModConfig.PICKUP_RANGE.get();
            BlacklistHelper.rebuildCache();

            ServerAutoPickupMod.LOGGER.info("Pickup range loaded: {} and blacklist: OK", ModConfig.cachedPickupRange);
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.cachedPickupRange = ModConfig.PICKUP_RANGE.get();
            BlacklistHelper.rebuildCache();

            ServerAutoPickupMod.LOGGER.info("Pickup range reloaded: {} and blacklist: OK", ModConfig.cachedPickupRange);
        }
    }
}