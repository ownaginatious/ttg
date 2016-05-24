#! /bin/bash
set -e -u

if [ -z "${1:-}" ]
then
    >&2 printf " -> Missing MongoDB home volume argument (arg 1)\n"
    exit 1
fi
export TTG_MONGO_VOL="${1}"
printf " -> Destroying any existing ttg-web containers... "
docker-compose down > /dev/null
printf "done!\n"

printf " -> Starting new ttg-web containers... "
docker-compose up -d > /dev/null
printf "done!\n"
