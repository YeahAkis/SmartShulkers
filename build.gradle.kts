plugins {
    kotlin("jvm") version "2.2.0-Beta1"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.ajoberstar.grgit") version "5.2.0"
    id("com.modrinth.minotaur") version "2.8.7"
}

fun getGitCommitHash(): String {
    return try {
        grgit.head().abbreviatedId ?: "NULL"
    } catch (e: Exception) {
        "NULL"
    }
}

val baseVersion = property("version") as String
version = if (baseVersion.endsWith("-dev")) {
    "$baseVersion-${getGitCommitHash()}"
} else {
    baseVersion
}

group = property("group") as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("net.kyori:adventure-text-minimessage:4.20.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

}

tasks {
    runServer {
        minecraftVersion("1.21")
    }

    shadowJar {
        relocate("co.aikar.commands", "org.notionsmp.shadow.acf")
        relocate("co.aikar.locales", "org.notionsmp.shadow.locales")

        relocate("org.jetbrains.kotlin", "org.notionsmp.shadow.kotlin")

        exclude("net.kyori:.*")
        exclude("META-INF/**")

        minimize()

        archiveClassifier.set("")
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin", "src/main/java")
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

val supportedGameVersions = listOf(
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8",
    "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3"
)

modrinth {
    token.set(providers.gradleProperty("modrinthToken").orElse(providers.environmentVariable("MODRINTH_TOKEN")))
    projectId.set("smartshulker")
    versionNumber.set(project.version.toString())
    versionName.set("SmartShulkers ${project.version}")
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(supportedGameVersions)
    loaders.addAll("paper", "purpur")
    changelog.set(providers.provider { file("CHANGELOG.md").readText().substringAfter("## ").substringBefore("\n## ").substringAfter("\n").trim() })
    debugMode.set(providers.gradleProperty("modrinthDryRun").isPresent)
}
