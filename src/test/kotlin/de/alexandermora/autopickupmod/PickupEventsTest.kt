package de.alexandermora.autopickupmod

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.neoforged.neoforge.event.tick.EntityTickEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever

@Tag("minecraft")
class PickupEventsTest {

    @AfterEach
    fun resetConfig() {
        ModConfig.cacheBlacklist = setOf()
        ModConfig.cachedPickupRange = 5.0
    }

    @Test
    fun `onEntityTick non player entity does nothing`() {
        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(mock(Entity::class.java))

        // Should return early without touching inventory or level
        PickupEvents.onEntityTick(event)
    }

    @Test
    fun `onEntityTick client side level does nothing`() {
        // ServerPlayer.level() returns ServerLevel (covariant); simulate client-side by flagging isClientSide()
        val clientLevel = mock(ServerLevel::class.java)
        whenever(clientLevel.isClientSide).thenReturn(true)

        val player = mock(ServerPlayer::class.java)
        whenever(player.level()).thenReturn(clientLevel)

        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(player)

        PickupEvents.onEntityTick(event)

        verify(clientLevel, never()).getEntitiesOfClass(eq(ItemEntity::class.java), any(AABB::class.java), any())
    }

    @Test
    fun `onEntityTick blacklisted item skips pickup`() {
        val blacklisted = mock(Item::class.java)
        ModConfig.cacheBlacklist = setOf(blacklisted)

        val stack = mock(ItemStack::class.java)
        whenever(stack.isEmpty).thenReturn(false)
        whenever(stack.item).thenReturn(blacklisted)

        val itemEntity = itemEntityWith(stack)
        val player = playerWithItems(listOf(itemEntity), mock(Inventory::class.java))

        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(player)

        PickupEvents.onEntityTick(event)

        verify(itemEntity, never()).discard()
        verify(player, never()).inventory
    }

    @Test
    fun `onEntityTick full pickup discards item entity`() {
        val copy = mock(ItemStack::class.java)
        whenever(copy.count).thenReturn(1)
        whenever(copy.isEmpty).thenReturn(true) // fully consumed

        val stack = mock(ItemStack::class.java)
        whenever(stack.isEmpty).thenReturn(false)
        whenever(stack.item).thenReturn(mock(Item::class.java))
        whenever(stack.copy()).thenReturn(copy)

        val itemEntity = itemEntityWith(stack)

        val inventory = mock(Inventory::class.java)
        whenever(inventory.add(copy)).thenReturn(true)

        val player = playerWithItems(listOf(itemEntity), inventory)

        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(player)

        PickupEvents.onEntityTick(event)

        verify(itemEntity).discard()
        verify(itemEntity, never()).setItem(any())
    }

    @Test
    fun `onEntityTick partial pickup updates item stack`() {
        val copy = mock(ItemStack::class.java)
        // getCount() returns 10 on first call (before), then 5 (after partial add)
        whenever(copy.count).thenReturn(10, 5)
        whenever(copy.isEmpty).thenReturn(false)

        val stack = mock(ItemStack::class.java)
        whenever(stack.isEmpty).thenReturn(false)
        whenever(stack.item).thenReturn(mock(Item::class.java))
        whenever(stack.copy()).thenReturn(copy)

        val itemEntity = itemEntityWith(stack)

        val inventory = mock(Inventory::class.java)
        whenever(inventory.add(copy)).thenReturn(false)

        val player = playerWithItems(listOf(itemEntity), inventory)

        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(player)

        PickupEvents.onEntityTick(event)

        verify(itemEntity).setItem(copy)
        verify(itemEntity, never()).discard()
    }

    @Test
    fun `onEntityTick full inventory leaves item in world`() {
        val copy = mock(ItemStack::class.java)
        whenever(copy.count).thenReturn(10) // unchanged — no items were added
        whenever(copy.isEmpty).thenReturn(false)

        val stack = mock(ItemStack::class.java)
        whenever(stack.isEmpty).thenReturn(false)
        whenever(stack.item).thenReturn(mock(Item::class.java))
        whenever(stack.copy()).thenReturn(copy)

        val itemEntity = itemEntityWith(stack)

        val inventory = mock(Inventory::class.java)
        whenever(inventory.add(copy)).thenReturn(false)

        val player = playerWithItems(listOf(itemEntity), inventory)

        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(player)

        PickupEvents.onEntityTick(event)

        verify(itemEntity, never()).discard()
        verify(itemEntity, never()).setItem(any())
    }

    @Test
    fun `onEntityTick no nearby items does nothing`() {
        val player = playerWithItems(listOf(), mock(Inventory::class.java))

        val event = mock(EntityTickEvent.Post::class.java)
        whenever(event.entity).thenReturn(player)

        PickupEvents.onEntityTick(event)

        verify(player, never()).inventory
    }

    // --- helpers ---

    private fun itemEntityWith(stack: ItemStack): ItemEntity {
        val entity = mock(ItemEntity::class.java)
        whenever(entity.isAlive).thenReturn(true)
        whenever(entity.hasPickUpDelay()).thenReturn(false)
        whenever(entity.item).thenReturn(stack)
        return entity
    }

    private fun playerWithItems(items: List<ItemEntity>, inventory: Inventory): ServerPlayer {
        val level = mock(ServerLevel::class.java)
        whenever(level.isClientSide).thenReturn(false)
        whenever(
            level.getEntitiesOfClass(eq(ItemEntity::class.java), any(AABB::class.java), any())
        ).thenReturn(items)

        val player = mock(ServerPlayer::class.java)
        whenever(player.level()).thenReturn(level)
        whenever(player.boundingBox).thenReturn(AABB(0.0, 0.0, 0.0, 1.0, 2.0, 1.0))
        whenever(player.inventory).thenReturn(inventory)
        return player
    }
}