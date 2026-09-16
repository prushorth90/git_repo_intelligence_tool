FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY backend/api/gradlew backend/api/gradlew.bat backend/api/settings.gradle backend/api/build.gradle ./
COPY backend/api/gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY backend/api/src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
