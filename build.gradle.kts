/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2022
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = findProperty("kotlinVersion")

project.gradle.startParameter.excludedTaskNames.add("copyGeneratedJenkinsTestPluginDependencies")
project.gradle.startParameter.excludedTaskNames.add("copyTestPluginDependencies")

plugins {
    kotlin("jvm") version ("1.5.10")
    kotlin("kapt") version ("1.5.10")

    id("org.jenkins-ci.jpi") version ("0.43.0")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://zowe.jfrog.io/zowe/libs-release")
    }
}

dependencies {
    implementation(fileTree("lib"))
    compileOnly(fileTree("lib"))
    kotlin("stdlib-jre11", kotlinVersion as String)

    // Retrofit and r2z is used to run z/OSMF REST API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("org.zowe.sdk:zowe-client-kotlin-sdk:0.4.0-rc.2")

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
    includeCompileClasspath = false
}

jenkinsPlugin {
    jenkinsVersion.set("2.357")
    displayName = "Zowe z/OS DevOps"
    shortName = "zdevops"
    gitHubUrl = "https://github.com/jenkinsci/zos-devops-plugin.git"

    compatibleSinceVersion = jenkinsVersion.get()
    fileExtension = "hpi"
    pluginFirstClassLoader = true

    licenses = this.Licenses().apply {
        license(delegateClosureOf<org.jenkinsci.gradle.plugins.jpi.JpiLicense> {
            setProperty("name", "Eclipse Public License - v 2.0")
            setProperty("url", "https://www.eclipse.org/org/documents/epl-1.0/EPL-1.0.txt")
            setProperty("distribution", "repo")
        })
    }
}

tasks.withType(KotlinCompile::class.java).all {
    dependsOn("localizer")
}
