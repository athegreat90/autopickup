package de.alexandermora.autopickupmod

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig.Type
import net.neoforged.fml.loading.FMLPaths
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files

/** Mod entry point: creates the config folder and registers the server config. */
@Mod(ServerAutoPickupMod.MODID)
class ServerAutoPickupMod(modBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MODID = "autopickupmod"
        val LOGGER: Logger = LoggerFactory.getLogger(MODID)
    }

    init {
        val folder = FMLPaths.CONFIGDIR.get().resolve("autopickup")

        runCatching { Files.createDirectories(folder) }
            .onFailure { e -> LOGGER.warn("Could not create config folder: {}", folder, e) }

        // Register server config. In NeoForge 26.3+, Type enum was refactored.
        // Using values()[0] which corresponds to the default server config type.
        val serverConfigType = Type.entries.firstOrNull()
        if (serverConfigType != null) {
            modContainer.registerConfig(serverConfigType, ModConfig.SPEC, "autopickup/autopickup-server.toml")
        } else {
            LOGGER.error("Failed to find appropriate config type for registration")
        }
        modBus.register(ConfigEvents)
    }
}