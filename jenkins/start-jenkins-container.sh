#! /bin/bash
set -e -u

# Microcontroller slaves require a different build.
if [[ "$(uname -m)" =~ ^armv6.*$ ]]
then
    arch="-armv6h"
elif [[ "$(uname -m)" =~ ^armv7.*$ ]]
then
    arch="-armv7h"
fi

[ -z "${JENKINS_PORT:-}" ] && JENKINS_PORT=8080
[ -z "${JENKINS_SLAVE_PORT:-}" ] && JENKINS_SLAVE_PORT=50000

case "${1:-}" in
    slave) master=false; slave=true ;;
    master) master=true; slave=false ;;
    *) >&2 printf " -> \"${1:-}\" is not a valid mode [valid modes: slave, master]\n"; exit 1
esac

if [ -z "${2:-}" ]
then
    >&2 printf " -> Missing JENKINS_HOME argument (arg 2)\n"
    exit 1
fi

if [ -z "${3:-}" ] && "${slave}"
then
    >&2 printf " -> Missing SSH dir argument (arg 3)\n"
    exit 1
fi

if [ -z "${4:-}" ] && "${slave}"
then
    >&2 printf " -> Missing JENKINS_SLAVE_SECRET argument (arg 4)\n"
    exit 1
fi

"${master}" && jenkins_type="master" || jenkins_type="slave"
"${master}" && ports="-p ${JENKINS_PORT}:8080 -p ${JENKINS_SLAVE_PORT}:50000" || ports=""
container="ttg-jenkins-${jenkins_type}"
image="docker.timetablegenerator.io/ttg/jenkins-${jenkins_type}${arch:-}"

# Check if the container is already running.
running="$(docker ps --filter name=${container} --format {{.Names}})"
stopped="$(docker ps -a --filter name=${container} --format {{.Names}})"

if [ ! -z "${running}" ]
then
    printf " -> Stopping running Jenkins ${jenkins_type} container \"${container}\"... "
    docker stop "${container}" > /dev/null
    printf "done!\n"
    stopped="true"
fi

if [ ! -z "${stopped}" ]
then
    printf " -> Removing the stopped Jenkins ${jenkins_type} container \"${container}\"... "
    docker rm "${container}" > /dev/null
    printf "done!\n"
fi

# Change to the directory of this script
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

printf " -> Starting new Jenkins ${jenkins_type} container (${container})... "

# Pull the latest version of the image.
docker pull "${image}"

# Non-x86 containers have to build their own images in chroots, so they should be
# given privileged access to do so.
if [ ! -z "${arch:-}" ] && "${slave}"
then
    printf " -> Container will be started with special system privileges [--privileged]"
    super_container="--privileged"
fi

# Communication with this docker server will be integrated into the Jenkins container
if "${slave}"
then
    docker run -d -v /var/run/docker.sock:/var/run/docker.sock \
                  -v "$2":/var/jenkins_home ${ports} \
                  -v "$3":/ssh_keys:ro ${ports} \
                  -e "JENKINS_SLAVE_SECRET=${4:-}" \
                  -e "JENKINS_SLAVE_ID=$(hostname)" \
                  --log-driver json-file \
                  --log-opt max-size=1m \
                  --name "${container}" ${super_container:-} "${image}" > /dev/null
else
    docker run -d -v /var/run/docker.sock:/var/run/docker.sock \
                  -v "$2":/var/jenkins_home ${ports} \
                  --name "${container}" \
                  "${image}" > /dev/null
fi

# Hack to get docker to run as the root user. Permission
# is denied to the jenkins user on /var/run/docker.sock
# otherwise.
docker exec -u root "${container}" /bin/chmod a+s /usr/bin/docker

printf "done!\n"

docker ps --filter "name=${container}"
