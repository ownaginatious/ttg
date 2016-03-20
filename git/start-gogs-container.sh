#! /bin/bash
set -e -u

[ -z "${GOGS_SSH_PORT:-}" ] && GOGS_SSH_PORT=22
[ -z "${GOGS_WEB_PORT:-}" ] && GOGS_WEB_PORT=3000

if [ -z "${1:-}" ]
then
    >&2 printf " -> Missing gogs home volume argument (arg 1)\n"
    exit 1
fi

# Check if the container is already running.
container="ttg-gogs"
running="$(docker ps --filter status=running --filter name=${container} --format {{.Names}})"
stopped="$(docker ps --filter status=exited --filter name=${container} --format {{.Names}})"

if [ ! -z "${running}" ]
then
    printf " -> Stopping the running gogs container ${container}... "
    docker stop "${container}" > /dev/null
    printf "done!\n"
    stopped="true"
fi

if [ ! -z "${stopped}" ]
then
    printf " -> Removing the stopped gogs container ${container}... "
    docker rm "${container}" > /dev/null
    printf "done!\n"
fi

printf " -> Starting a new gogs container (${container})... "
# Communication with this docker server will be integrated into the Jenkins container
docker run -d --name "${container}" \
           -p ${GOGS_SSH_PORT}:22 \
           -p ${GOGS_WEB_PORT}:3000 \
           -v $1:/data gogs/gogs > /dev/null
printf "done!\n"

docker ps --filter "name=${container}"
