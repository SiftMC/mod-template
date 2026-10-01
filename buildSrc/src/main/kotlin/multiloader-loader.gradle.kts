import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import kotlin.concurrent.thread
import org.gradle.tooling.GradleConnector

plugins {
    id("multiloader-common")
}

// Loaders compile common's sources themselves, so the final jar contains everything and each toolchain processes it.
// The common node's main source set is common/src/main for the active version. For every other version it is the
// output of that node's stonecutterGenerate task, which runs first because Stonecutter marks the directory as built by it.
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

val modId = providers.gradleProperty("mod_id").get()

// Forge has no mixin entry in mods.toml, so the jar manifest names the config.
val mixinConfig = "$modId.mixins.json"
if (sc.branch.id == "forge") {
    tasks.jar { manifest.attributes["MixinConfigs"] = mixinConfig }
}

// The toolchains that reobfuscate the jar write a refmap, and Mixin looks it up by this key.
// The shared config leaves the key out, because the other toolchains write no refmap.
for (plugin in listOf("net.neoforged.moddev.legacyforge", "com.gtnewhorizons.retrofuturagradle")) {
    pluginManager.withPlugin(plugin) {
        tasks.processResources {
            val config = mixinConfig
            val refmap = "\"refmap\": \"$modId.refmap.json\",\n  \"package\":"
            filesMatching(config) {
                filter { it.replace("\"package\":", refmap) }
            }
            doLast {
                check("\"refmap\"" in destinationDir.resolve(config).readText()) { "$config did not get its refmap key" }
            }
        }
    }
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
        // A new client stops at the accessibility screen and at the multiplayer warning.
        doFirst {
            options.parentFile.mkdirs()
            if (!options.exists()) options.writeText("onboardAccessibility:false\nskipMultiplayerWarning:true\n")
        }
    }
}

// joinTest runs runServer and then runClient -Pjoin as two more builds. It passes when the server logs the join,
// or when the text of -Pexpect shows up in the output of either build.
tasks.register("joinTest") {
    group = "verification"
    val root = layout.settingsDirectory.asFile
    val node = path.removeSuffix(name)
    val logs = temporaryDir
    val expect = providers.gradleProperty("expect").orElse("joined the game")
    val eula = providers.gradleProperty("eula").map { "-Peula=$it" }
    // Every dev server listens on the same port, so one test runs at a time.
    usesService(gradle.sharedServices.registrations["minecraftToolchainMutex"].service)
    doLast {
        val connection = GradleConnector.newConnector().forProjectDirectory(root).connect()
        val cancel = GradleConnector.newCancellationTokenSource()
        val builds = mutableMapOf<File, Thread>()
        fun start(task: String, vararg arguments: String) {
            val log = logs.resolve("$task.log")
            val output = log.outputStream()
            builds[log] = thread {
                // A failed or cancelled build throws. Its reason is in the log.
                runCatching {
                    connection.newBuild().forTasks(node + task).withArguments(*arguments)
                        .setStandardOutput(output).setStandardError(output).withCancellationToken(cancel.token()).run()
                }
                output.close()
            }
        }
        fun await(text: String) {
            val deadline = System.nanoTime() + 900_000_000_000
            while (builds.keys.none { text in it.readText() }) {
                val stopped = builds.entries.firstOrNull { !it.value.isAlive }?.key
                check(stopped == null && System.nanoTime() < deadline) {
                    val log = stopped ?: builds.keys.last()
                    "\"$text\" did not show up within 15 minutes, or a build stopped first. The logs are in $logs. " +
                        "${log.name} ends with:\n${log.readLines().takeLast(20).joinToString("\n")}"
                }
                Thread.sleep(1000)
            }
        }
        try {
            start("runServer", *listOfNotNull(eula.orNull).toTypedArray())
            await("Done (")
            start("runClient", "-Pjoin")
            await(expect.get())
        } finally {
            // Cancelling a build stops the game it started. close() returns when both builds have ended.
            cancel.cancel()
            connection.close()
        }
    }
}
