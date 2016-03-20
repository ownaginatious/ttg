#! /bin/bash
set -e -u

[ -z "${DOCKER_REGISTRY_PORT:-}" ] && DOCKER_REGISTRY_PORT=5000

if [ -z "${1:-}" ]
then
    >&2 printf " -> Missing docker registry home volume argument (arg 1)\n"
    exit 1
fi

# Check if the container is already running.
container="ttg-docker-registry"
running="$(docker ps --filter status=running --filter name=${container} --format {{.Names}})"
stopped="$(docker ps --filter status=exited --filter name=${container} --format {{.Names}})"

if [ ! -z "${running}" ]
then
    printf " -> Stopping running docker registry container ${container}... "
    docker stop "${container}" > /dev/null
    printf "done!\n"
    stopped="true"
fi

if [ ! -z "${stopped}" ]
then
    printf " -> Removing stopped docker registry container ${container}... "
    docker rm "${container}" > /dev/null
    printf "done!\n"
fi

printf " -> Starting a new docker registry container (${container})... "

docker run -d --name "${container}" \
           -p ${DOCKER_REGISTRY_PORT}:5000 \
           -v $1/registry:/var/lib/registry registry:2 > /dev/null

printf "done!\n"

docker ps --filter "name=${container}"

