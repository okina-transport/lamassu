#!/usr/bin/env bash

echo Building docker image

VERSION_JAR=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

echo "version_jar:"$VERSION_JAR
BACK_IMAGE_NAME=registry.okina.fr/mobiiti/lamassu:"${VERSION_JAR}"
LAMASSU_JAR="lamassu-${VERSION_JAR}.jar"

cp "./target/$LAMASSU_JAR" .

docker build -t "${BACK_IMAGE_NAME}" --build-arg JAR_FILE="$LAMASSU_JAR" .

rm "$LAMASSU_JAR"

docker push "${BACK_IMAGE_NAME}"
