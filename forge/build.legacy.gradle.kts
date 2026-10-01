// Forge before 1.20.6 runs on SRG names. ModDevGradle's legacy plugin compiles against Mojang mappings and
// reobfuscates the jar to SRG names. The mixin annotation processor writes the refmap that Mixin needs for that.
plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev.legacyforge")
}

val modId = "${property("mod_id")}"

legacyForge {
    enable { forgeVersion = "${sc.current.version}-${property("forge_version")}" }

    runs {
        register("client") { client() }
        register("server") {
            server()
            programArgument("--nogui")
        }
        configureEach { disableIdeRun() }
    }

    mods {
        register(modId) { sourceSet(sourceSets.main.get()) }
    }
}

// Dev runs read the mixin config from here. The loader plugin puts it in the jar manifest and adds the refmap key.
mixin {
    config("$modId.mixins.json")
    add(sourceSets.main.get(), "$modId.refmap.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}
