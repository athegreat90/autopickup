package de.alexandermora.autopickupmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ServerAutoPickupMod.MODID, dist = Dist.DEDICATED_SERVER)
public class ServerAutoPickupDedicatedServer {
    public ServerAutoPickupDedicatedServer(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(PickupEvents.class);
    }
}