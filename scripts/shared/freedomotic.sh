#!/bin/sh
# A simple shell script to start Freedomotic

echo "Your Java version is" 
java --version
echo "Starting Freedomotic..."
SCRIPT=`readlink -f $0`
SCRIPT_PATH=`dirname $SCRIPT`
cd ${SCRIPT_PATH}
echo Running with: java -javaagent:lib/freedomotic-jar-loader-0.0.1.jar -jar ${SCRIPT_PATH}/freedomotic.jar
exec java -javaagent:lib/freedomotic-jar-loader-0.0.1.jar -splash:${SCRIPT_PATH}/splash.png -jar freedomotic.jar
read -p "Press any key to continue... " -n1 -s
