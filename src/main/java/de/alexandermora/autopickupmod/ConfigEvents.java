package de.alexandermora.autopickupmod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class ConfigEvents {

    private ConfigEvents() {}

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.cachedPickupRange = ModConfig.PICKUP_RANGE.get();
            ServerAutoPickupMod.LOGGER.info("Pickup range loaded: {}", ModConfig.cachedPickupRange);

        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.cachedPickupRange = ModConfig.PICKUP_RANGE.get();
            ServerAutoPickupMod.LOGGER.info("Pickup range reloaded: {}", ModConfig.cachedPickupRange);

        }
    }
}