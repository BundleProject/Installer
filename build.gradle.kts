plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.kyori.blossom") version "1.3.0"
    application
}

group = "org.bundleproject"
version = "0.2.6"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-client-gson:1.6.6")
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-apache:1.6.6")

    implementation(kotlin("stdlib-jdk8", "1.6.0"))
    implementation(kotlin("reflect", "1.6.0"))

    implementation("com.formdev:flatlaf:1.6.4")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.xenomachina:kotlin-argparser:2.0.7") {
        exclude(module = "kotlin-stdlib")
    }

    implementation("org.bundleproject:libversion:0.0.2")
}

blossom {
    val constants = "src/main/kotlin/org/bundleproject/installer/utils/Constants.kt"
    
    replaceToken("__GRADLE_VERSION__", project.version, constants)
}

application {
    mainClass.set("org.bundleproject.installer.MainKt")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
