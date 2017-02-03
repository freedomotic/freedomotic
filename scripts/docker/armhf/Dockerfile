FROM resin/armv7hf-debian-qemu

RUN [ "cross-build-start" ]

MAINTAINER Matteo Mazzoni <matteo@freedomotic.com> 

# Set download urls
ENV FREEDOMOTIC_URL="http://teamcity.jetbrains.com/guestAuth/repository/download/bt1177/.lastSuccessful/freedomotic-5.6.0-%7Bbuild.number%7D.zip"
ENV JAVA_URL="https://www.azul.com/downloads/zulu/zdk-8-ga-linux_aarch32hf.tar.gz"

# Set variables
ENV \
    JAVA_HOME="/usr/lib/java-8"
    
# Install basepackages
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
      ca-certificates \
      locales \
      locales-all \
      netbase \
      unzip \
      wget \
      && rm -rf /var/lib/apt/lists/*

# Set locales
ENV \
    LC_ALL=en_US.UTF-8 \
    LANG=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8

# Install java
RUN wget -nv -O /tmp/java.tar.gz ${JAVA_URL} && \
    mkdir ${JAVA_HOME} && \
    tar -xvf /tmp/java.tar.gz --strip-components=1 -C ${JAVA_HOME} && \
    update-alternatives --install /usr/bin/java java ${JAVA_HOME}/bin/java 50 && \
    update-alternatives --install /usr/bin/javac javac ${JAVA_HOME}/bin/javac 50

# Install Freedomotic
RUN wget ${FREEDOMOTIC_URL} -O latest.zip && \
    unzip latest.zip -d /srv/ && \
    rm latest.zip &&\
    mv /srv/freedom* /srv/freedomotic && \
    rm -rf /srv/freedomotic/plugins/devices/frontend-java

VOLUME /srv/freedomotic/data

EXPOSE 9111 8090

ENTRYPOINT /srv/freedomotic/freedomotic.sh -D FOREGROUND

RUN [ "cross-build-end" ] 

