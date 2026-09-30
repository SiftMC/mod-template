plugins {
    id("multiloader-common")
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Only for Mixin, MixinExtras and @Environment. Never call Fabric Loader API from common.
    compileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
}
