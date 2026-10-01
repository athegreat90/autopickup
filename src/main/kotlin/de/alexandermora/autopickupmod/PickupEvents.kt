package de.alexandermora.autopickupmod

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

object PickupEvents {

    @SubscribeEvent
    fun onEntityTick(event: EntityTickEvent.Post) {
        val player = event.entity as? ServerPlayer ?: return

        val serverLevel = player.level()

        if (serverLevel.isClientSide) {
            return
        }

        val range = ModConfig.cachedPickupRange
        val box = player.boundingBox.inflate(range)

        val nearbyItems = serverLevel.getEntitiesOfClass(
            ItemEntity::class.java,
            box
        ) { item -> item.isAlive && !item.hasPickUpDelay() && !item.item.isEmpty }

        for (itemEntity in nearbyItems) {
            val stack = itemEntity.item

            if (BlacklistHelper.isBlacklisted(stack)) {
                continue
            }

            val toInsert = stack.copy()
            val before = toInsert.count

            val inserted = player.inventory.add(toInsert)

            if (inserted || toInsert.isEmpty) {
                itemEntity.discard()
            } else if (toInsert.count < before) {
                itemEntity.setItem(toInsert)
            }
        }
    }
}