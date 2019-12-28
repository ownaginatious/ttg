#!/bin/bash
set -e -u

code_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker run --volume "${code_dir}:/code" \
           -e "GRADLE_USER_HOME=/code/.gradle" \
           -w "/code" --rm java:8 ./gradlew clean jar

