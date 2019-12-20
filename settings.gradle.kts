pluginManagement {
  plugins {
    repositories {
      gradlePluginPortal()
    }
    val kotlinVersion: String by extra
    val nodeGradleVersion: String by extra
    val springBootVersion: String by extra
    val versionsPluginVersion: String by extra
    val dependencyManagementVersion: String by extra
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version springBootVersion
    id("com.github.node-gradle.node") version nodeGradleVersion
    id("com.github.ben-manes.versions") version versionsPluginVersion
    id("io.spring.dependency-management") version dependencyManagementVersion
  }
}

val projectName: String by extra
rootProject.name = projectName

include(":ui")
