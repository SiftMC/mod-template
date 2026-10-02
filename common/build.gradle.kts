plugins {
    id("multiloader-common")
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Only for Mixin and MixinExtras. Fabric Loader API stays out of common.
    compileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
}
