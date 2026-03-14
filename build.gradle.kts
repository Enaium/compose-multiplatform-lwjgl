import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    id("com.gradleup.shadow") version "9.3.0+"
}

group = "cn.enaium"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val os = when (OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "windows"
    OperatingSystem.LINUX -> "linux"
    OperatingSystem.MAC_OS -> "macos"
    else -> throw Error("Unknown OS")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.lwjgl:lwjgl:3.4.1")
    implementation("org.lwjgl:lwjgl-glfw:3.4.1")
    implementation("org.lwjgl:lwjgl-opengl:3.4.1")
    implementation("org.lwjgl:lwjgl:3.4.1:natives-$os")
    implementation("org.lwjgl:lwjgl-glfw:3.4.1:natives-$os")
    implementation("org.lwjgl:lwjgl-opengl:3.4.1:natives-$os")
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "cn.enaium.MainKt"
    }
}
