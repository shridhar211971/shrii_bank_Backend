# =========================
# Stage 1: Build the app
# =========================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven

# Copy pom.xml first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# =========================
# Stage 2: Run the app
# =========================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy generated jar
COPY --from=builder /app/target/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]