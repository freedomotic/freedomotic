#!/bin/bash

#  RUN THIS SCRIPT WITH BASH COMMAND NOT SH

workdir=$(dirname $0)
NAME="unknown"
FORCE=$1
        
function mavenize {


if [ ! -d "$BASE/src/main" ]; then
  mv $BASE/src/ $BASE/tmp
  mkdir -p $BASE/src/main/java
  mkdir -p $BASE/src/test/java
  rm -r $BASE/nbproject
  rm $BASE/build.xml
  mv $BASE/tmp/* $BASE/src/main/java
  rm -r $BASE/tmp
fi

#-f option forces pom overwriting
if [ ! -d "$BASE/src/main" ] || [[ $FORCE == "-f" ]]; then
  echo "   Writing $BASE/pom.xml file";
  echo "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>it.freedomotic</groupId>
        <artifactId>freedomotic</artifactId>
        <version>5.5-SNAPSHOT</version>
        <relativePath>../../../</relativePath>
    </parent>    
    <artifactId>$NAME</artifactId>
    <packaging>jar</packaging>
    <name>$NAME</name>
    <version>3.0</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <freedomotic.basedir>\${project.parent.basedir}</freedomotic.basedir>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>\${project.groupId}</groupId>
            <artifactId>freedomotic-core</artifactId>
            <version>\${project.parent.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- read the build.option file -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>properties-maven-plugin</artifactId>
            </plugin>
            <!-- write the build.option file with new data -->
            <plugin>
                <artifactId>maven-antrun-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
 </project>" > $BASE/pom.xml
else
  	echo "   Already Mavenized or is not a mavenizable project";
fi

}

echo $workdir
files=`ls $workdir`
for Dir in $(find $workdir/plugins/* -maxdepth 1 -type d ); 
do
    FolderName=$(basename $Dir);
    if [ -d "$Dir" ]; then 
       if [[ $FolderName != "plugins" ]] &&  [[ $FolderName != "devices" ]] && [[ $FolderName != "objects" ]]; then
         echo "Mavenization of " $FolderName
         BASE=$Dir
         NAME=$FolderName
         mavenize
       fi
    fi
done

