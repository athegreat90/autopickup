# Decisions behind this change

Record of why the repo's branches and the mod's source language changed, for future contributors.

## Version 26.3 support

- Added official support for Minecraft 26.3 / NeoForge 26.3.0.83 on 2026-10-18.
- Updated version constraints in `gradle.properties`:
  - `minecraft_version`: 26.2 → 26.3
  - `minecraft_version_range`: [26.1.2,26.2] → [26.1.2,26.3]
  - `neo_version`: 26.2.0.88 → 26.3.0.83
- Java 25 remains the target (unchanged; 26.3 ships with Java 25 like earlier 26.x versions).
- KotlinLangForge 2.14.1 (language provider 3.1) already covers MC 1.21.9-26.x, so no dependency update needed.
- Parchment mappings (2024.11.17) remain compatible with 26.3.
- ModDevGradle plugin (2.0.147) remains compatible.
- No code changes were required; compatibility verified through `./gradlew build` and `./gradlew runServer`.

## Release automation

- `.github/workflows/release.yml` ("Create release") runs on push to `26` when `src/**`, `build.gradle` or
  `gradle.properties` change, and on manual `workflow_dispatch` (only on `26`). It first runs `./gradlew build`;
  only if that passes does it release.
- Versioning: if the `mod_version` in `gradle.properties` has no release/tag yet it is released as is (use this for
  manual minor/major bumps); otherwise the patch number is bumped automatically and committed to `26` as
  `chore: release X.Y.Z [skip ci]`, and the release targets that commit.
- The bump commit and release use the default `GITHUB_TOKEN` (no PAT needed). Because they do not trigger other
  workflows (no loop), `release.yml` calls `publish.yml` directly (`workflow_call`) after creating the release.
  This needs `26` to allow pushes from `github-actions` (no branch protection at the time of writing).
- `.github/workflows/publish.yml` remains the single place that publishes to Modrinth. It runs via
  `workflow_call` from `release.yml` and on `release: published` (so manually created releases publish too).
  It checks out the release tag and reads name, body and pre-release flag from the release itself.
  Pre-releases are published as `beta`.
- Keep `game-versions` in `publish.yml` in sync with `minecraft_version_range` in `gradle.properties`
  (`26.3` was added alongside the 26.3 support).
- Delivery of this automation used short-lived branches merged into `26` through PRs (squash-merged).

## Branch consolidation

- The repo moved from three branches (`main`, `26.1`, `26.2`) to two: `main` and `26`. `26` was created
  from `26.2` (a strict superset of `26.1`), replacing the per-minor-version branch naming so the branch
  doesn't need to be re-cut every time the targeted NeoForge minor version bumps.
- `26` became the new GitHub default branch, replacing `26.1`.
- `main` was deliberately **left untouched** (not fast-forwarded to match `26`) — it continues to exist as
  a branch name only, not as a mirror of current development.
- `26.1`, `26.2`, and the `feat/Kotlin-Migration` branch used for this change were deleted (locally and on
  `origin`) once merged.

## Kotlin migration

- The mod's source (`src/main/java` → `src/main/kotlin`, `src/test/java` → `src/test/kotlin`) was migrated
  from Java to Kotlin, using [KotlinLangForge](https://github.com/btwonion/KotlinLangForge) (KLF) as the
  NeoForge Kotlin language adapter (`modLoader="klf"` / `loaderVersion="[1,)"` in `neoforge.mods.toml`,
  dependency `dev.nyon:KotlinLangForge:2.14.1-k2.4.20-3.1+neoforge` from `https://repo.nyon.dev/releases`).
- This was a language migration only — not a response to a bug or limitation in the prior Java code.
- `build.gradle` / `settings.gradle` were deliberately **kept as Groovy**, not converted to Kotlin DSL
  (`.kts`) — only the mod's own source was migrated, to minimize build-tooling risk.
- What were static-only Java classes (`ModConfig`, `ConfigEvents`, `BlacklistHelper`, `PickupEvents`)
  became Kotlin `object`s; NeoForge event-bus registration switched from `modBus.register(X.class)` to
  `modBus.register(X)` (the singleton instance) — no `@JvmStatic` needed on `@SubscribeEvent` methods.
  Former package-private methods became Kotlin `internal`, relying on Kotlin's default main/test "friend"
  association so tests can still call them directly.
- Tests kept plain JUnit 5 + Mockito (`org.mockito.Mockito.mock(...)` / `` `when`(...) `` called from
  Kotlin) — no new test dependency (e.g. `mockito-kotlin`) was introduced.
- The `feat/Kotlin-Migration` → `26` PR was merged with a regular merge commit (not squashed), to preserve
  the migration's commit history on `26`.
- `mod_version` was bumped from `2.0.0-rc1` to `3.0.0-RC1` as part of this change — treated as a major,
  breaking release because server operators now need the KLF mod installed alongside this one for the jar
  to load at all.
- Error handling in the migrated Kotlin sources prefers `runCatching` over `try`/`catch`, and skips a
  single invalid/missing item (logging a warning) rather than aborting the whole operation — see
  `BlacklistHelper.rebuildCacheFromList` (skips unknown/invalid blacklist ids) and
  `ServerAutoPickupMod`'s config-folder creation (logs and continues into config registration on failure
  instead of crashing mod startup).
- `.github/workflows/publish.yml` now declares a required Modrinth dependency on `kotlin-lang-forge`
  (KLF's own Modrinth project) via mc-publish's `dependencies` input — since the mod only `implementation`s
  the KLF jar rather than `jarJar`-embedding it, players installing from Modrinth need KLF installed
  separately or the mod won't load.

- The repo moved from three branches (`main`, `26.1`, `26.2`) to two: `main` and `26`. `26` was created
  from `26.2` (a strict superset of `26.1`), replacing the per-minor-version branch naming so the branch
  doesn't need to be re-cut every time the targeted NeoForge minor version bumps.
- `26` became the new GitHub default branch, replacing `26.1`.
- `main` was deliberately **left untouched** (not fast-forwarded to match `26`) — it continues to exist as
  a branch name only, not as a mirror of current development.
- `26.1`, `26.2`, and the `feat/Kotlin-Migration` branch used for this change were deleted (locally and on
  `origin`) once merged.

## Kotlin migration

- The mod's source (`src/main/java` → `src/main/kotlin`, `src/test/java` → `src/test/kotlin`) was migrated
  from Java to Kotlin, using [KotlinLangForge](https://github.com/btwonion/KotlinLangForge) (KLF) as the
  NeoForge Kotlin language adapter (`modLoader="klf"` / `loaderVersion="[1,)"` in `neoforge.mods.toml`,
  dependency `dev.nyon:KotlinLangForge:2.14.1-k2.4.20-3.1+neoforge` from `https://repo.nyon.dev/releases`).
- This was a language migration only — not a response to a bug or limitation in the prior Java code.
- `build.gradle` / `settings.gradle` were deliberately **kept as Groovy**, not converted to Kotlin DSL
  (`.kts`) — only the mod's own source was migrated, to minimize build-tooling risk.
- What were static-only Java classes (`ModConfig`, `ConfigEvents`, `BlacklistHelper`, `PickupEvents`)
  became Kotlin `object`s; NeoForge event-bus registration switched from `modBus.register(X.class)` to
  `modBus.register(X)` (the singleton instance) — no `@JvmStatic` needed on `@SubscribeEvent` methods.
  Former package-private methods became Kotlin `internal`, relying on Kotlin's default main/test "friend"
  association so tests can still call them directly.
- Tests kept plain JUnit 5 + Mockito (`org.mockito.Mockito.mock(...)` / `` `when`(...) `` called from
  Kotlin) — no new test dependency (e.g. `mockito-kotlin`) was introduced.
- The `feat/Kotlin-Migration` → `26` PR was merged with a regular merge commit (not squashed), to preserve
  the migration's commit history on `26`.
- `mod_version` was bumped from `2.0.0-rc1` to `3.0.0-RC1` as part of this change — treated as a major,
  breaking release because server operators now need the KLF mod installed alongside this one for the jar
  to load at all.
- Error handling in the migrated Kotlin sources prefers `runCatching` over `try`/`catch`, and skips a
  single invalid/missing item (logging a warning) rather than aborting the whole operation — see
  `BlacklistHelper.rebuildCacheFromList` (skips unknown/invalid blacklist ids) and
  `ServerAutoPickupMod`'s config-folder creation (logs and continues into config registration on failure
  instead of crashing mod startup).
- `.github/workflows/publish.yml` now declares a required Modrinth dependency on `kotlin-lang-forge`
  (KLF's own Modrinth project) via mc-publish's `dependencies` input — since the mod only `implementation`s
  the KLF jar rather than `jarJar`-embedding it, players installing from Modrinth need KLF installed
  separately or the mod won't load.