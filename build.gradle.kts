import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
}

application.mainClass = "de.nplay.moderationbot.Bootstrapper"
group = "de.nplay"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jda) {
        exclude(module = "opus-java")
    }
    implementation(libs.jdacommands)
    implementation(libs.bundles.database)
    implementation(libs.bundles.logging)
    implementation(libs.jspecify)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.compilerArgs.addAll(listOf("-parameters", "--enable-preview"))
    sourceCompatibility = "25"
}

tasks.withType<JavaExec>{
    jvmArgs("--enable-preview", "--sun-misc-unsafe-memory-access=allow")
}

tasks.withType<ShadowJar> {
    archiveFileName = "moderationbot.jar"
}

spotless {
    encoding("UTF-8")

    format("misc") {
        target("*.gradle.kts", ".gitattributes", ".gitignore")

        trimTrailingWhitespace()
        endWithNewline()
    }

    java {
        target("**/*.java")
        targetExclude(".github/workflows/**", "build/**")

        importOrder("", "java", "javax", "\\#")
        forbidModuleImports()
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// separate task for potential additional formatting tasks in the future
tasks.register("format") {
    group = "verification"
    dependsOn(tasks.named("spotlessApply"))
}

tasks.named("check").configure {
    dependsOn(tasks.named("spotlessCheck"))
}
