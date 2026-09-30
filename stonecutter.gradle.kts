plugins {
    id("dev.kikugie.stonecutter")
}

// The version whose sources are in src/. Change it with ./gradlew "Set active project to <version>".
stonecutter active "26.3"

// IntelliJ run configurations for the loaders of the active version, rewritten on every Gradle sync.
if (System.getProperty("idea.sync.active") == "true") {
    val runs = listOf(
        Triple("Client", "runClient", ""),
        Triple("Server", "runServer", ""),
        Triple("Client (join server)", "runClient", "-Pjoin"),
    )
    val dir = file(".idea/runConfigurations").apply { mkdirs() }
    for ((loader, title) in mapOf("fabric" to "Fabric", "neoforge" to "NeoForge", "forge" to "Forge")) {
        val node = findProject(":$loader:${stonecutter.current?.project}")
        for ((run, task, parameters) in runs) {
            val file = dir.resolve("$title $run.xml")
            if (node == null) file.delete() else file.writeText(
                """
                <component name="ProjectRunConfigurationManager">
                  <configuration name="$title $run" type="GradleRunConfiguration" factoryName="Gradle">
                    <ExternalSystemSettings>
                      <option name="externalProjectPath" value="${'$'}PROJECT_DIR$" />
                      <option name="externalSystemIdString" value="GRADLE" />
                      <option name="scriptParameters" value="$parameters" />
                      <option name="taskNames"><list><option value="${node.path}:$task" /></list></option>
                    </ExternalSystemSettings>
                    <method v="2" />
                  </configuration>
                </component>
                """.trimIndent()
            )
        }
    }
}
