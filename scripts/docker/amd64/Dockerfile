# This Dockerfile uses Docker Multi-Stage Builds
# See https://docs.docker.com/engine/userguide/eng-image/multistage-build/
# Requires Docker v17.05

## Base image for build and runtime
FROM openjdk:8-jre-alpine AS base

LABEL freedomotic.version="5.6.0" \
      maintainer="Matteo Mazzoni <matteo@freedomotic.com>"

# Set workdir
WORKDIR /srv

# Freedomotic release artifact location
ENV FREEDOMOTIC_URL="http://teamcity.jetbrains.com/guestAuth/repository/download/bt1177/.lastSuccessful/freedomotic-5.6.0-%7Bbuild.number%7D.zip"

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
