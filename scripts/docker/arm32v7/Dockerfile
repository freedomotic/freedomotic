# Build image
FROM arm32v7/eclipse-temurin:11 AS base

LABEL freedomotic.version="5.6.0" \
      maintainer="Freedomotic Team <info@freedomotic-platform.com>"

# Set workdir
WORKDIR /srv

# Freedomotic release artifact location
ENV FREEDOMOTIC_URL="https://github.com/mcicolella/freedomotic/releases/download/dailybuild/freedomotic-5.6-SNAPSHOT.zip"

## Build image
FROM base AS build

# Update && upgrade && install build packages
RUN apt-get update && apt-get install -y curl zip

# Download and install Freedomotic
RUN curl -sL -o /tmp/latest.zip "${FREEDOMOTIC_URL}" \
    && unzip /tmp/latest.zip -d /srv/ \
    && mv /srv/freedom* /srv/freedomotic \
    && rm -rf /srv/freedomotic/plugins/devices/frontend-java

## Runtime image
FROM base AS runtime

# Copy application and artifacts from build image
COPY --from=build /srv/ /srv/

VOLUME /srv/freedomotic/data /srv/freedomotic/plugins

EXPOSE 9111 8090

HEALTHCHECK --interval=5m --timeout=3s --start-period=10s CMD curl -fI http://localhost:8090 || exit 1

ENTRYPOINT /srv/freedomotic/freedomotic.sh -D FOREGROUND
