# Mod template

Multiloader Minecraft mod template for Fabric, NeoForge and Forge on Minecraft 26.x.
The mod lives in `common`. Each loader project holds only an entrypoint and its metadata.

The example mixin in `common` greets you in chat when you join a world or server, on every loader:

```
Hello from examplemod on fabric!
```

## Commands

```
./gradlew build               # jars in <loader>/build/libs
./gradlew :fabric:runClient   # also runServer, and :neoforge: or :forge:
```

Gradle downloads JDK 25 if you don't have it.

## New mod

1. Set the `mod_*` entries in `gradle.properties`.
2. Rename the `com.example.examplemod` package and `ExampleMod.MOD_ID`.
3. Rename `examplemod.mixins.json` to `<mod_id>.mixins.json` and update its `package`.
4. Update the entrypoint class in `fabric.mod.json`.
5. Update the copyright holder in `LICENSE`.

## Update Minecraft

Change the versions in `gradle.properties`. Latest versions are on the
[Fabric](https://fabricmc.net/develop), [NeoForge](https://projects.neoforged.net/neoforged/neoforge)
and [Forge](https://files.minecraftforge.net) sites. Loader Gradle plugins are pinned in `buildSrc/build.gradle.kts`.

26.1.2, 26.2 and 26.3 all work in game on every loader with only the version strings changed.

## Rules for common

- Use only vanilla Minecraft, Mixin and MixinExtras. Fabric Loader is on the classpath for its annotations.
  Calling its API compiles, then crashes on Forge and NeoForge.
- Keep the mixin `compatibilityLevel` at `JAVA_21`. Forge ships Mixin 0.8.7, which rejects anything newer.
- Put client-only mixins in the `client` list so dedicated servers skip them.
- Put assets and data in `common/src/main/resources`. Forge only loads them because of
  `forge/src/main/resources/pack.mcmeta`, so bump its format when the Forge MDK does.
