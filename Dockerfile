# =============================================================================
# Stage 1 — Build
#
# Uses the full JDK + Maven image to compile the source and produce a fat JAR.
# This stage is discarded after the build; none of its tools or source code
# end up in the final image.
#
# Dependency layer is copied and resolved before the source code so Docker
# can cache it. Subsequent builds only re-download deps when pom.xml changes.
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B


# =============================================================================
# Stage 2 — Runtime
#
# Starts from a minimal JRE-only image. No JDK, no Maven, no source code.
# The final image is roughly 200 MB vs 500+ MB for a JDK-based image.
#
# The application runs as a non-root user (appuser, UID 1000) to reduce
# the impact of any container escape or privilege escalation attempt.
# =============================================================================
FROM eclipse-temurin:17-jre-alpine

# curl is used by the HEALTHCHECK instruction below.
# It is not present in the base image by default.
RUN apk add --no-cache curl

WORKDIR /app

# Create a dedicated non-root group and user for running the application.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the compiled JAR from the builder stage.
COPY --from=builder /build/target/*.jar app.jar

# Give ownership of the working directory to the app user.
RUN chown -R appuser:appgroup /app

EXPOSE 8080

USER appuser

# JAVA_OPTS can be passed at runtime via docker run -e JAVA_OPTS="-Xmx256m"
# to tune the JVM without rebuilding the image.
ENV JAVA_OPTS=""

# Poll the Spring Boot Actuator health endpoint every 30 seconds.
# Three consecutive failures mark the container as unhealthy.
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
