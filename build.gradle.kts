buildscript {
    configurations["classpath"].resolutionStrategy.activateDependencyLocking()
}

plugins {
    kotlin("jvm") version "2.0.21"
    application
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

repositories { mavenCentral() }

group = "edu.lab"
version = "1.0.0"

dependencies {
    implementation("info.picocli:picocli:4.7.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.3")
}

kotlin { jvmToolchain(17) }
application {
    mainClass.set("lab.MainKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}
tasks.withType<JavaExec>().configureEach { jvmArgs("-Dfile.encoding=UTF-8") }
dependencyLocking { lockAllConfigurations() }
tasks.test { useJUnitPlatform(); finalizedBy(tasks.jacocoTestReport) }
jacoco { toolVersion = "0.8.12" }
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports { xml.required.set(true); html.required.set(true) }
}
detekt {
    toolVersion = "1.23.7"
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt.yml"))
}
tasks.check { dependsOn(tasks.detekt) }
