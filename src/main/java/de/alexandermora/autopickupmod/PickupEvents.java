package de.alexandermora.autopickupmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

public final class PickupEvents {

    private PickupEvents() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double range = ModConfig.cachedPickupRange;
        AABB box = player.getBoundingBox().inflate(range);

        List<ItemEntity> nearbyItems = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                box,
                item -> item.isAlive()
                        && !item.hasPickUpDelay()
                        && !item.getItem().isEmpty()
        );

        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack stack = itemEntity.getItem();

            if (BlacklistHelper.isBlacklisted(stack)) {
                continue;
            }

            ItemStack toInsert = stack.copy();
            int before = toInsert.getCount();

            boolean inserted = player.getInventory().add(toInsert);

            if (inserted || toInsert.isEmpty()) {
                itemEntity.discard();
            } else if (toInsert.getCount() < before) {
                itemEntity.setItem(toInsert);
            }
        }
    }
}