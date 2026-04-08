package de.alexandermora.autopickupmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

import static net.neoforged.fml.config.ModConfig.*;

@Mod(ServerAutoPickupMod.MODID)
public class ServerAutoPickupMod {
    public static final String MODID = "autopickupmod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public ServerAutoPickupMod(IEventBus modBus, ModContainer modContainer) {
        var folder = FMLPaths.CONFIGDIR.get().resolve("autopickup");

        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new RuntimeException("Could not create config folder: " + folder, e);
        }


        modContainer.registerConfig(Type.SERVER, ModConfig.SPEC);
        modContainer.registerConfig(Type.SERVER, ModConfig.SPEC, "autopickup/autopickup-server.toml");
        modBus.register(ConfigEvents.class);
    }
}