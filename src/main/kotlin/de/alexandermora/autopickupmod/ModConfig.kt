package de.alexandermora.autopickupmod

import net.minecraft.world.item.Item
import net.neoforged.neoforge.common.ModConfigSpec

object ModConfig {
    @JvmField
    val SPEC: ModConfigSpec

    @JvmField
    val PICKUP_RANGE: ModConfigSpec.DoubleValue

    @JvmField
    val BLACKLISTED_ITEMS: ModConfigSpec.ConfigValue<List<String>>

    @Volatile
    @JvmField
    var cachedPickupRange: Double = 5.0

    @Volatile
    @JvmField
    var cacheBlacklist: Set<Item> = setOf()

    init {
        val builder = ModConfigSpec.Builder()

        builder.push("general")

        PICKUP_RANGE = builder
            .comment("How far from the player items can be auto-picked up.")
            .defineInRange("pickup_range", 5.0, 0.5, 16.0)

        BLACKLISTED_ITEMS = builder
            .comment("Items that should never be auto-picked up.")
            .defineListAllowEmpty(
                "blacklisted_items",
                listOf("minecraft:dirt"),
                { it is String && it.contains(":") }
            )

        builder.pop()

        SPEC = builder.build()
    }
}