#!/bin/bash

echo "Docker Login"
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
echo "Pushing to Dockerhub"

if [[ $TRAVIS_BRANCH =~ ^master$ ]]
then
    echo "Develop Build: Tagging unstable image"
    # Also tagging master branch name to keep backwards compat
    echo $(docker tag $REPO:$TAG $REPO:master)
    echo $(docker tag $REPO:$TAG $REPO:unstable)
    echo $(docker tag $REPO:$TAG $REPO:unstable-$TRAVIS_BUILD_NUMBER)
    echo "Develop Build: Pushing unstable image"
    echo $(docker push $REPO:master)
    echo $(docker push $REPO:unstable)
    echo $(docker push $REPO:unstable-$TRAVIS_BUILD_NUMBER)
elif [ "$TRAVIS_BRANCH" = "$TRAVIS_TAG" ]
then
    echo "Tagged Release: Pushing versioned docker image." 
    echo $(docker tag $REPO:$TAG $REPO:$TRAVIS_TAG)
    echo $(docker tag $REPO:$TAG $REPO:latest)
    echo $(docker push $REPO:$TRAVIS_TAG)
    echo $(docker push $REPO:latest)
else
    echo "Feature Branch: Pushing only branch Tag"
    echo $(docker push $REPO:$TAG)
fi
