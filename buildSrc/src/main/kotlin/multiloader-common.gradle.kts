import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

plugins {
    `java-library`
}

val sc = the<StonecutterBuildExtension>()
val modId = providers.gradleProperty("mod_id").get()
val minecraftVersion = sc.current.version
val javaVersion = when {
    sc.current.parsed >= "26.1" -> 25
    sc.current.parsed >= "1.20.5" -> 21
    sc.current.parsed >= "1.18" -> 17
    sc.current.parsed >= "1.17" -> 16
    else -> 8
}

// Gradle tells projects apart by group and name. The name is the version, so the branch goes into the group.
group = "${providers.gradleProperty("mod_group").get()}.${sc.branch.id}"
version = "${providers.gradleProperty("mod_version").get()}+$minecraftVersion-${sc.branch.id}"
base.archivesName = modId

java.toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)

repositories {
    mavenCentral()
}

dependencies {
    // For IntelliJ's null checks. Not in the jar.
    compileOnly("org.jetbrains:annotations:26.1.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    from(layout.settingsDirectory.file("LICENSE"))
}

// Loader metadata can use ${name} for every mod_* entry in gradle.properties, every snake_case key in
// stonecutter.properties.toml, and the values computed here.
tasks.processResources {
    val numbers = minecraftVersion.substringBefore('-').split('.').map(String::toInt)
    // Up to 1.21 the third number is a release of its own, e.g. 1.21.1 -> 1.21.2. From 26.1 it is a hotfix, e.g. 26.3 -> 26.4.
    val next = if (numbers[0] == 1) "1.${numbers[1]}.${numbers.getOrElse(2) { 0 } + 1}" else "${numbers[0]}.${numbers[1] + 1}"
    val props = (providers.gradlePropertiesPrefixedBy("mod_").get() +
        project.extra.properties.filterKeys { it.matches(Regex("[a-z0-9]+(_[a-z0-9]+)+")) }.mapValues { it.value.toString() } +
        mapOf(
            "minecraft_version" to minecraftVersion,
            "minecraft_version_range" to "[$minecraftVersion,$next)",
            "minecraft_version_predicate" to if (numbers[0] == 1) minecraftVersion else "~$minecraftVersion",
            // Forge's Mixin rejects anything above JAVA_21.
            "mixin_compatibility_level" to "JAVA_${minOf(javaVersion, 21)}",
        ))
        // Every value lands in a JSON or TOML string, and both escape these the same way.
        .mapValues { (_, value) ->
            value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace(Regex("\\p{Cntrl}")) { "\\u%04x".format(it.value[0].code) }
        }

    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/*mods.toml", "mcmod.info", "pack.mcmeta", "*.mixins.json")) {
        expand(props)
    }
}

// Toolchains that decompile Minecraft or share files in the Gradle user home set up one project at a time.
interface Mutex : BuildService<BuildServiceParameters.None>
val mutex = gradle.sharedServices.registerIfAbsent("minecraftToolchainMutex", Mutex::class.java) { maxParallelUsages = 1 }
tasks.named { it == "createMinecraftArtifacts" }.configureEach { usesService(mutex) }

pluginManager.withPlugin("com.gtnewhorizons.retrofuturagradle") {
    tasks.named { !it.startsWith("run") }.configureEach { usesService(mutex) }
    // RetroFuturaGradle adds source sets for its own generated code. Stonecutter only handles main.
    tasks.named { Regex("stonecutter(Prepare|Generate|Merge).+").matches(it) }.configureEach {
        enabled = false
    }
}
