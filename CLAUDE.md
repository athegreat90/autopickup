# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A NeoForge mod for Minecraft (`autopickupmod` / "Auto Pickup Mod") that automatically pulls nearby dropped items into a player's inventory server-side, with a configurable pickup range and item blacklist. Targets Minecraft 26.1.2–26.2 / NeoForge 26.1.2.109–26.2.0.88 (built against 26.2), Java 25.

## Build & run commands

Use the Gradle wrapper (`gradlew.bat` on Windows, `./gradlew` in bash) for everything — there is no separate lint tooling configured beyond what Gradle/NeoForge provide.

- `gradlew build` — full build (compiles, runs `generateModMetadata`, packages the mod jar).
- `gradlew --refresh-dependencies` — refresh dependency cache if the IDE reports missing libraries.
- `gradlew clean` — reset build outputs without touching source.
- `gradlew runServer` — launch a dedicated server run configuration with this mod loaded (`--nogui`).
- `gradlew runGameTestServer` — launch `GameTestServer` and run registered gametests (crashes if none are registered; there are currently none).
- `gradlew test` — run the JUnit 5 unit tests. ModDevGradle's `unitTest` integration runs them inside the NeoForge environment (FMLLoader + Bootstrap), which tests touching Minecraft registries such as `BlacklistHelperTest` need. `gradlew build` also runs them.
- `gradlew runData` — run the data generator (client data), reading from `src/main/resources` and writing to `src/generated/resources`.

Beyond the unit tests (see Testing), correctness is exercised via the in-game runs above.

CI (`.github/workflows/build.yml`) runs `./gradlew build` on JDK 25 (Temurin) for every push/PR, matching the Java 25 toolchain required by `build.gradle` (`java.toolchain.languageVersion = 25`).

## Testing

Unit tests live in `src/test/java/de/alexandermora/autopickupmod/` (JUnit 5.11.4, Mockito 5.14.2). `build.gradle` adds `sourceSets.test` to the mod's `neoForge.mods` entry and enables `unitTest` with `testedMod`, which is what makes `test` run inside the NeoForge environment.

- `BlacklistHelperTest` — `isBlacklisted` and `rebuildCacheFromList` (null, blank, malformed, unknown and known ids); bootstraps Minecraft registries.
- `ConfigEventsTest` — events for a different config spec must not touch the caches.
- `ModConfigTest` — default cache values (range 5.0, empty immutable blacklist).
- `PickupEventsTest` — `onEntityTick` for non-player/client-side entities, blacklisted items, full/partial pickup, full inventory, no nearby items.
- `ServerAutoPickupModTest` — the `MODID` constant.

Mockito uses the inline mock maker (`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`) so final Minecraft types (`ItemStack`) and methods (`Entity#discard`) can be mocked. The `test` task in `build.gradle` passes `-XX:+EnableDynamicAgentLoading` and `-Dnet.bytebuddy.experimental=true` so ByteBuddy works on Java 25; keep these when changing test config.

## Architecture

This is a small, server-only mod (`side="BOTH"` in the mod metadata, but pickup logic is registered only on `Dist.DEDICATED_SERVER`). Everything lives under `src/main/java/de/alexandermora/autopickupmod/`:

- **`ServerAutoPickupMod`** — the `@Mod` entry point (common/all-dist). Creates the `config/autopickup/` directory and registers the server config spec (`ModConfig.SPEC`) as `autopickup/autopickup-server.toml`. Registers `ConfigEvents` on the mod event bus.
- **`ServerAutoPickupDedicatedServer`** — a second `@Mod`-annotated class scoped to `Dist.DEDICATED_SERVER` only. This is where `PickupEvents` gets registered onto `NeoForge.EVENT_BUS` (the game event bus, as opposed to the mod event bus) — so pickup logic only runs on a dedicated server, not in-process on integrated/singleplayer.
- **`ModConfig`** — defines the `ModConfigSpec` (pickup range as a bounded double, blacklisted items as a string list of `namespace:path` ids). Also holds two `volatile` runtime caches (`cachedPickupRange`, `cacheBlacklist`) that are read on every tick — these are populated by `ConfigEvents`/`BlacklistHelper`, not read directly from the `ModConfigSpec` values, to avoid the cost of resolving config on the hot path.
- **`ConfigEvents`** — listens for `ModConfigEvent.Loading`/`Reloading` on `ModConfig.SPEC` specifically, and refreshes the two caches above (`ModConfig.cachedPickupRange`, and `BlacklistHelper.rebuildCache()`).
- **`BlacklistHelper`** — resolves the configured blacklist strings into `Item` instances via `BuiltInRegistries.ITEM`, logging and skipping unknown/invalid ids. Populates `ModConfig.cacheBlacklist`. `isBlacklisted(ItemStack)` is the hot-path check used per item.
- **`PickupEvents`** — the core loop, subscribed to `EntityTickEvent.Post`. For each server-side player tick: builds an AABB around the player inflated by `ModConfig.cachedPickupRange`, queries nearby non-empty `ItemEntity`s without pickup delay, skips blacklisted items, and inserts into the player's inventory — discarding the item entity on full pickup or updating its stack on partial pickup.

Key pattern to preserve when touching config: config values are cached into plain volatile fields at load/reload time rather than dereferenced from the `ModConfigSpec` on every tick.

Resource generation: `src/main/templates/META-INF/neoforge.mods.toml` is a template expanded by the `generateModMetadata` Gradle task (`${mod_id}`, `${mod_version}`, etc. from `gradle.properties`) into `build/generated/sources/modMetadata`, not edited directly for per-build values.

## Config

Project-wide values (mod id, version, Minecraft/NeoForge versions, group id) live in `gradle.properties`, not hardcoded in `build.gradle`. The mod id (`autopickupmod`) must match the `@Mod(MODID)` constant in `ServerAutoPickupMod`.

Generated server config file at runtime: `config/autopickup/autopickup-server.toml`, containing `general.pickup_range` (0.5–16.0, default 5.0) and `general.blacklisted_items` (default `["minecraft:dirt"]`).