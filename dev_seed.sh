#!/bin/bash

set -e

temp_dir=$(mktemp -d)
cd "${temp_dir}"

function clean_up {
    rm -rf "${temp_dir}"
}

trap clean_up EXIT

function fail() {
    echo ""
    echo "!!!!!!!!!!!!!!"
    echo "!!! FAILED !!!"
    echo "!!!!!!!!!!!!!!"
    echo ""
    exit 1
}

function load_data() {

    version=$1
    school=$2
    echo ""
    echo "##############################"
    echo " Retrieving V${version} data from prod: [${school}]..."
    echo "##############################"
    echo ""
    curl -f "https://www.timetablegenerator.io/api/v${version}/school/${school}"\
         --output "${school}_v${version}.json" || fail
    echo ""
    echo "############################"
    echo " Loading V${version} data to dev env: [${school}]..."
    echo "############################"
    echo ""
    curl -X POST -f --data-binary @./"${school}_v${version}.json" -H 'Content-Type: application/json' \
                 "http://localhost:7447/api/v${version}/school/${school}?token=DEV_POST_KEY" || fail
    echo ""
    echo "####################"
    echo "Refreshing cache for V${version} ${school}..."
    echo "####################"
    echo ""
    curl "http://localhost:7447/api/v${version}/school/${school}?refresh=true&token=DEV_POST_KEY" || fail
}

for school in "mcmaster" "mcmaster_summer" "western" "waterloo" "utsc"; do

    if [[ "mcmaster mcmaster_summer western utsc" =~ "${school}" ]]; then
        load_data 1 "${school}"
    fi
    if [[ "mcmaster_summer" != "${school}" ]]; then
        load_data 2 "${school}"
    fi
done
