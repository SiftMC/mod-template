plugins {
    `java-library`
}

val modId = providers.gradleProperty("mod_id").get()
val minecraftVersion = providers.gradleProperty("minecraft_version").get()

group = providers.gradleProperty("mod_group").get()
version = providers.gradleProperty("mod_version").get()
base.archivesName = "$modId-${project.name}-$minecraftVersion"

java.toolchain.languageVersion = JavaLanguageVersion.of(providers.gradleProperty("java_version").get())

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    from(layout.settingsDirectory.file("LICENSE"))
}

// Every gradle.properties entry can be used as ${name} in loader metadata.
tasks.processResources {
    val (major, minor) = minecraftVersion.substringBefore('-').split('.')
    val props = providers.gradlePropertiesPrefixedBy("").get() +
        // Compatible with every hotfix of this minor version, e.g. 26.3 -> [26.3,26.4)
        ("minecraft_version_range" to "[$minecraftVersion,$major.${minor.toInt() + 1})")

    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/*mods.toml", "pack.mcmeta")) {
        expand(props)
    }
}
