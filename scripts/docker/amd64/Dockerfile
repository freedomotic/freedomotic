# This Dockerfile uses Docker Multi-Stage Builds
# See https://docs.docker.com/engine/userguide/eng-image/multistage-build/
# Requires Docker v17.05

## Base image for build and runtime
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine AS base

LABEL freedomotic.version="5.6.0" \
      maintainer="Freedomotic Team <info@freedomotic-platform.com>"

# Set workdir
WORKDIR /srv

# Freedomotic release artifact location
ENV FREEDOMOTIC_URL="https://github.com/freedomotic/freedomotic/releases/download/dailybuild/freedomotic-5.6-SNAPSHOT.zip"

# Install build/run packages
RUN apk add --no-cache curl

## Build image
FROM base AS build

# Install build packages
RUN apk add --no-cache zip

# Download and install Freedomotic
RUN curl -sL -o /tmp/latest.zip "${FREEDOMOTIC_URL}"
RUN unzip /tmp/latest.zip -d /srv/
RUN mv /srv/freedom* /srv/freedomotic \
    && rm -rf /srv/freedomotic/plugins/devices/frontend-java

## Runtime image
FROM base AS runtime
# Copy application and artifacts from build image
COPY --from=build /srv/ /srv/

VOLUME /srv/freedomotic/data /srv/freedomotic/plugins

EXPOSE 9111 8090

HEALTHCHECK --interval=5m --timeout=3s --start-period=10s CMD curl -fI http://localhost:8090 || exit 1

ENTRYPOINT /srv/freedomotic/freedomotic.sh -D FOREGROUND
