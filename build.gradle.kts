plugins {
    java
    id("com.palantir.git-version") version "4.0.0"
    id("com.gradleup.shadow") version "9.0.2"
}

repositories {
    // mavenLocal() // NEVER use in Production/Commits!
    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://mvn.alps-bte.com/repository/alps-bte/")
    }

    maven {
        url = uri("https://repo.fancyplugins.de/releases")
    }

    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        url = uri("https://repo.onarandombox.com/content/groups/public/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.com.alpsbte.canvas)
    implementation(libs.com.alpsbte.alpslib.alpslib.io)
    implementation(libs.com.alpsbte.alpslib.alpslib.hologram)
    implementation(libs.com.alpsbte.alpslib.alpslib.utils)
    implementation(libs.org.mariadb.jdbc.mariadb.java.client)
    implementation(libs.com.zaxxer.hikaricp)
    compileOnly(libs.io.papermc.paper.paper.api)
    implementation(platform(libs.com.intellectualsites.bom.bom.newest))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit)
    compileOnly(libs.multiverse.core)
    compileOnly(libs.com.github.fierioziy.particlenativeapi.particlenativeapi.plugin)
    compileOnly(libs.com.arcaniax.headdatabase.api)
    compileOnly(libs.com.github.decentsoftware.eu.decentholograms)
    compileOnly(libs.de.oliver.fancynpcs)
    compileOnly(libs.li.cinnazeyy.langlibs.api)
    compileOnly(libs.commons.io.commons.io)
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()

group = "com.alpsbte"
version = "5.0.0" // + "-" + details.commitDistance + "-" + details.gitHash + "-SNAPSHOT"
description = "An easy to use building system for the BuildTheEarth project."
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    // Exclude annotation classes (e.g. org.jetbrains.annotations)
    exclude("org/jetbrains/annotations/**")
    // Exclude slf4j classes
    exclude("org/slf4j/**")
    exclude("META-INF/**")
    archiveClassifier = ""

    val relocationPrefix = "alpsplotsystem.libs"
    relocate("com.alpsbte.alpslib", "$relocationPrefix.com.alpsbte.alpslib")
    relocate("org.mariadb.jdbc", "$relocationPrefix.org.mariadb.jdbc")
    relocate("com.zaxxer.hikari", "$relocationPrefix.com.zaxxer.hikari")
}

tasks.assemble {
    dependsOn(tasks.shadowJar) // Ensure that the shadowJar task runs before the build task
}

tasks.jar {
    enabled = false // Disable the default jar task since we are using shadowJar
}

tasks.processResources {
    // work around IDEA-296490
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    with(copySpec {
        from("src/main/resources/plugin.yml") {
            expand(
                mapOf(
                    "version" to project.version,
                    "description" to project.description
                )
            )
        }
    })
}
