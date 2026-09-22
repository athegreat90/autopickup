# Auto Pickup Mod

A server-side [NeoForge](https://neoforged.net/) mod that automatically pulls nearby dropped items into a
player's inventory. Pickup range and an item blacklist are configurable.

Pickup logic runs on **dedicated servers only**. Players do not need the mod installed on their client.

## Requirements

| Component | Version              |
|-----------|-----------------------|
| Minecraft | 26.1.2–26.2           |
| NeoForge  | 26.1.2.109–26.2.0.88  |
| Java      | 25                    |

## Installation

1. Build the mod (see below) or download the jar.
2. Place the jar in your server's `mods/` folder.
3. Start the server. The config file is generated on first launch.

## Configuration

The config file is `config/autopickup/autopickup-server.toml`. It is reloaded when the file changes, so a
restart is not needed.

| Key                         | Default             | Description                                                                       |
|-----------------------------|---------------------|-----------------------------------------------------------------------------------|
| `general.pickup_range`      | `5.0`               | Distance in blocks (0.5 to 16.0) around the player within which items are picked up. |
| `general.blacklisted_items` | `["minecraft:dirt"]` | Item ids (`namespace:path`) that are never auto-picked up. Unknown ids are logged and skipped. |

Items that do not fit in the inventory are left in the world; a partially fitting stack is picked up as far as
it fits.

## Building and testing

Use the Gradle wrapper (`gradlew.bat` on Windows, `./gradlew` elsewhere):

- `gradlew build` — compile and package the mod jar.
- `gradlew test` — run the JUnit 5 unit tests inside the NeoForge environment (also part of `build`).
- `gradlew runServer` — start a dedicated server with the mod loaded.
- `gradlew --refresh-dependencies` — refresh the dependency cache if your IDE reports missing libraries.
- `gradlew clean` — reset build outputs without touching source.

## Mapping names

The project uses the official Mojang mapping names for methods and fields in the Minecraft codebase. These names
are covered by a specific license; see https://github.com/NeoForged/NeoForm/blob/main/Mojang.md.

## Additional resources

- Community documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/

## License

All Rights Reserved.
