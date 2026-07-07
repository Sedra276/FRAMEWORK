#!/bin/bash

TOMCAT_HOME="/home/sedra/Documents/dossierS1/tomcat/tomcat-10.0.16"
SERVLET_JAR="$TOMCAT_HOME/lib/servlet-api.jar"
TARGET_DIR="$TOMCAT_HOME/webapps/sprint5"
BIN_DIR="bin"

# Nettoyage
rm -rf "$BIN_DIR" framework.jar test/WEB-INF/classes "$TARGET_DIR"
mkdir -p "$BIN_DIR" test/WEB-INF/classes

# Compilation Framework & JAR
javac -d "$BIN_DIR" -cp "$SERVLET_JAR" \
    framework/src/itu/webdynamique/framework/*.java \
    framework/src/itu/webdynamique/framework/annotation/*.java \
    framework/src/itu/webdynamique/framework/util/*.java
jar -cvf framework.jar -C "$BIN_DIR" .

# Compilation Application Test
javac -d test/WEB-INF/classes -cp "framework.jar:$SERVLET_JAR" test/src/itu/webdynamique/app/controller/*.java

# Deploiement Tomcat
mkdir -p "$TARGET_DIR/WEB-INF/lib" "$TARGET_DIR/WEB-INF/classes"
cp test/WEB-INF/web.xml "$TARGET_DIR/WEB-INF/"
cp -r test/WEB-INF/classes/* "$TARGET_DIR/WEB-INF/classes/"
cp -r test/WEB-INF/views "$TARGET_DIR/WEB-INF/"
cp framework.jar "$TARGET_DIR/WEB-INF/lib/"

echo " Deploiement Sprint  termine."
