# Mod template

Multiloader Minecraft mod template for Fabric, NeoForge and Forge.
The mod lives in `common`. Each loader project holds only an entrypoint and its metadata.
One file lists the Minecraft versions, and Stonecutter builds the same sources for each of them.

The example mixin in `common` greets you in chat when you join a world or server, on every loader and version:

```
Hello from examplemod on fabric 26.3!
```

## Commands

```
./gradlew build                           # mod jars in <loader>/versions/<version>/build/libs
./gradlew :fabric:26.3:runServer          # any loader, any listed version
./gradlew :fabric:26.3:runClient
./gradlew :fabric:26.3:runClient -Pjoin   # joins the dev server on localhost
```

Gradle needs JDK 21 or newer to start. It downloads the JDK each Minecraft version needs.
On 1.7.10 the jar to ship is the one without `-dev` in its name.

The first `runServer` exits. Set `eula=true` in `<loader>/versions/<version>/run/eula.txt` and run it again.
Set `online-mode=false` and `white-list=false` in `server.properties` next to it, or the server rejects the dev client.

A Gradle sync in IntelliJ writes three run configurations per loader for the version in `src/`.
They are `Fabric Client`, `Fabric Server` and `Fabric Client (join server)`, and the same for NeoForge and Forge.

## New mod

1. Set the `mod_*` entries in `gradle.properties`. Forge and NeoForge accept only lowercase letters, digits and `_`
   in `mod_id`.
2. Rename the `com.example.examplemod` package in `common`, `fabric`, `neoforge` and `forge`, rename the four
   `ExampleMod*` classes, set `ExampleMod.MOD_ID` to your `mod_id` and change the `examplemod$` prefix of the mixin
   methods to it.
3. Rename `examplemod.mixins.json` to `<mod_id>.mixins.json` and update its `package`.
4. Update the entrypoint class in `fabric.mod.json`.
5. Update the copyright holder in `LICENSE`.

IDE refactors skip the commented-out Stonecutter branches, such as the 1.7.10 `@Mod` line in `ExampleModForge`.
Check those by hand.

## Minecraft versions

`stonecutter.properties.toml` lists them:

```toml
fabric_loader_version = "0.19.5"

["26.3"]
fabric_api_version = "0.161.0+26.3"
neoforge_version = "26.3.0.34-beta"
forge_version = "66.0.8"

["1.20.1"]
fabric_api_version = "0.92.12+1.20.1"
forge_version = "47.4.23"
```

Each section is a Minecraft version. It gets a loader when it sets `fabric_api_version`, `neoforge_version` or
`forge_version`, so 1.20.1 above gets Fabric and Forge but no NeoForge. Add a section to add a version.
Delete a line or a section to drop one.

The first section is the version in `src/`. To move to a new Minecraft, rename that section, update its lines and change
`stonecutter active` in `stonecutter.gradle.kts` to match. Latest loader versions are on the
[Fabric](https://fabricmc.net/develop), [NeoForge](https://projects.neoforged.net/neoforged/neoforge)
and [Forge](https://files.minecraftforge.net) sites. Loader Gradle plugins are pinned in `buildSrc/build.gradle.kts`.
Stonecutter is pinned there and in `settings.gradle.kts`; bump both.

The build picks the Java version, the mixin `compatibilityLevel`, the Minecraft dependency range and the build script
from the Minecraft version:

| Loader | Minecraft | Build script | Toolchain |
|---|---|---|---|
| Fabric | 26.1 and newer | `build.gradle.kts` | Loom |
| Fabric | 1.14.4 to 1.21.x | `build.legacy.gradle.kts` | Loom with Mojang mappings |
| NeoForge | 1.21 and newer | `build.gradle.kts` | ModDevGradle |
| Forge | 1.20.6 and newer | `build.gradle.kts` | ForgeGradle |
| Forge | 1.17 to 1.20.1 | `build.legacy.gradle.kts` | ModDevGradle for legacy Forge |
| Forge | 1.7.10 | `build.retro.gradle.kts` | RetroFuturaGradle. Players need [UniMixins](https://modrinth.com/mod/unimixins) |

Versions outside these rows have no script, including 1.8 to 1.13 and Forge 1.20.2 to 1.20.4.
If you never build the older versions, delete the legacy and retro scripts. With the retro scripts gone, also delete
the RetroFuturaGradle line and the GTNH repository in `buildSrc/build.gradle.kts`.

Tested by joining a dev server with a dev client and reading the greeting:

| Minecraft | Fabric | NeoForge | Forge |
|---|---|---|---|
| 26.3 | yes | yes | yes |
| 1.21.1 | yes | yes | yes |
| 1.20.1 | yes | not built | yes |
| 1.7.10 | not built | not built | yes |

## Versioned code

`src/` holds the active version. The build generates the other versions from the same files, with the parts that
don't apply commented out. Mark those parts with Stonecutter comments, as the example mixin does:

```java
//? if >=1.21.6 {
String version = SharedConstants.getCurrentVersion().name();
//?} elif >=1.19 {
/*String version = SharedConstants.getCurrentVersion().getName();
*///?} else {
/*String version = RealmsSharedConstants.VERSION_STRING;
*///?}
```

The example's branches cover the four listed versions. A version between them, such as 1.16.5, needs its own.
Before 1.14.4 there are no Mojang names, so on 1.7.10 every line that touches Minecraft needs a branch.
The example mixin needs one for its imports, its target class, its method name and its chat call.

`./gradlew "Set active project to 1.20.1"` rewrites `src/` for that version, so the IDE compiles the 1.20.1 branches.
`./gradlew "Reset active project"` puts the first version back. Run it before you commit. Reload the Gradle project
in the IDE after either one, or it keeps checking the old version.

Resources are copied unchanged to every version. Files that only one version should get go in
`<module>/versions/<version>/src/main/resources`. Metadata files use `${...}` placeholders. The build fills them from
the `mod_*` entries in `gradle.properties`, every key with an underscore in its name in `stonecutter.properties.toml`,
and `minecraft_version`, `minecraft_version_range`, `minecraft_version_predicate` and `mixin_compatibility_level`.
Syntax reference: https://stonecutter.kikugie.dev/wiki/

## Rules for common

- Use only vanilla Minecraft and Mixin. The compile classpath holds more than that, Fabric Loader on most versions
  and Forge on 1.7.10. Calling those compiles in `common` and in the IDE, then fails to compile on the nodes of the
  other loaders, which build common's sources on their own classpath.
- MixinExtras ships with Fabric, NeoForge, Forge 26.x and UniMixins. Forge 1.21.1 and 1.20.1 do not have it,
  so bundle it there or leave it out.
- Write Java that the oldest listed version can compile. With 1.7.10 in the list that is Java 8. Lines inside a
  version branch may use that version's Java.
- The build sets the mixin `compatibilityLevel` from the Java version, capped at `JAVA_21`.
  Forge's Mixin rejects anything newer.
- Put client-only mixins in the `client` list so dedicated servers skip them.
- Put assets and data in `common/src/main/resources`. Forge only loads them because of
  `forge/src/main/resources/pack.mcmeta`.
