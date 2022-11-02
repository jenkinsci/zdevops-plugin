import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = findProperty("kotlinVersion")

plugins {
    kotlin("jvm") version ("1.5.10")
    kotlin("kapt") version ("1.5.10")

    id("org.jenkins-ci.jpi") version ("0.43.0")
}

repositories {
    mavenCentral()
    flatDir {
        dirs("lib")
    }
}

dependencies {
    implementation(fileTree("lib"))
    compileOnly(fileTree("lib"))
    kotlin("stdlib", kotlinVersion as String)

    // Retrofit and r2z is used to run z/OSMF REST API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("eu.ibagroup:r2z:1.2.2")

    // Jenkins development related plugins
    implementation("org.jenkins-ci.plugins.workflow:workflow-step-api:2.23")
    implementation("org.jenkins-ci.plugins.workflow:workflow-aggregator:581.v0c46fa_697ffd")

    // SezPoz is used to process @hudson.Extension and other annotations
    kapt("net.java.sezpoz:sezpoz:1.13")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

kapt {
    correctErrorTypes = true
}

jenkinsPlugin {
    jenkinsVersion.set("2.357")
    displayName = "z/OS DevOps Plugin"
    shortName = "zos-devops"
    gitHubUrl = "https://github.com/IlyaAbnitski/zos-devops-plugin.git"

    compatibleSinceVersion = jenkinsVersion.get()
    fileExtension = "hpi"
    pluginFirstClassLoader = true
}

tasks.withType(KotlinCompile::class.java).all {
    dependsOn("localizer")
}
