# Runtime stage with minimal footprint
FROM amazoncorretto:21-alpine-jdk

# Install only necessary packages
RUN apk add --no-cache tzdata curl

# Set timezone
ENV TZ=Asia/Seoul

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY build/libs/workfolio-server-boot.jar /app/app.jar

RUN echo "[Dockerfile] /app/app.jar 파일 정보:" && ls -lh /app/app.jar

# Copy run-java.sh script for optimized container execution
COPY config/script/run-java.sh /opt/run-java.sh
RUN chmod +x /opt/run-java.sh

# Set proper ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set environment variables for optimized JVM
ENV JAVA_APP_JAR=/app/app.jar
ENV JAVA_OPTIONS="-Djava.security.egd=file:/dev/./urandom"

# Use run-java.sh for optimized container execution
ENTRYPOINT ["/bin/sh", "-c", "echo '[ENTRYPOINT] 실행할 JAR: $JAVA_APP_JAR'; ls -lh $JAVA_APP_JAR; exec /opt/run-java.sh"]