plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    mavenCentral()
}

group = rootProject.group
version = rootProject.version
description = "Plot System API."

dependencies {
    compileOnly(libs.io.papermc.paper.paper.api)
}

java {
    toolchain {
        languageVersion.set(rootProject.providers.gradleProperty("targetJava").map(JavaLanguageVersion::of).orElse(JavaLanguageVersion.of(25)))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(rootProject.providers.gradleProperty("targetJava").map(String::toInt).orElse(25))
}

val alpsMavenUser: String? = project.findProperty("alpsMavenUser") as String?
val alpsMavenPassword: String? = project.findProperty("alpsMavenPassword") as String?

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            credentials {
                username = alpsMavenUser
                password = alpsMavenPassword
            }
            url = uri("https://mvn.alps-bte.com/repository/alps-bte/")
        }
    }
}
