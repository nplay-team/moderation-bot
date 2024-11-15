import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.gradleup.shadow") version "8.3.5"
}

application.mainClass = "de.nplay.moderationbot.Bootstrapper"
group = "de.nplay"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:5.2.1") {
        exclude(module = "opus-java")
    }

    implementation("com.github.kaktushose:jda-commands:d1e7cb6229")

    implementation("ch.qos.logback:logback-core:1.5.6")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.9")

    implementation("com.zaxxer:HikariCP:6.1.0")
    implementation("org.postgresql:postgresql:42.7.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    sourceCompatibility = "21"
}

tasks.withType<ShadowJar> {
    archiveFileName = "moderationbot.jar"
}
