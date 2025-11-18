plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("java")
    application
}

group = "bot"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

// You were missing this opening block!
dependencies {
    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // --- PDF Util ---
    implementation("org.apache.pdfbox:pdfbox:3.0.2")

    // --- Discord Bot (Local File) ---
    implementation(files("libs/JDA-5.6.1.jar"))

    // --- JDA Transitive Dependencies (Required because we use a local jar) ---
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // --- Database ---
    runtimeOnly("org.postgresql:postgresql")

    // --- Testing ---
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // --- Testcontainers (Docker Support) ---
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("bot.BotMain")
}