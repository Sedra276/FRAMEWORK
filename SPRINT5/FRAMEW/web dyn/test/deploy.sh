#!/bin/bash

APP_NAME="TESTFrame"

BUILD_DIR="build"

SRC_DIR="src/main/java"

WEB_DIR="src/main/webapp"

TOMCAT_WEBAPPS="/home/larissa/Documents/All/tomcat/webapps"

TOMCAT_DIR="/home/larissa/Documents/All/tomcat"

echo "Nettoyage..."
rm -rf $BUILD_DIR

echo "Création de l'arborescence WAR..."
mkdir -p $BUILD_DIR/WEB-INF/lib

echo "Copie du web.xml..."
cp -r $WEB_DIR/* $BUILD_DIR/

# Compilation des fichiers Java avec le JAR des Servlets
find $SRC_DIR -name "*.java" > sources.txt
javac -cp lib/*.jar -d $BUILD_DIR/WEB-INF/classes @sources.txt
rm sources.txt

echo "Copie du framework..."
cp lib/*.jar $BUILD_DIR/WEB-INF/lib/

# # copier les jsp s'il y en a
# cp *.jsp $BUILD_DIR/ 2>/dev/null

echo "Création du WAR..."
cd $BUILD_DIR || exit
jar -cf $APP_NAME.war *
cd ..

echo "Déploiement..."
cp "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

echo "Redémarrage Tomcat..."
if bash -c 'cat < /dev/tcp/localhost/8005' >/dev/null 2>&1; then
  echo "Tomcat en cours d'exécution, arrêt en cours..."
  "$TOMCAT_DIR/bin/shutdown.sh"
  sleep 2
else
  echo "Tomcat ne semble pas démarré, démarrage direct..."
fi

"$TOMCAT_DIR/bin/startup.sh"

echo "Déploiement terminé."
