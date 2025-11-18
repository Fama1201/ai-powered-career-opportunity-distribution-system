# 1. Use Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# 2. Set working directory
WORKDIR /app

# 3. Copy the jar file
# NOTE: You must run './gradlew build' first to create this jar!
COPY build/libs/discord-ai-bot-java-1.0-SNAPSHOT.jar app.jar

# 4. Copy the local JDA library
COPY libs/JDA-5.6.1.jar libs/JDA-5.6.1.jar

# 5. Expose the port
EXPOSE 8081

# 6. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]