plugins {
    id("multiloader-common")
}

// Loaders compile common's sources themselves, so the final jar contains everything and
// each toolchain processes it; the project dependency is only for IDE navigation.
val common = layout.settingsDirectory.dir("common/src/main")

dependencies {
    compileOnly(project(":common"))
}

tasks.compileJava {
    source(common.dir("java"))
}

tasks.processResources {
    from(common.dir("resources"))
}
