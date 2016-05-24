#! /bin/bash
set -e -u

if [ -z "${1:-}" ]
then
    >&2 printf " -> Missing MongoDB home volume argument (arg 1)\n"
    exit 1
fi
export TTG_MONGO_VOL="${1}"
printf " -> Destroying any existing ttg-web containers... \n\n"
docker-compose down
printf " -> Done!\n"

printf " -> Starting new ttg-web containers... \n\n"
docker-compose up -d
printf " -> Done!\n"
