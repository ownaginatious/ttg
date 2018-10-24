#! /bin/bash
set -e -u

[ -z "${RABBITMQ_MANAGEMENT_PORT:-}" ] && RABBITMQ_MANAGEMENT_PORT=15672
[ -z "${RABBITMQ_AMQP_TLS_PORT:-}" ] && RABBITMQ_AMQP_TLS_PORT=5671

if [ -z "${1:-}" ]
then
    >&2 printf " -> Missing RabbitMQ home volume argument (arg 1)\n"
    exit 1
fi

if [ -z "${2:-}" ]
then
    >&2 printf " -> Missing RabbitMQ certs argument (arg 2)\n"
    exit 1
fi

# Check if the container is already running.
container="ttg-rabbitmq"
running="$(docker ps --filter status=running --filter name=${container} --format {{.Names}})"
stopped="$(docker ps --filter status=exited --filter name=${container} --format {{.Names}})"

if [ ! -z "${running}" ]
then
    printf " -> Stopping running docker RabbitMQ container ${container}... "
    docker stop "${container}" > /dev/null
    printf "done!\n"
    stopped="true"
fi

if [ ! -z "${stopped}" ]
then
    printf " -> Removing stopped docker RabbitMQ container ${container}... "
    docker rm "${container}" > /dev/null
    printf "done!\n"
fi

./refresh-certs.sh "${1}" "${2}"

printf " -> Starting a new RabbitMQ container (${container})... "

work_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Update to the latest RabbitMQ image.
docker pull rabbitmq:3-management
docker run -d --name "${container}" \
           -p ${RABBITMQ_MANAGEMENT_PORT}:15672 \
           -p ${RABBITMQ_AMQP_TLS_PORT}:5671 \
           -e "RABBITMQ_CONFIG_FILE=/rabbitmq" \
           -h "${container}" \
           -v ${work_dir}/rabbitmq.config:/rabbitmq.config \
           -v "$1":/var/lib/rabbitmq rabbitmq:3-management > /dev/null

printf "done!\n"

docker ps --filter "name=${container}"
