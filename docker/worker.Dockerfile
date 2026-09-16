FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY backend/worker/gradlew backend/worker/gradlew.bat backend/worker/settings.gradle backend/worker/build.gradle ./
COPY backend/worker/gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY backend/worker/src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
