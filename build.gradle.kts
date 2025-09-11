import com.google.protobuf.gradle.id
import java.util.*

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("com.google.protobuf") version "0.9.4"
    id("org.liquibase.gradle") version "2.0.4"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.spectrum"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.redisson:redisson-spring-boot-starter:3.41.0")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core:4.31.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.1")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.1")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.1")
    implementation("com.google.protobuf:protobuf-kotlin:4.29.2")
    implementation("com.google.protobuf:protobuf-java:4.29.2") // protobuf 라이브러리
    implementation("com.google.protobuf:protobuf-java-util:4.29.2") // protobuf JSON 변환 유틸리티

    testImplementation ("org.springframework.batch:spring-batch-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest:kotest-property:5.6.2")
    testImplementation("io.mockk:mockk:1.13.14")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    getByPath("bootJar").enabled = true
    jar {
        archiveFileName.set("workfolio-server.jar")
        enabled = true
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.2"
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("kotlin")
            }
        }
    }
}

sourceSets{
    getByName("main"){
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/kotlin"
            )
        }
    }
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changeLogFile" to "$projectDir/src/main/resources/db/primary/db.changelog-master.yaml",
            "url" to localConfig["spring.datasource.url"],
            "driver" to localConfig["spring.datasource.driver-class-name"],
            "username" to localConfig["spring.datasource.username"],
            "password" to localConfig["spring.datasource.password"],
            "classpath" to "${projectDir}/src/main/resources",
        )
    }
}

val localConfig = Properties()

try {
    val f = File("$rootDir/application.properties")
    localConfig.load(f.inputStream())
} catch (ignored: java.io.IOException) {

}
