# -------------------------------------------------------------
# Stage 1: Build Application with Maven and JDK 17
# -------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source tree and compile runnable fat JAR
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------------------------------------------
# Stage 2: Ultra-lightweight Production JRE 17 Runtime
# -------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy packaged executable JAR from builder stage
COPY --from=builder /app/target/InventoryManagement-1.0-SNAPSHOT.jar app.jar

# Render injects the PORT environment variable (default 10000)
ENV PORT=10000
EXPOSE 10000

# Enforce container memory limits (512MB Render free tier safe)
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]