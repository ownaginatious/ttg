# The model

The `model` is the abstract data format that scrapped timetable scheduling data is loaded into. It aspires to be as generic as possible to adequetly support the largest number of schools possible.

## Formats

There are presently two supported model formats: the legacy ([v1](format/v1.md)) format and the future ([v2](format/v2/md)) format.

The legacy format is what was used for the entirety of the timetable generator's existence by the front end.

It's capable of documenting course scheduling for schools that are similar to *McMaster University*, meaning schools that only have 3 course section types: tutorials, labs, and lectures, and three term types: first semester, second semester, and full year.

This format quickly proved to be insufficient when trying to adapt to other schools, and so the future format was created with significantly better flexibility. Some schools were adapted for v1 compatibility (e.g. McMaster, UWO. etc), but others were too different to support under the old format (Waterloo, UOttawa. etc) and only have a v2 format. Unfortunately, the work was never put in to have the front end support the future format.

The v2 format was actually superceded again by a third format ([v3](format/v3.md)) before being used in production. The third format has a mostly complete Java implementation (see [here](java/)), but the scrapers were never updated to use it.

The scrapers use an older version of this library that was frozen [here](../scrapers/ttg-java-api) for the v2 format. It has an adaptation layer for transforming it into the v1 format where possible (see [here](../scrapers/scraper-framework/src/main/java/com/timetablegenerator/scraper/serialize/LegacyJsonGenerator.java)).

Documentation for all formats exists [here](../format). Ideally anyone continuing this project should look through the documentation on all formats to see the issues with v1 and v2 and v3 is best for supporting the widest number of universities. The java implementation for v3 is also by far the most tested and feature rich.
