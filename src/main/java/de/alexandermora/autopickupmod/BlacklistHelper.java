package de.alexandermora.autopickupmod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BlacklistHelper {

    private BlacklistHelper() {}

    public static void rebuildCache() {
        rebuildCacheFromList(ModConfig.BLACKLISTED_ITEMS.get());
    }

    static void rebuildCacheFromList(List<? extends String> configured) {
        Set<Item> resolved = new HashSet<>();

        for (var raw : configured) {
            if (raw == null || raw.isBlank()) {
                continue;
            }

            var normalized = raw.trim().toLowerCase(Locale.ROOT);

            try {
                var id = Identifier.parse(normalized);

                if (!BuiltInRegistries.ITEM.containsKey(id)) {
                    ServerAutoPickupMod.LOGGER.warn("Unknown blacklisted item id: {}", raw);
                    continue;
                }

                var item = BuiltInRegistries.ITEM.getValue(id);
                resolved.add(item);
            } catch (RuntimeException exception) {
                ServerAutoPickupMod.LOGGER.warn("Invalid blacklisted item id: {}", raw);
            }
        }

        ModConfig.cacheBlacklist = Collections.unmodifiableSet(resolved);
        ServerAutoPickupMod.LOGGER.info(
                "Blacklist cache rebuilt with {} item(s)",
                ModConfig.cacheBlacklist.size()
        );
    }

    public static boolean isBlacklisted(ItemStack stack) {
        return !stack.isEmpty() && isBlacklisted(stack.getItem());
    }

    static boolean isBlacklisted(Item item) {
        return ModConfig.cacheBlacklist.contains(item);
    }
}
