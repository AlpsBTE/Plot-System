plugins {
    java
    alias(libs.plugins.git.semver)
    alias(libs.plugins.shadow)
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
        url = uri("https://maven.fancyspaces.net/fancynpcs/fi-releases")
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

val paperNextEnabled = providers.gradleProperty("paperNext")
    .map(String::toBoolean)
    .orElse(false)

dependencies {
    implementation(libs.com.alpsbte.canvas)
    implementation(libs.com.alpsbte.alpslib.alpslib.io)
    implementation(libs.com.alpsbte.alpslib.alpslib.hologram)
    implementation(libs.com.alpsbte.alpslib.alpslib.utils)
    implementation(libs.org.mariadb.jdbc.mariadb.java.client)
    implementation(libs.com.zaxxer.hikaricp) {
        exclude(group = "org.slf4j")
    }
    implementation(libs.com.github.querz.nbt)
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
    compileOnly(libs.io.papermc.paper.paper.api)

    if (paperNextEnabled.get()) {
        constraints {
            compileOnly("io.papermc.paper:paper-api:26.2-rc-2.build.+")
        }
    }
}

group = "com.alpsbte"

version = semver.semVersion.toString().let {
    if ("-SNAPSHOT" in it) it else semver.version // If it's a release (no .SNAPSHOT Suffix) use the version without additional metadata
}

description = "An easy to use building system for the BuildTheEarth project."

tasks.shadowJar {
    archiveClassifier = ""
    relocationPrefix = "com.alpsbte.plotsystem.shaded"
    enableAutoRelocation = true
}

tasks.assemble {
    dependsOn(tasks.shadowJar) // Ensure that the shadowJar task runs before the build task
}

tasks.jar {
    archiveClassifier = "UNSHADED"
    enabled = false // Disable the default jar task since we are using shadowJar
}

tasks.register("printNextReleaseVersion") {
    description = "Prints the next full release version"
    group = "versioning"
    val nextRelease = semver.version.removeSuffix("-SNAPSHOT")
    doLast {
        println(nextRelease)
    }
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


val targetJava = providers.gradleProperty("targetJava")
    .map(String::toInt)
    .orElse(21)

java {
    toolchain {
        languageVersion.set(targetJava.map(JavaLanguageVersion::of))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJava)
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

tasks.register<GradleBuild>("buildPaperNext") {
    group = "verification"
    description = "Builds against Java 25 and the Paper-next dependency set"

    tasks = listOf("clean", "build")

    startParameter.projectProperties.putAll(
        mapOf(
            "paperNext" to "true",
            "targetJava" to "25"
        )
    )
}