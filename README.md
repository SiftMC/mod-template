# Mod Template

A minimal multiloader Minecraft mod template for **Fabric**, **NeoForge** and **Forge** on the latest Minecraft (26.x).
You write the mod once in `common`; each loader project only holds an entrypoint and its metadata.

The example mod greets you in chat when you join a world or server, on every loader, from a single mixin in `common`:

```
Hello from examplemod on fabric!
Hello from examplemod on neoforge!
Hello from examplemod on forge!
```

## Layout

```
common/     Mod code against plain vanilla Minecraft (no loader API)
fabric/     Fabric entrypoint + fabric.mod.json
neoforge/   NeoForge entrypoint + neoforge.mods.toml
forge/      Forge entrypoint + mods.toml + pack.mcmeta
buildSrc/   Shared build logic and the loader Gradle plugin versions
gradle.properties   Mod info and every game/loader version
```

## Usage

JDK 25 is provisioned automatically if missing.

| Command | |
|---|---|
| `./gradlew build` | Jars in `<loader>/build/libs/` |
| `./gradlew :fabric:runClient` | Also `runServer`, and `:neoforge:` / `:forge:` |

## Start a new mod

1. Set the `mod_*` entries in `gradle.properties`.
2. Rename the `com.example.examplemod` package and set `ExampleMod.MOD_ID`.
3. Rename `common/src/main/resources/examplemod.mixins.json` to `<mod_id>.mixins.json` and update its `package`.
4. Update the entrypoint class name in `fabric/src/main/resources/fabric.mod.json`.
5. Update the copyright holder in `LICENSE`.

## Update Minecraft

Change the version block in `gradle.properties`:
[Fabric](https://fabricmc.net/develop), [NeoForge](https://projects.neoforged.net/neoforged/neoforge), [Forge](https://files.minecraftforge.net).
The loader plugin versions live in `buildSrc/build.gradle.kts`. The Minecraft dependency range in the metadata is derived
from `minecraft_version` (`26.3` accepts `26.3.x`).

Built and tested in game on every loader with no code changes:

| Minecraft | Fabric API | NeoForge | Forge |
|---|---|---|---|
| 26.3 | 0.161.0+26.3 | 26.3.0.34-beta | 66.0.8 |
| 26.2 | 0.161.0+26.2 | 26.2.0.88 | 65.1.3 |
| 26.1.2 | 0.155.3+26.1.2 | 26.1.2.112 | 64.1.3 |

## Rules for common

- Only vanilla Minecraft, Mixin and MixinExtras (all three loaders ship MixinExtras). Fabric Loader is on the compile
  classpath only for its annotations; calling its API compiles but crashes on (Neo)Forge.
- Mixins go in `<mod_id>.mixins.json`. Keep `compatibilityLevel` at `JAVA_21`: Forge ships Mixin 0.8.7, which knows nothing newer.
- Client-only code belongs in the `client` mixin list so dedicated servers never load it.
- Resources (`assets/`, `data/`) go in `common/src/main/resources`. Forge only loads them because of `forge/src/main/resources/pack.mcmeta`;
  bump its format when the Forge MDK does.
