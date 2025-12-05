# Stage 1: Build the app with Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and pre-download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source files
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Render needs PORT env
ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
