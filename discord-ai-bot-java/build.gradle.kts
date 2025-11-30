import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("application")
    id("java")
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.experts"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.2")

    // Discord API
    implementation("net.dv8tion:JDA:5.0.0-beta.23")

    // JSON con Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Cliente HTTP para OpenAI
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // PostgreSQL & Connection Pool
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // PDF parsing
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
}

application {
    // Clase principal del bot
    mainClass.set("bot.BotMain")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// ✅ Soporte para emojis y caracteres especiales (UTF-8)
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// ✅ CRITICAL FIX: Enable JUnit 5 Support
tasks.withType<Test> {
    useJUnitPlatform()
}

// ✅ CRITICAL FIX: Enable JUnit 5 Support AND Pass Environment Variables
tasks.withType<Test> {
    useJUnitPlatform()

    // You are MISSING these lines in your screenshot! Paste them in:
    environment("DB_URL", "jdbc:postgresql://ep-spring-rice-agpo2o6m-pooler.c-2.eu-central-1.aws.neon.tech/neondb?sslmode=require")
    environment("DB_USER", "neondb_owner")
    environment("DB_PASSWORD", "npg_1nkGuaU6KLZO")
}