package de.alexandermora.autopickupmod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class BlacklistHelper {

    private BlacklistHelper() {}

    public static void rebuildCache() {
        List<? extends String> configured = ModConfig.BLACKLISTED_ITEMS.get();
        Set<String> normalized = new HashSet<>();
        Set<Item> resolved = new HashSet<>();

        for (String raw : configured) {
            if (raw == null) {
                continue;
            }

            String id = raw.trim().toLowerCase();
            if (!id.contains(":")) {
                ServerAutoPickupMod.LOGGER.warn("Invalid blacklisted item id: {}", raw);
                continue;
            }

            normalized.add(id);
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Optional<ResourceKey<Item>> keyOpt = BuiltInRegistries.ITEM.getResourceKey(item);

            if (keyOpt.isEmpty()) {
                continue;
            }

            String registryName = extractRegistryName(keyOpt.get());
            if (registryName == null) {
                continue;
            }

            if (normalized.contains(registryName)) {
                resolved.add(item);
            }
        }

        ModConfig.cacheBlacklist = Collections.unmodifiableSet(resolved);

        ServerAutoPickupMod.LOGGER.info(
                "Blacklist cache rebuilt with {} item(s)",
                ModConfig.cacheBlacklist.size()
        );
    }

    private static String extractRegistryName(ResourceKey<Item> key) {
        String text = key.toString();

        // Typical format:
        // ResourceKey[minecraft:item / minecraft:dirt]
        int slash = text.indexOf(" / ");
        int end = text.lastIndexOf(']');

        if (slash == -1 || end == -1 || slash + 3 >= end) {
            ServerAutoPickupMod.LOGGER.warn("Could not parse registry key: {}", text);
            return null;
        }

        return text.substring(slash + 3, end).trim().toLowerCase();
    }

    public static boolean isBlacklisted(ItemStack stack) {
        return !stack.isEmpty() && ModConfig.cacheBlacklist.contains(stack.getItem());
    }
}