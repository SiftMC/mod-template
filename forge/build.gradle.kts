plugins {
    id("multiloader-loader")
    id("net.minecraftforge.gradle")
}

val mixinConfig = "${property("mod_id")}.mixins.json"

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${sc.current.version}-${property("forge_version")}"))
}

// ForgeGradle runs in runs/main/<run> by default, and the loader plugin sets up run/.
// Forge metadata has no mixin entry. Dev runs get the config as an argument, the jar through its manifest.
minecraft {
    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("run"))
            args("--mixin.config=$mixinConfig")
        }
        register("client")
        register("server") {
            args("--nogui")
        }
    }
}

tasks.jar {
    manifest.attributes["MixinConfigs"] = mixinConfig
}
