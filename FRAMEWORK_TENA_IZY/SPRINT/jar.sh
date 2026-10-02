#!/bin/bash

FRAMEWORK_NAME="framework"

SRC_DIR="src"
BUILD_DIR="build"

LIB_DIR="/opt/tomcat/lib"

SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
GSON_JAR="lib/gson.jar"

echo "Nettoyage..."
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes

echo "Compilation..."

find $SRC_DIR -name "*.java" > sources.txt

javac \
-parameters \
-cp "$SERVLET_API_JAR:$GSON_JAR" \
-d "$BUILD_DIR/classes" \
@sources.txt

if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

echo "Création du JAR..."

cd "$BUILD_DIR/classes" || exit

jar cvf ../../${FRAMEWORK_NAME}.jar *

cd ../..

echo ""
echo "JAR genere : ${FRAMEWORK_NAME}.jar"
echo ""
