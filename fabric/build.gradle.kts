plugins {
    id("multiloader-loader")
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}
