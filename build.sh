#!/usr/bin/env bash

echo Building docker image


# Back
VERSION_JAR=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
BACK_IMAGE_NAME=registry.okina.fr/mobiiti/lamassu:"${VERSION_JAR}"

# Maven job is done by Jenkins


docker build -t "${BACK_IMAGE_NAME}" --build-arg JAR_FILE=target/lamassu-${VERSION_JAR}.jar .
docker push "${BACK_IMAGE_NAME}"
