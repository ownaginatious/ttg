# ttg-scrapers

## What is this?

`ttg-scrapers` is a CLI tool for automatically scraping information from a university website into approximately the v2 data format (JSON) and for some schools, the `v1` data format.

The is done by replaying HTTP requests to the websites to collect data, and then scraping the results using `jsoup`.

This tool was run in an automated fashion by a Jenkins server that would upload the results of scraping to the backend.

## Which schools have implementations?
_Statuses were last checked November 2018_

| School        | Key|Status           | Description  |
| ------------- |------|-------| -----|
| McMaster University | `mcmaster` |**broken** | Data is parsed directly from MOSAIC's search functionality. Requires McMaster login credentials provided via environment variables (`MCMASTER_USERNAME` and `MCMASTER_PASSWORD`). Broken due to HTML element IDs the scraping relied on changing. Supported in both the V1 and V2 format.|
| University of Ottawa    |`uottawa`  | working      |  Scrapes from public course listing website. Only supported in the V2 format. |
| University of Waterloo | `waterloo` | working    |    Scrapes from public course listing website. Only supported in the V2 format. |
| University of Western | `western` | working    |    Scrapes from public course listing website, but is slow due to rate limiting. Supported in both the V1 and V2 format.|
|University of Toronto Scarborough|`utsc`|working?|Seems to work, but was failing due to timeouts. May no longer be an issue.|
|Lakehead University|`luoc` and `lutbc`|incomplete|Incomplete implementations. What is there so far may no longer be relevant.|

## How do I use it?

Easiest way to build the jar is via `docker`. Just run `build-scraper-lib-via-docker.sh` and then run the `jar` it creates.

```bash
$ ./build-scraper-lib-via-docker.sh
$ java -jar ./build/libs/ttg-scraper.jar --help
Usage: java -jar <jar name>.jar -s SCHOOL_NAME -jo|--json-output PATH_TO_JSON_FILE [-jlo|--json-legacy-output PATH_TO_JSON_FILE

 -jlo (-json-legacy-output) VAL : The path of the legacy (V1) JSON file to
                                  write.
 -jo (-json-output) VAL         : The path of the V2 JSON file to write.
 -s (-school) VAL               : The school to scrape data for.
```

## How can I develop this in my IDE?

Easiest IDE to develop this in is [JetBrains IDEA IntelliJ](https://www.jetbrains.com/idea/).

To set that up:

1. Import the project as a `gradle` project. It has a `gradle.wrapper`; use that instead of a local `gradle` installation.
2. Wait while the dependencies are resolved.
3. Enable annotation processing in the project settings. This will allow the Java preprocessor to work (i.e. the thing that generates the console from the annotations on the scraper implementations for each school).
4. Install the `lombok` plugin. This project uses [`project Lombok`](https://projectlombok.org/) to get rid of a lot of boilerplate. Go to provided link to learn more about that.

## How can I write my own scraper?

Scrapers are implemented under `src/main/java/com/timetablegenerator/scraper/school`. As long as your implementation extends `Scraper` and includes a `SchoolConfig` annotation, then it will automatically be added to the CLI tool.

Refer to the [UTSC](/src/main/java/com/timetablegenerator/scraper/school/uoft/UofTScarboroughScraper.java) implementation as a simpler example of how to get started.

## Random questions

### Why are there `Dockerfile`s for ARM targets?

This site used to use a pool of Raspberry Pi computers for performing the scraping. There was no practical reason for this. It was just a fun experiment.

### Why does the scraper library use fancy annotation preprocessing?

It was for the minor convenience of not having to manually register new scraper implementations. Obviously, with this few scrapers that isn't exactly a lot of work. This was mostly just a learning exercise in how to write Java code that writes Java code.

### What is `ttg-java-api`?

`ttg-java-api` used to be a git submodule pointing to a particular commit in the repo that the `model` at the root of this repo was derived from.

It represents the model for the V2 format, while the `model` has since then been upgraded to the `V3` format. The scrapers were never upgraded for compatibility with that, as front and backends that are compatible with it were never created.