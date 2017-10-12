FROM openjdk:8-jre-alpine
LABEL freedomotic.version="5.6.0"
MAINTAINER Matteo Mazzoni <matteo@freedomotic.com>

ENV FREEDOMOTIC_URL="http://teamcity.jetbrains.com/guestAuth/repository/download/bt1177/.lastSuccessful/freedomotic-5.6.0-%7Bbuild.number%7D.zip"

RUN apk add --no-cache wget curl \
    && wget "${FREEDOMOTIC_URL}" -O latest.zip \
    && apk del wget \
    && unzip latest.zip -d /srv/ \
    && rm latest.zip \
    && mv /srv/freedom* /srv/freedomotic \
    && rm -rf /srv/freedomotic/plugins/devices/frontend-java

VOLUME /srv/freedomotic/data /srv/freedomotic/plugins

EXPOSE 9111 8090

HEALTHCHECK --interval=5m --timeout=3s --start-period=10s CMD curl -fI http://localhost:8090 || exit 1

ENTRYPOINT /srv/freedomotic/freedomotic.sh -D FOREGROUND
