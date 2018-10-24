#!/bin/bash
set -e -u

# Change to the directory of this script
code_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker run --volumes-from $(hostname) \
           -e "GRADLE_USER_HOME=${code_dir}/.gradle" \
           -w "${code_dir}" --rm java:8 ./gradlew clean jar

