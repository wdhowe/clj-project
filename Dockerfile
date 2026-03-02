# Stage 1: Build the uberjar
FROM clojure:temurin-21-tools-deps-trixie-slim AS builder

WORKDIR /app

# Copy dependency files first for better layer caching
COPY deps.edn build.clj ./

# Download dependencies
RUN clojure -P && clojure -P -T:build

# Copy source files
COPY src/ src/
COPY resources/ resources/

# Inject version from build arg (git describe computed by CI or docker build)
ARG VERSION=dev
RUN echo -n "$VERSION" > resources/VERSION

# Build the uberjar
RUN clojure -T:build uber

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

# Create non-root user for security (explicit UID for K8s runAsNonRoot verification)
RUN groupadd --system --gid 1001 myapp && useradd --system --uid 1001 --gid myapp myapp

# Copy the uberjar from the build stage (wildcard handles any version)
ARG PROJECT_GROUP=com.github.wdhowe
ARG PROJECT_NAME=clj-project
COPY --from=builder /app/target/${PROJECT_GROUP}/${PROJECT_NAME}-*.jar app.jar

# Set ownership
RUN chown -R myapp:myapp /app

USER myapp

# Expose the default port
#EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# Metadata last - changes frequently, won't invalidate functional layers
LABEL org.opencontainers.image.source=https://github.com/wdhowe/clj-project
LABEL org.opencontainers.image.description="My Clojure application"

ARG COMMIT_SHA
ARG BUILD_DATE
ARG BUILD_NUMBER

LABEL org.opencontainers.image.revision="$COMMIT_SHA"
LABEL org.opencontainers.image.created="$BUILD_DATE"
LABEL org.opencontainers.image.version="$BUILD_NUMBER"
