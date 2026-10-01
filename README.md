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
./gradlew :fabric:26.3:runClient          # any loader, any listed version
./gradlew :fabric:26.3:runServer
./gradlew :fabric:26.3:runClient -Pjoin   # joins the dev server that is running
./gradlew :fabric:26.3:joinTest           # starts a server, joins it with a client, stops both
./gradlew joinTest                        # the same for every loader and version, one after the other
```

Any Java from 8 up starts Gradle. Gradle downloads the JDK it runs on and the JDK each Minecraft version needs.
On 1.7.10 the jar to ship is the one without `-dev` in its name.

A dev server starts once you agree to the [Minecraft EULA](https://aka.ms/MinecraftEULA). Put `eula=true` in
`~/.gradle/gradle.properties` to agree for every run directory, or set it in `<loader>/versions/<version>/run/eula.txt`.
The build writes the rest that a new run directory needs, so the dev client can join without an account.

`joinTest` passes when the server logs that the client joined. With `-Pexpect="some text"` it waits for that text
in the output of the server or the client instead. On Linux without a display, run it under `xvfb-run`.
`.github/workflows/build.yml` runs `build` and `joinTest` on every pull request.

A Gradle sync in IntelliJ writes three run configurations per loader for the version in `src/`.
They are `Fabric Client`, `Fabric Server` and `Fabric Client (join server)`, and the same for NeoForge and Forge.
Start the server before the client that joins it.

## New mod

Set the `mod_*` entries in `gradle.properties`, then run this in the repo root with Java 11 or newer:

```
java NewMod.java
```

`mod_id` takes 2 to 64 lowercase letters, digits and `_`, and starts with a letter.
The script renames the package, the classes, `MOD_ID` and the mixin config in all four projects. That includes
the commented Stonecutter branches, which an IDE refactor skips. It names the classes after `mod_name`, puts
`mod_author` in `LICENSE`, removes this section and deletes itself. `git checkout . && git clean -fd` undoes it.

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
and [Forge](https://files.minecraftforge.net) sites. `buildSrc/build.gradle.kts` pins the loader Gradle plugins and
Stonecutter. `settings.gradle.kts` pins Stonecutter too, so bump both.

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

Versions outside these rows have no build script, for example Forge 1.16.5, NeoForge 1.20.x and anything from
1.8 to 1.13. If you never build the older versions, delete the legacy and retro scripts. With the retro scripts gone,
also delete the RetroFuturaGradle line and the GTNH repository in `buildSrc/build.gradle.kts`.

`joinTest` passes on each of these:

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
//?} elif >=1.14 {
/*String version = SharedConstants.getCurrentVersion().getName();
*///?} else {
/*String version = RealmsSharedConstants.VERSION_STRING;
*///?}
```

Where only a name differs, a replacement comment renames it in the next line:

```java
//~ if <1.14 'handleLogin' -> 'handleJoinGame'
@Inject(method = "handleLogin", at = @At("TAIL"))
```

The example covers the four listed versions. Another version, such as 1.16.5, needs a branch wherever Minecraft
differs from them. Before 1.14.4 there are no Mojang names, so on 1.7.10 every line that touches Minecraft differs.

`./gradlew "Set active project to 1.20.1"` rewrites `src/` for that version, so the IDE compiles the 1.20.1 branches.
`./gradlew "Reset active project"` puts the first version back. Run it before you commit. Reload the Gradle project
in the IDE after either one, or it keeps checking the old version.

The build copies resources unchanged to every version. Files that only one version should get go in
`<module>/versions/<version>/src/main/resources`. The build fills `${...}` placeholders in `fabric.mod.json`,
the `mods.toml` files, `mcmod.info`, `pack.mcmeta` and the mixin config. The values are the `mod_*` entries in
`gradle.properties`, every key with an underscore in its name in `stonecutter.properties.toml`, and
`minecraft_version`, `minecraft_version_range`, `minecraft_version_predicate` and `mixin_compatibility_level`.
The [Stonecutter wiki](https://stonecutter.kikugie.dev/wiki/) documents the comment syntax.

## Rules for common

- Use only vanilla Minecraft and Mixin. The compile classpath also holds Fabric Loader on most versions and Forge
  on 1.7.10. Calling those compiles in `common` and in the IDE, then fails to compile on the nodes of the
  other loaders, which build common's sources on their own classpath.
- Code that needs a loader API, such as Fabric API or loader events, goes in that loader's project and calls
  into `common`.
- MixinExtras ships with Fabric, NeoForge, Forge 26.x and UniMixins. Forge 1.21.1 and 1.20.1 do not have it,
  so bundle it there or leave it out.
- Write Java that the oldest listed version can compile. With 1.7.10 in the list that is Java 8. Lines inside a
  version branch may use that version's Java.
- The build sets the mixin `compatibilityLevel` from the Java version, capped at `JAVA_21`.
  Forge's Mixin rejects anything newer.
- Put client-only mixins in the `client` list so dedicated servers skip them.
- Put assets and data in `common/src/main/resources`. Forge only loads them because of
  `forge/src/main/resources/pack.mcmeta`.
