#!/bin/bash
set -e -u

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

printf " -> Purging any existing certificates [%s]... " "${1}/certs"
rm -rf "${1}/certs"
printf "done!\n"

printf " -> Copying certificates:\n"
printf "     -> From : %s\n" "${2}"
printf "     -> To   : %s... " "${1}/certs"
mkdir -p "${1}/certs"
cp -L -r "${2}"/* "${1}/certs"
chmod 444 -R "${1}/certs"/*
printf "done!\n"
