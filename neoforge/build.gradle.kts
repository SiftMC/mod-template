plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = "${property("neoforge_version")}"

    runs {
        register("client") { client() }
        register("server") {
            server()
            programArgument("--nogui")
        }
        configureEach { disableIdeRun() }
    }

    mods {
        register("${property("mod_id")}") { sourceSet(sourceSets.main.get()) }
    }
}
