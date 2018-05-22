#!/bin/bash
set -e

TRAVIS_TAG="0.0.1";

if [ ! -z "$TRAVIS_TAG" ]; then
    echo "###################################################################"
    echo "# Creating new maven release version $TRAVIS_TAG"
    echo "###################################################################"
    echo "# setting pom.xml version to $TRAVIS_TAG"
    mvn versions:set -DgenerateBackupPoms=false -Prelease -DnewVersion=$TRAVIS_TAG

    echo "# deploy to central"
    mvn clean deploy -DskipTests=true --batch-mode --update-snapshots -Prelease
else
    echo "No release skipping..."
fi

