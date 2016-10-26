# ttg-web

The front and backend implementations of the Timetable Generator

## How do I begin development?

### Required tools

#### Docker
[Docker](https://www.docker.com/) is a service for shipping and deploying ready-to-go software as *images*, which when instantiated run as objects called *containers*.

This differs from traditional software deployment, which usually involves uninstalling older versions and then reinstalling new versions. With docker, you simply destroy the container, download the new image, and start a new container.

For development on Linux, you can usually install `docker` through your package manager (e.g. `pacman -Sy docker` or `apt-get install docker`. etc).

For all other operating systems, install `docker` from [here](https://www.docker.com/products/docker).

#### Docker-Compose

[`docker-compose`](https://docs.docker.com/compose) is a tool used for automating starting numerous `docker` containers and connecting them together.

This is used to start all the essential services for a minimal `ttg-web` development environment as what on the surface appears to be a single atomic unit.

You can install `docker-compose` via several different methods as listed [here](https://docs.docker.com/compose/install/).

### Setting up the development environment

Once you have the above tools installed, you are ready to initialize your development environment. Follow the steps listed below.

1. Clone the [ttg-web](https://git.timetablegenerator.io/ttg/ttg-web) repository to your local machine. Cloning into a folder called something like `~/git/` is usually a good idea.
2. Clone the [ttg-saved-schedule](https://git.timetablegenerator.io/ttg/ttg-saved-schedules) repository to your local machine **in the same parent directory as `ttg-web`**. This repository contains the server necessary for supporting the ability to save and retrieve schedules. This will functionality will eventually be overtaken by `ttg_backend`.
3. Open a terminal and change the current directory to your local `ttg-web` repository.
4. Run the following command `docker compose up`. This will download and start the following containers. All container will log simultaneously to the same terminal in different colours. It will take ~1 minute to fully initialize on the first run. Read the `docker-compose` and `docker` documentation if you would like to daemonize the process (`-d`) and/or view the logs of each container separately.
	- MongoDB for supporting saved schedules
	- Python 3 for running the Django backend
	- Node for running the frontend
5. The node container will start a development server on port 8000. Navigate to `https://localhost:9000` to see your local development version of the Timetable Generator. Changing *any* of the frontend assets will cause the frontend development server to refresh, so that you should be able to immediately see changes.
6. At the moment, the local dev environment does *not* contain any scheduling data to work with. Seed the server with data from the real `http://www.timetablegenerator.io` by executing the [`./dev_seed.sh`](/dev_seed.sh) script in `ttg-web` while the test environment is running.

#### Bringing down the development environment

You can stop your development environment with the following command.

`docker-compose stop` (if daemonized) or just simply hit `ctrl-c` in the logging terminal.

You can bring it up again quickly with: `docker-compose start` or `docker-compose up`

#### Purging the development environment

If you completely screw up your environment and want to rebuild it, execute the above followed by:
`docker-compose down && rm -rf ./dev_data`

(*note*: deleting `./dev_data` may require `sudo` due to the way that `docker` works)

Following this, you will be required to do all the same setup steps again (i.e. running `./dev_seed.sh`) to get your development environment back into working order.

### Submitting changes

It is strongly advised to **not** push your changes directly to `master` as the production environment currently deploys from it (will be changing in the future).

When you want to make changes, you should instead use the following procedure:
- Make an issue (optional)
- Create a fork of the repository and do your work there (may change to branching model later).
- Submit a pull-request to merge your branch into master.
- Notify someone to have them review your work and eventually merge.

### Most important rule of all
To have fun :)
