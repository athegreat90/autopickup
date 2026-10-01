package de.alexandermora.autopickupmod

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.config.IConfigSpec
import net.neoforged.fml.event.config.ModConfigEvent

object ConfigEvents {

    @SubscribeEvent
    fun onConfigLoading(event: ModConfigEvent.Loading) {
        refreshCaches(event.config.spec, "loaded")
    }

    @SubscribeEvent
    fun onConfigReloading(event: ModConfigEvent.Reloading) {
        refreshCaches(event.config.spec, "reloaded")
    }

    internal fun refreshCaches(spec: IConfigSpec, action: String) {
        if (spec !== ModConfig.SPEC) {
            return
        }

        ModConfig.cachedPickupRange = ModConfig.PICKUP_RANGE.get()
        BlacklistHelper.rebuildCache()

        ServerAutoPickupMod.LOGGER.info("Pickup range {}: {} and blacklist: OK", action, ModConfig.cachedPickupRange)
    }
}