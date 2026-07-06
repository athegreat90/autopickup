package de.alexandermora.autopickupmod;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;

public final class ModConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue PICKUP_RANGE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;

    public static volatile double cachedPickupRange = 5.0D;
    public static volatile Set<Item> cacheBlacklist = Set.of();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        PICKUP_RANGE = builder
                .comment("How far from the player items can be auto-picked up.")
                .defineInRange("pickup_range", 5.0D, 0.5D, 16.0D);

        BLACKLISTED_ITEMS = builder
                .comment("Items that should never be auto-picked up.")
                .defineListAllowEmpty(
                        "blacklisted_items",
                        List.of("minecraft:dirt"),
                        o -> o instanceof String s && s.contains(":")
                );

        builder.pop();

        SPEC = builder.build();
    }

    private ModConfig() {
    }
}