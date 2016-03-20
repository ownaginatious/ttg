#! /bin/bash
set -e -u

[ -z "${JENKINS_PORT:-}" ] && JENKINS_PORT=8080
[ -z "${JENKINS_SLAVE_PORT:-}" ] && JENKINS_SLAVE_PORT=50000

case "${1:-}" in
    slave) master=false ;;
    master) master=true ;;
    *) >&2 printf " -> \"${1:-}\" is not a valid mode [valid modes: slave, master]\n"; exit 1
esac

if [ -z "${2:-}" ]
then
    >&2 printf " -> Missing JENKINS_HOME argument (arg 2)\n"
    exit 1
fi

"${master}" && jenkins_type="master" || jenkins_type="slave"
"${master}" && ports="-p ${JENKINS_PORT}:8080 -p ${JENKINS_SLAVE_PORT}:50000"
container="ttg-jenkins-${jenkins_type}"
image="docker.timetablegenerator.com/ttg/jenkins-${jenkins_type}"

# Check if the container is already running.
running="$(docker ps --filter status=running --filter name=${container} --format {{.Names}})"
stopped="$(docker ps --filter status=exited --filter name=${container} --format {{.Names}})"

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

# Communication with this docker server will be integrated into the Jenkins container
docker run -d -v /var/run/docker.sock:/var/run/docker.sock \
              -v "$2":/var/jenkins_home \
              ${ports:-} \
              --name "${container}" "${image}" > /dev/null

# Hack to get docker to run as the root user. Permission
# is denied to the jenkins user on /var/run/docker.sock
# otherwise.
docker exec -u root "${container}" /bin/chmod a+s /usr/bin/docker

printf "done!\n"

docker ps --filter "name=${container}"
