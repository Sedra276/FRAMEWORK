    #!/bin/bash

# Nom du framework
FRAMEWORK_NAME="framework"

# Répertoires
SRC_DIR="src"
BUILD_DIR="build"

# Tomcat
LIB_DIR="/home/sedra/Documents/dossierS1/tomcat/tomcat-10.0.16/lib"

# Avec Tomcat 10+
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"

echo "Nettoyage..."
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes

echo "Compilation..."

find $SRC_DIR -name "*.java" > sources.txt

javac \
-cp "$SERVLET_API_JAR" \
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