# Decisions behind this change

Record of why the repo's branches and the mod's source language changed, for future contributors.

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