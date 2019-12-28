# ttg-infrastructure

This repo holds scripts and `Dockerfile`s for getting some of the infrastructure that was used for running www.timetablegenerator.io. This probably isn't very useful to anyone salvaging anything from this project, but who knows.

## Components

### `docker`
Scripts for running a `docker` repo that contained images used by machines running the scrapers.

### `git`
Scripts for running a `git` webserver (`gogs`) as the central repository for everything.

### `jenkins`
Scripts for running the Jenkins server for coordinating scraping and scripts for Jenkins slaves running inside of `docker` locally and on remote machines.

### `rabbitmq`
Scripts for running a `rabbitmq` server for pipelining. This was never used.

### `ttg-website`
Scripts for running an `nginx` proxy to direct web requests to the appropriate service based on the endpoint.

