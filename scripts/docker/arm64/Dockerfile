# This Dockerfile uses Docker Multi-Stage Builds
# See https://docs.docker.com/engine/userguide/eng-image/multistage-build/
# Requires Docker v17.05

## Base image for build and runtime
FROM arm64v8/openjdk:8-jre-slim AS base

LABEL freedomotic.version="5.6.0" \
      maintainer="Matteo Mazzoni <matteo@freedomotic.com>"

# Set workdir
WORKDIR /srv

# Freedomotic release artifact location
ENV FREEDOMOTIC_URL="http://teamcity.jetbrains.com/guestAuth/repository/download/bt1177/.lastSuccessful/freedomotic-5.6.0-%7Bbuild.number%7D.zip" \
    DEBIAN_FRONTEND=noninteractive \
    LC_ALL=en_US.UTF-8 \
    LANG=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8

# Copy required bins for cross-build on Dockerhub
# See https://medium.com/@kurt.stam/building-aarch64-arm-containers-on-dockerhub-d2d7c975215c
COPY --from=resin/aarch64-debian:stretch /usr/bin/cross-build-* /usr/bin/
COPY --from=resin/aarch64-debian:stretch /usr/bin/*aarch64* /usr/bin/
COPY --from=resin/aarch64-debian:stretch /usr/bin/resin-xbuild /usr/bin/

RUN [ "cross-build-start" ]

# Install build/run packages
RUN apt-get update && apt-get install -yq --no-install-recommends \
      curl \
      ca-certificates \
      && apt-get clean \
      && rm -rf /var/lib/apt/lists/*

## Build image
FROM base AS build

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

RUN [ "cross-build-end" ]
