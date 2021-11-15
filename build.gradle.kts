plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.kyori.blossom") version "1.3.0"
    application
}

group = "org.bundleproject"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-gson:1.6.5")
    implementation("io.ktor:ktor-client-core:1.6.5")
    implementation("io.ktor:ktor-client-apache:1.6.5")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")

    implementation("com.formdev:flatlaf:1.6.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.xenomachina:kotlin-argparser:2.0.7") {
        exclude(module = "kotlin-stdlib")
    }
}

blossom {
    val constants = "src/main/kotlin/org/bundleproject/installer/utils/Constants.kt"
    
    replaceToken("__GRADLE_VERSION__", project.version, constants)
}

application {
    mainClass.set("org.bundleproject.installer.MainKt")
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}