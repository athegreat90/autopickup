# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A NeoForge mod for Minecraft (`autopickupmod` / "Auto Pickup Mod") that automatically pulls nearby dropped items into a player's inventory server-side, with a configurable pickup range and item blacklist. Targets Minecraft 26.1.2 / NeoForge 26.1.2.77, Java 25.

## Build & run commands

Use the Gradle wrapper (`gradlew.bat` on Windows, `./gradlew` in bash) for everything — there is no separate lint/test tooling configured beyond what Gradle/NeoForge provide.

- `gradlew build` — full build (compiles, runs `generateModMetadata`, packages the mod jar).
- `gradlew --refresh-dependencies` — refresh dependency cache if the IDE reports missing libraries.
- `gradlew clean` — reset build outputs without touching source.
- `gradlew runServer` — launch a dedicated server run configuration with this mod loaded (`--nogui`).
- `gradlew runGameTestServer` — launch `GameTestServer` and run registered gametests (crashes if none are registered; there are currently none).
- `gradlew runData` — run the data generator (client data), reading from `src/main/resources` and writing to `src/generated/resources`.

There are no unit tests in this repo currently; correctness is exercised via the in-game runs above.

CI (`.github/workflows/build.yml`) runs `./gradlew build` on JDK 21 for every push/PR.

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