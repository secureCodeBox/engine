#!/bin/bash

echo "Docker Login"
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
echo "Pushing to Dockerhub"
if [[ $TRAVIS_BRANCH =~ ^master|develop$ ]]
then
    echo "Pushing all tags"
    echo $(docker push securecodebox/engine)
else
    echo "Pushing only branch Tag"
    echo $(docker push securecodebox/engine:$TAG)
fi