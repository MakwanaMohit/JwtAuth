# ==========================================
# Stage 1: Build the Application & Extract Layers
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper & pom.xml first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies in offline mode for faster rebuilds
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the JAR (skipping unit tests for fast image creation)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Extract Spring Boot layers for optimized caching
RUN java -Djarmode=layertools -jar target/*.jar extract

# ==========================================
# Stage 2: Minimal Runtime Image
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /application

# Security: Create a non-root system group and user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy extracted layers from the builder stage
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/application/ ./

# Default PORT fallback (Render will inject $PORT automatically)
ENV PORT=8080
EXPOSE ${PORT}

# Run Spring Boot Launcher passing Render's dynamic PORT to SERVER_PORT variable
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "org.springframework.boot.loader.launch.JarLauncher"]
