package de.alexandermora.autopickupmod

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.Locale

object BlacklistHelper {

    fun rebuildCache() {
        rebuildCacheFromList(ModConfig.BLACKLISTED_ITEMS.get())
    }

    internal fun rebuildCacheFromList(configured: List<String?>?) {
        val resolved = mutableSetOf<Item>()

        for (raw in configured.orEmpty()) {
            if (raw == null || raw.isBlank()) {
                continue
            }

            val normalized = raw.trim().lowercase(Locale.ROOT)

            val id = runCatching { Identifier.parse(normalized) }.getOrNull()
            if (id == null) {
                ServerAutoPickupMod.LOGGER.warn("Invalid blacklisted item id: {}", raw)
                continue
            }

            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                ServerAutoPickupMod.LOGGER.warn("Unknown blacklisted item id: {}", raw)
                continue
            }

            resolved.add(BuiltInRegistries.ITEM.getValue(id))
        }

        ModConfig.cacheBlacklist = resolved.toSet()
        ServerAutoPickupMod.LOGGER.info(
            "Blacklist cache rebuilt with {} item(s)",
            ModConfig.cacheBlacklist.size
        )
    }

    fun isBlacklisted(stack: ItemStack): Boolean {
        return !stack.isEmpty && isBlacklisted(stack.item)
    }

    internal fun isBlacklisted(item: Item): Boolean {
        return ModConfig.cacheBlacklist.contains(item)
    }
}