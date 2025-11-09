import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.gradleup.shadow") version "9.2.2"
}

application.mainClass = "de.nplay.moderationbot.Bootstrapper"
group = "de.nplay"
version = "1.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
}

dependencies {
    implementation(libs.jda) {
        exclude(module = "opus-java")
    }
    implementation(libs.jdacommands)
    implementation(libs.jdwebhooks)

    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.slf4j)

    implementation(libs.postgres)
    implementation(libs.hikari)
    implementation(libs.sadu.datasource)
    implementation(libs.sadu.queries)
    implementation(libs.sadu.mapper)
    implementation(libs.sadu.postgresql)
    implementation(libs.sadu.updater)

    implementation(libs.jspecify)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.compilerArgs.add("-parameters")
    sourceCompatibility = "25"
}

tasks.withType<ShadowJar> {
    archiveFileName = "moderationbot.jar"
}
