package de.alexandermora.autopickupmod

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig.Type
import net.neoforged.fml.loading.FMLPaths
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files

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

        modContainer.registerConfig(Type.SERVER, ModConfig.SPEC, "autopickup/autopickup-server.toml")
        modBus.register(ConfigEvents)
    }
}