# ==========================================
# Stage 1: Build the Spring Boot application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy only the pom.xml first to cache dependencies (improves build speed in docker caches)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and compile the jar file
COPY src ./src
RUN mvn package -DskipTests -B

# ==========================================
# Stage 2: Create runtime environment (JRE)
# ==========================================
FROM eclipse-temurin:17-jre-alpine

# Set security headers & parameters
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

WORKDIR /app

# Add a non-privileged user for security compliance (Avoid running containers as root!)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar file from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership of files to our non-root user
RUN chown -R appuser:appgroup /app

# Expose port 8080 (standard Spring Boot port)
EXPOSE 8080

# Switch to the non-root user
USER appuser

# Healthcheck to verify the container state (Actuator endpoint)
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Launch Spring Boot app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
