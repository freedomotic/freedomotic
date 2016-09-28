FROM java:8-jre 

MAINTAINER Matteo Mazzoni <matteo@freedomotic.com> 

RUN apt-get update && apt-get upgrade -y

RUN wget "http://teamcity.codebetter.com/guestAuth/repository/download/bt1177/.lastSuccessful/freedomotic-5.6.0-%7Bbuild.number%7D.zip" -O latest.zip
RUN unzip latest.zip -d /srv/
RUN rm latest.zip
RUN mv /srv/freedom* /srv/freedomotic
RUN rm -rf /srv/freedomotic/plugins/devices/frontend-java

VOLUME /srv/freedomotic/data
VOLUME /srv/freedomotic/plugins

EXPOSE 9111 8090

ENTRYPOINT /srv/freedomotic/freedomotic.sh -D FOREGROUND
