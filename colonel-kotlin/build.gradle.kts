import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    kotlin("plugin.lombok") version "2.1.20"
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(project(":colonel-common"))

    testImplementation(project(":colonel-common"))
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}
