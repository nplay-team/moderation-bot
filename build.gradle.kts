plugins {
    id("java")
}

group = "de.nplay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:5.2.0") {
        exclude(module="opus-java")
    }

    implementation("com.github.kaktushose:jda-commands:4.0.0-beta.2")
}

tasks.test {
    useJUnitPlatform()
}
