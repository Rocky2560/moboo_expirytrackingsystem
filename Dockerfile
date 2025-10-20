# Stage 1: Build the app with Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (cached)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Run the app with minimal JDK image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Use dynamic PORT for Render
ENV PORT=8080
EXPOSE ${PORT}

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
