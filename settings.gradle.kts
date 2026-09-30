plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = providers.gradleProperty("mod_id").get()

include("common", "fabric", "forge", "neoforge")
