import dev.kikugie.stonecutter.build.StonecutterBuildExtension

plugins {
    id("multiloader-common")
    id("me.modmuss50.mod-publish-plugin")
}

// Each loader compiles common's sources itself, so its toolchain processes the whole jar.
// The project dependency is only for IDE navigation.
val sc = the<StonecutterBuildExtension>()
val common = checkNotNull(sc.node.sibling("common")) { "No common node for ${sc.current.version}" }.project
evaluationDependsOn(common.path)
val commonMain = common.the<SourceSetContainer>()["main"]

dependencies {
    // Loom's remapping variant publishes the remapped jar. The Mojang-mapped one is in namedElements.
    compileOnly(project(common.path, common.configurations.findByName("namedElements")?.name))
}

tasks.compileJava {
    source(commonMain.java)
}

tasks.processResources {
    from(commonMain.resources)
}

// A new run directory gets what the dev server needs to let the dev client in. The client has no Minecraft account,
// and a 26.x server turns its whitelist on. The eula property agrees to https://aka.ms/MinecraftEULA.
// afterEvaluate puts this action ahead of RetroFuturaGradle's, which asks for both files on the console.
afterEvaluate {
    tasks.withType<JavaExec>().named { it == "runServer" }.configureEach {
        val run = layout.projectDirectory.dir("run").asFile
        val eula = providers.gradleProperty("eula").orNull == "true"
        doFirst {
            run.mkdirs()
            val properties = run.resolve("server.properties")
            if (!properties.exists()) properties.writeText("online-mode=false\nwhite-list=false\n")
            if (eula) run.resolve("eula.txt").writeText("eula=true\n")
        }
    }
}

// -Pjoin makes runClient connect to the dev server on localhost as soon as the game is up.
// An argument provider puts the arguments last. ModDevGradle reads its first argument as the main class.
if (providers.gradleProperty("join").isPresent) {
    val join = if (sc.current.parsed >= "1.20") listOf("--quickPlayMultiplayer", "localhost") else listOf("--server", "localhost")
    tasks.withType<JavaExec>().named { it == "runClient" }.configureEach {
        val options = layout.projectDirectory.file("run/options.txt").asFile
        argumentProviders.add(CommandLineArgumentProvider { join })
        // A new client stops at the accessibility screen and the multiplayer warning.
        doFirst {
            options.parentFile.mkdirs()
            if (!options.exists()) options.writeText("onboardAccessibility:false\nskipMultiplayerWarning:true\n")
        }
    }
}

// Each platform with a project id in gradle.properties gets the release jar. -PdryRun uploads nothing.
publishMods {
    val minecraft = sc.current.version
    val loader = sc.branch.id
    // The toolchains that remap or reobfuscate the jar ship the output of that task.
    file = provider { listOf("remapJar", "reobfJar", "jar").first(tasks.names::contains) }
        .flatMap { tasks.named<AbstractArchiveTask>(it).flatMap(AbstractArchiveTask::getArchiveFile) }
    version = project.version.toString()
    displayName = "${providers.gradleProperty("mod_name").get()} ${project.version}"
    changelog = providers.environmentVariable("CHANGELOG").orElse("")
    type = STABLE
    modLoaders.add(loader)
    dryRun = providers.gradleProperty("dryRun").isPresent
    providers.gradleProperty("modrinth_id").orNull?.takeIf(String::isNotBlank)?.let { id ->
        modrinth {
            projectId = id
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.add(minecraft)
            if (loader == "fabric") requires("fabric-api")
        }
    }
    providers.gradleProperty("curseforge_id").orNull?.takeIf(String::isNotBlank)?.let { id ->
        curseforge {
            projectId = id
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add(minecraft)
            client = true
            server = true
            if (loader == "fabric") requires("fabric-api")
        }
    }
}
