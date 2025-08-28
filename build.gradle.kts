import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.gradleup.shadow") version "9.0.2"
}

application.mainClass = "de.nplay.moderationbot.Bootstrapper"
group = "de.nplay"
version = "1.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val saduVersion = "2.3.0"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:5.6.1") {
        exclude(module = "opus-java")
    }

    implementation("org.jspecify:jspecify:1.0.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("io.github.kaktushose:jda-commands:4.0.0-beta.8")
    implementation("ch.qos.logback:logback-core:1.5.13")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:6.1.0")
    implementation("de.chojo.sadu", "sadu-datasource", saduVersion)
    implementation("de.chojo.sadu", "sadu-queries", saduVersion)
    implementation("de.chojo.sadu", "sadu-mapper", saduVersion)
    implementation("de.chojo.sadu", "sadu-postgresql", saduVersion)
    implementation("de.chojo.sadu", "sadu-updater", saduVersion)

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.compilerArgs.add("-parameters")
    sourceCompatibility = "24"
}

tasks.withType<ShadowJar> {
    archiveFileName = "moderationbot.jar"
}
