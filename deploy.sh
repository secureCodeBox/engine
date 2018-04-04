#!bin/bash

echo "Docker Login"
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
echo "Pushing to Dockerhub"
echo $(docker push securecodebox/engine)
