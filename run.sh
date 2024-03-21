#!/bin/bash

# run.sh

# Canvia el directori de treball a on es troba l'script
cd "$(dirname "$0")"

# Configura la variable d'entorn MAVEN_OPTS
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"

if [ -z "$1" ]; then
  echo "Error: No s'ha especificat la classe principal."
  exit 1
fi

mainClass="$1"
echo "Setting MAVEN_OPTS to: $MAVEN_OPTS"
echo "Main Class: $mainClass"

# Elimina el primer argument
shift

# Construeix l'argument Maven per a la classe principal
mavenMainClassArg="-Dexec.mainClass=$mainClass"

# Executa mvn command
mvn clean compile test
mvn exec:java $mavenMainClassArg -Dexec.args="$*"

