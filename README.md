# Mod template

Multiloader Minecraft mod template for Fabric, NeoForge and Forge.
The mod lives in `common`. Each loader project holds only an entrypoint and its metadata.
One file lists the Minecraft versions, and Stonecutter builds the same sources for each of them.

The example greets you in chat when you join a world or server, on every loader and version.
A mixin in `common` sends the greeting, and on Forge 1.7.10 a Forge event in `ExampleModForge` does.

```
Hello from examplemod on fabric 26.3!
```

## New mod

Set the `mod_*` entries in `gradle.properties`, then run this in the repo root with Java 11 or newer:

```
java NewMod.java
```

`mod_id` takes 2 to 64 lowercase letters, digits and `_`, and starts with a letter.
The script renames the package, the classes, the mod id and the mixin config in all four projects. That includes
the commented Stonecutter branches, which an IDE refactor skips. It names the classes after `mod_name`, puts
`mod_author` in `LICENSE`, removes this section and deletes itself. `git checkout . && git clean -fd` undoes it.
Delete the sections of `stonecutter.properties.toml` that you don't target before you write code.

## Commands

```
./gradlew build                           # mod jars in <loader>/versions/<version>/build/libs
./gradlew :fabric:26.3:runClient          # any loader, any listed version
./gradlew :fabric:26.3:runServer
./gradlew :fabric:26.3:runClient -Pjoin   # joins the dev server that is running
```

Any Java from 8 up starts Gradle. Gradle downloads the JDK it runs on and the JDK each Minecraft version needs.
On 1.7.10 the jar to ship is the one without `-dev` in its name. A commit subject starts with `feat:`, `fix:` or another
[Conventional Commits](https://www.conventionalcommits.org) prefix, because the release version comes from them.

A dev server starts once you agree to the [Minecraft EULA](https://aka.ms/MinecraftEULA). Add `-Peula=true` to the
command, or put `eula=true` in `~/.gradle/gradle.properties` to agree for every run directory.
In a new run directory the build turns off online mode and the whitelist in `server.properties`, so the dev client
can join without an account.

A Gradle sync in IntelliJ writes three run configurations per loader for the version in `src/`.
They are `Fabric Client`, `Fabric Server` and `Fabric Client (join server)`, and the same for NeoForge and Forge.
Start the server before the client that joins it.

## Releases

The release workflow is a button under Actions. It reads the commits since the last `v*` tag, picks the next
version from their prefixes, uploads every loader and Minecraft version to Modrinth and CurseForge, and creates the
tag and a GitHub release. The `feat`, `fix` and `perf` commit subjects are the changelog.

| Commit subject starts with | Before 1.0.0 | From 1.0.0 |
|---|---|---|
| `fix:` or `perf:` | patch, 0.3.0 to 0.3.1 | patch |
| `feat:` | minor, 0.3.1 to 0.4.0 | minor |
| any type with `!`, such as `feat!:`, or a body line starting `BREAKING CHANGE:` | minor | major |
| anything else, such as `docs:` or `chore:` | no release | no release |

One release takes one step, however many commits it holds. The first release is 0.1.0. For 1.0.0, type it into the
version field of the button. Each file is versioned `<mod version>+<Minecraft version>-<loader>`, such as
`0.3.1+1.7.10-forge`. Local builds are 0.0.0.

1. Set `modrinth_id` and `curseforge_id` in `gradle.properties`. A platform without an id gets nothing.
2. Add the repository secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` under Settings, Secrets and variables, Actions.
   The Modrinth token needs the scope to create versions. CurseForge issues its token under API tokens in your
   account settings.
3. Tick `dry-run` on the button to test the workflow. It builds every jar and prints what each upload would send,
   with placeholder ids and without tokens. It uploads nothing and creates no tag.

The workflow stops before the first upload when no id is set, or when a set id has no token. An upload that fails
on the platform's side can still leave others done, because Gradle uploads in parallel. The log names the failed
tasks. Run only those from your machine, on the release commit, with the version and changelog from the log:

```
CURSEFORGE_TOKEN=... CHANGELOG="- fix: ..." ./gradlew :forge:1.7.10:publishCurseforge -Pmod_version=0.3.1
gh release create v0.3.1 --target <release commit> --title 0.3.1 --notes "- fix: ..."
```

## Minecraft versions

Each Minecraft version is a section in `stonecutter.properties.toml`, for example:

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

A version gets a loader when its section sets `fabric_api_version`, `neoforge_version` or `forge_version`,
so 1.20.1 above gets Fabric and Forge but no NeoForge. Add a section to add a version.
Delete a line or a section to drop one.

The first section is the version in `src/`. To move to a new Minecraft, rename that section, update its lines and change
`stonecutter active` in `stonecutter.gradle.kts` to match. `buildSrc/build.gradle.kts` pins the loader Gradle plugins
and Stonecutter. `settings.gradle.kts` pins Stonecutter too, so bump both.

The build picks the Java version, the mixin `compatibilityLevel`, the Minecraft dependency range and the build script
from the Minecraft version:

| Loader | Minecraft | Build script | Toolchain |
|---|---|---|---|
| Fabric | 26.1 and newer | `build.gradle.kts` | Loom |
| Fabric | 1.14.4 to 1.21.x | `build.legacy.gradle.kts` | Loom with Mojang mappings |
| NeoForge | 1.21 and newer | `build.gradle.kts` | ModDevGradle |
| Forge | 1.20.6 and newer | `build.gradle.kts` | ForgeGradle |
| Forge | 1.17 to 1.20.1 | `build.legacy.gradle.kts` | ModDevGradle for legacy Forge |
| Forge | 1.7.10 | `build.retro.gradle.kts` | RetroFuturaGradle, without Mixin |

Versions outside these rows have no build script, for example Forge 1.16.5, NeoForge 1.20.x and anything from
1.8 to 1.13. If you never build the older versions, delete the legacy and retro scripts. With the retro scripts gone,
also delete the RetroFuturaGradle line and the GTNH repository in `buildSrc/build.gradle.kts`.

## Versioned code

`src/` holds the active version. The build generates the other versions from the same files, with the parts that
don't apply commented out. Mark those parts with Stonecutter comments, as the example mixin does:

```java
//? if >=1.21.6 {
String version = SharedConstants.getCurrentVersion().name();
//?} else {
/*String version = SharedConstants.getCurrentVersion().getName();
*///?}
```

Where only a name differs, a replacement comment renames it in the next line. Minecraft 1.21.11 renamed
`ResourceLocation` to `Identifier`, for example:

```java
//~ if <1.21.11 'Identifier' -> 'ResourceLocation'
import net.minecraft.resources.Identifier;
```

The example covers the versions in `stonecutter.properties.toml`. Another version, such as 1.16.5, needs a
branch wherever Minecraft differs from them. Before 1.14.4 there are no Mojang names, so on 1.7.10 every line that
touches Minecraft differs.

`./gradlew "Set active project to 1.20.1"` rewrites `src/` for that version, so the IDE compiles the 1.20.1 branches.
`./gradlew "Reset active project"` puts the first version back. Run it before you commit. Reload the Gradle project
in the IDE after either one, or it keeps checking the old version.

Every version gets the same resources. Files that only one version should get go in
`<module>/versions/<version>/src/main/resources`. The build fills `${...}` placeholders in `fabric.mod.json`,
the `mods.toml` files, `mcmod.info`, `pack.mcmeta` and the mixin config. The values are the `mod_*` entries in
`gradle.properties`, every lowercase snake_case key in `stonecutter.properties.toml`, and
`minecraft_version`, `minecraft_version_range`, `minecraft_version_predicate` and `mixin_compatibility_level`.
The [Stonecutter wiki](https://stonecutter.kikugie.dev/wiki/) documents the comment syntax.

## Rules for common

- Use only vanilla Minecraft and Mixin. The compile classpath also holds Fabric Loader on most versions and Forge
  on 1.7.10. Code that calls them compiles in `common` and in the IDE, but fails on the other loaders, which
  compile common's sources against their own classpath.
- Code that needs a loader API, such as Fabric API or loader events, goes in that loader's project and calls
  into `common`.
- MixinExtras ships with Fabric, NeoForge and Forge 26.x. Forge 1.21.1 and 1.20.1 do not have it,
  so bundle it there or leave it out.
- Forge 1.7.10 has no Mixin, and the template adds no dependency for it. The 1.7.10 build leaves the `mixin`
  package and the mixin config out, so there a Forge event in the `forge` project does the mixin's work, as in
  `ExampleModForge`. While 1.7.10 is the active version, the IDE still shows the `mixin` package, with errors.
- `org.jetbrains.annotations` is on the compile classpath for `@NotNull` and `@Nullable`.
- Write Java that the oldest listed version can compile. With 1.7.10 in the list that is Java 8. Lines inside a
  version branch may use that version's Java.
- Put mixins in the `mixins` list of the mixin config, and client-only ones in the `client` list so dedicated
  servers skip them.
- Put assets and data in `common/src/main/resources`. Forge only loads them because of
  `forge/src/main/resources/pack.mcmeta`.
