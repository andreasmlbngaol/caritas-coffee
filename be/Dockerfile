# ===== Build stage =====
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app

# Cache dependency resolution terpisah dari source.
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

# ===== Runtime stage =====
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Jalankan sebagai non-root.
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /app/data/uploads && chown -R app:app /app
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
