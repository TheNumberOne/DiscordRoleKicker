/*
 * MIT License
 *
 * Copyright (c) 2020 Rosetta Roberts
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    application
    idea
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
    kotlin("kapt") version "1.3.72"
}

group = "io.github.thenumberone"
version = "1.1.0"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}


repositories {
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation("io.github.microutils:kotlin-logging:1.7.9")

    implementation("com.discord4j:discord4j-core:3.1.0-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.5")

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor.netty:reactor-netty:0.9.7.RELEASE")
    testImplementation("io.projectreactor:reactor-test")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }

    runtimeOnly("com.h2database:h2")
    implementation("com.github.TheNumberOne:r2dbc-h2:0.8.x-SNAPSHOT")
//    implementation("io.r2dbc:r2dbc-h2:0.8.3.RELEASE")

    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("com.ninja-squad:springmockk:2.0.1")

    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.0.291-SNAPSHOT")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "11"
    }
}

application {
    mainClassName = "io.github.thenumberone.discord.rolekickerbot.RoleKickerBotKt"
    applicationName = "role_kicker_bot"
}

kapt {
    includeCompileClasspath = false
}

tasks.getByName<BootBuildImage>("bootBuildImage") {
    isVerboseLogging = true
    imageName = "thenumeralone/role-kicker-bot:$version"
    isCleanCache = true
}