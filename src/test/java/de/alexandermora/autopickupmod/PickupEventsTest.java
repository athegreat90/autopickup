package de.alexandermora.autopickupmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("minecraft")
class PickupEventsTest {

    @AfterEach
    void resetConfig() {
        ModConfig.cacheBlacklist = Set.of();
        ModConfig.cachedPickupRange = 5.0D;
    }

    @Test
    void onEntityTick_nonPlayerEntity_doesNothing() {
        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(mock(Entity.class));

        // Should return early without touching inventory or level
        PickupEvents.onEntityTick(event);
    }

    @Test
    void onEntityTick_clientSideLevel_doesNothing() {
        // ServerPlayer.level() returns ServerLevel (covariant); simulate client-side by flagging isClientSide()
        ServerLevel clientLevel = mock(ServerLevel.class);
        when(clientLevel.isClientSide()).thenReturn(true);

        ServerPlayer player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(clientLevel);

        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(player);

        PickupEvents.onEntityTick(event);

        verify(clientLevel, never()).getEntitiesOfClass(any(), any(), any());
    }

    @Test
    void onEntityTick_blacklistedItem_skipsPickup() {
        Item blacklisted = mock(Item.class);
        ModConfig.cacheBlacklist = Set.of(blacklisted);

        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getItem()).thenReturn(blacklisted);

        ItemEntity itemEntity = itemEntityWith(stack);
        ServerPlayer player = playerWithItems(List.of(itemEntity), mock(Inventory.class));

        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(player);

        PickupEvents.onEntityTick(event);

        verify(itemEntity, never()).discard();
        verify(player, never()).getInventory();
    }

    @Test
    void onEntityTick_fullPickup_discardsItemEntity() {
        ItemStack copy = mock(ItemStack.class);
        when(copy.getCount()).thenReturn(1);
        when(copy.isEmpty()).thenReturn(true); // fully consumed

        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getItem()).thenReturn(mock(Item.class));
        when(stack.copy()).thenReturn(copy);

        ItemEntity itemEntity = itemEntityWith(stack);

        Inventory inventory = mock(Inventory.class);
        when(inventory.add(copy)).thenReturn(true);

        ServerPlayer player = playerWithItems(List.of(itemEntity), inventory);

        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(player);

        PickupEvents.onEntityTick(event);

        verify(itemEntity).discard();
        verify(itemEntity, never()).setItem(any());
    }

    @Test
    void onEntityTick_partialPickup_updatesItemStack() {
        ItemStack copy = mock(ItemStack.class);
        // getCount() returns 10 on first call (before), then 5 (after partial add)
        when(copy.getCount()).thenReturn(10, 5);
        when(copy.isEmpty()).thenReturn(false);

        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getItem()).thenReturn(mock(Item.class));
        when(stack.copy()).thenReturn(copy);

        ItemEntity itemEntity = itemEntityWith(stack);

        Inventory inventory = mock(Inventory.class);
        when(inventory.add(copy)).thenReturn(false);

        ServerPlayer player = playerWithItems(List.of(itemEntity), inventory);

        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(player);

        PickupEvents.onEntityTick(event);

        verify(itemEntity).setItem(copy);
        verify(itemEntity, never()).discard();
    }

    @Test
    void onEntityTick_fullInventory_leavesItemInWorld() {
        ItemStack copy = mock(ItemStack.class);
        when(copy.getCount()).thenReturn(10); // unchanged — no items were added
        when(copy.isEmpty()).thenReturn(false);

        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getItem()).thenReturn(mock(Item.class));
        when(stack.copy()).thenReturn(copy);

        ItemEntity itemEntity = itemEntityWith(stack);

        Inventory inventory = mock(Inventory.class);
        when(inventory.add(copy)).thenReturn(false);

        ServerPlayer player = playerWithItems(List.of(itemEntity), inventory);

        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(player);

        PickupEvents.onEntityTick(event);

        verify(itemEntity, never()).discard();
        verify(itemEntity, never()).setItem(any());
    }

    @Test
    void onEntityTick_noNearbyItems_doesNothing() {
        ServerPlayer player = playerWithItems(List.of(), mock(Inventory.class));

        EntityTickEvent.Post event = mock(EntityTickEvent.Post.class);
        when(event.getEntity()).thenReturn(player);

        PickupEvents.onEntityTick(event);

        verify(player, never()).getInventory();
    }

    // --- helpers ---

    private ItemEntity itemEntityWith(ItemStack stack) {
        ItemEntity entity = mock(ItemEntity.class);
        when(entity.isAlive()).thenReturn(true);
        when(entity.hasPickUpDelay()).thenReturn(false);
        when(entity.getItem()).thenReturn(stack);
        return entity;
    }

    private ServerPlayer playerWithItems(List<ItemEntity> items, Inventory inventory) {
        ServerLevel level = mock(ServerLevel.class);
        when(level.isClientSide()).thenReturn(false);
        when(level.getEntitiesOfClass(eq(ItemEntity.class), any(AABB.class), any()))
                .thenReturn(items);

        ServerPlayer player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(level);
        when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
        when(player.getInventory()).thenReturn(inventory);
        return player;
    }
}
