import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
}

taboolib {
    env {
        // 安装模块
        install(Basic, Bukkit, BukkitHook, BukkitUtil, Database, CommandHelper, I18n)
    }
    version { taboolib = "6.2.2" }

    description {
        dependencies {
            name("WorldGuard")
            name("PlaceholderAPI").optional(true)
            name("TiatCustomStructures").optional(true)
        }
    }

    relocate("kotlinx.serialization", "me.gei.tiatareas.kotlinx.serialization163")
    relocate("ink.ptms.uw", "me.gei.tiatareas.api.uw")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    taboo(files("universal-worldedit&worldguard-1.0.0.jar"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly("com.sk89q.worldguard:worldguard-legacy:6.2")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
