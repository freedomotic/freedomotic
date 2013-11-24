#!/bin/sh
# A simple shell script to start Freedomotic
# Author: Enrico Nicoletti 
# Last Change: 07/10/2012

echo "Your java version is" 
java -version
echo "Strarting Freedomotic..."
SCRIPT=`readlink -f $0`
SCRIPTPATH=`dirname $SCRIPT`
cd ${SCRIPTPATH}
echo Running with: java -jar ${SCRIPTPATH}/freedomotic.jar
exec java -splash:${SCRIPTPATH}/splash.png -jar freedomotic.jar
read -p "Press any key to continue... " -n1 -s
