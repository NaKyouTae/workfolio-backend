import com.google.protobuf.gradle.id
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import java.util.*

val detektVersion: String by project
val ktlintVersion: String by project

plugins {
    val kotlinVersion = "2.0.10"
    val springBootVersion = "3.4.5"
    val detektVersion = "1.23.7"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("com.google.protobuf") version "0.9.4"
    id("org.liquibase.gradle") version "2.0.4"
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.7"
    id("io.gitlab.arturbosch.detekt") version detektVersion
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:$ktlintVersion") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}
val ktlintCheck by tasks.registering(JavaExec::class) {
    inputs.files(rootProject.file(".editorconfig"), fileTree("src") { include("**/*.kt") })
    outputs.dir("${project.layout.buildDirectory.dir("reports/ktlint")}")
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

tasks.check {
    dependsOn(ktlintCheck)
}
tasks.register<JavaExec>("ktlintFormat") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style and format"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

// detekt
val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output.set(layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

group = "com.spectrum"
version = "0.0.1-SNAPSHOT"

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt/detekt.yml")
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "21"
}

detektReportMergeSarif {
    input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:$detektVersion")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.redisson:redisson-spring-boot-starter:3.41.0")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core:4.31.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.1")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.1")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.1")
    implementation("com.google.protobuf:protobuf-kotlin:4.29.2")
    implementation("com.google.protobuf:protobuf-java:4.29.2") // protobuf 라이브러리
    implementation("com.google.protobuf:protobuf-java-util:4.29.2") // protobuf JSON 변환 유틸리티

    // AWS SDK 2.x for Supabase Storage
    implementation("io.awspring.cloud:spring-cloud-aws-starter:3.4.0")
    implementation("software.amazon.awssdk:s3:2.36.3")

    testImplementation("org.springframework.batch:spring-batch-test")
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
    named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        archiveFileName.set("workfolio-server.jar")
    }
    // jar 태스크 비활성화: bootJar만 사용 (실행 가능한 JAR)
    jar {
        enabled = false
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

sourceSets {
    getByName("main") {
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/kotlin",
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
            "classpath" to "$projectDir/src/main/resources",
        )
    }
}

val localConfig = Properties()

try {
    val f = File("$rootDir/application.properties")
    localConfig.load(f.inputStream())
} catch (ignored: java.io.IOException) {
}
