plugins {
    id("application")
    id("java")
}

group = "com.experts"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Discord API
    implementation("net.dv8tion:JDA:5.0.0-beta.23")

    // JSON con Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Cliente HTTP para OpenAI
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("org.postgresql:postgresql:42.5.4")

    implementation("com.zaxxer:HikariCP:5.0.1")

}


application {
    // Esta clase se ejecuta al iniciar
    mainClass.set("bot.BotMain")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
