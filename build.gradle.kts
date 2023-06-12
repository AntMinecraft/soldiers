plugins {
    java
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.antonio32a"
version = "1.0.0"
description = "Ant Soldier"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://jitpack.io/")
}

dependencies {
    // Ant
    compileOnly("com.antonio32a:ant-core:1.0.0")

    // Paper
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")

    // scoreboard-library
    val scoreboardLibraryVersion = "2.0.0-RC8"
    implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-api:$scoreboardLibraryVersion")
    runtimeOnly("com.github.megavexnetwork.scoreboard-library:scoreboard-library-implementation:$scoreboardLibraryVersion")
    runtimeOnly("com.github.megavexnetwork.scoreboard-library:scoreboard-library-v1_19_R3:$scoreboardLibraryVersion")

    // CommandFramework (shaded by ant-core)
    compileOnly("cloud.commandframework:cloud-core:1.8.3")
    compileOnly("cloud.commandframework:cloud-annotations:1.8.3")
    compileOnly("cloud.commandframework:cloud-paper:1.8.3")
    compileOnly("cloud.commandframework:cloud-minecraft-extras:1.8.3")
    compileOnly("org.inventivetalent:reflectionhelper:1.18.13-SNAPSHOT")

    // InvUI (shaded by ant-core)
    compileOnly("xyz.xenondevs.invui:invui:1.5")

    // Lombok and misc utils
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    reobfJar {
        outputJar.set(layout.buildDirectory.file("libs/${project.name}.jar"))
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.19"
        )

        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
