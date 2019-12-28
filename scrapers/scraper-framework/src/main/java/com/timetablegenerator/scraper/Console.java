package com.timetablegenerator.scraper;

import com.google.gson.JsonObject;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TimeTable;
import com.timetablegenerator.scraper.annotation.LegacySchool;
import com.timetablegenerator.scraper.serialize.JsonGenerator;
import com.timetablegenerator.scraper.serialize.LegacyJsonGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Console {

    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("unused")
    @Option(name="-jlo", aliases = "-json-legacy-output", required = false,
            usage = "The path of the legacy (V1) JSON file to write.")
    private String legacyJsonOutput;

    @SuppressWarnings("unused")
    @Option(name="-jo", aliases = "-json-output", required = true,
            usage = "The path of the V2 JSON file to write.")
    private String jsonOutput;

    @SuppressWarnings("unused")
    @Option(name="-s", aliases = "-school", required = true,
            usage = "The school to scrape data for.")
    private String school;

    @SuppressWarnings("unused")
    @Option(name="-h", aliases = "--help", help = true)
    private boolean help;

    private static final String USAGE =
            "Usage: java -jar <jar name>.jar -s SCHOOL_NAME -jo|--json-output PATH_TO_JSON_FILE [-jlo|--json-legacy-output PATH_TO_JSON_FILE";

    public static void main(String[] args) throws Throwable {

        ScraperFactory scraperFactory =
                (ScraperFactory) Class.forName(ScraperFactory.class.getCanonicalName() + "Impl").newInstance();

        Console consoleArguments = new Console();

        CmdLineParser parser = new CmdLineParser(consoleArguments);

        try {

            parser.parseArgument(args);

        } catch (CmdLineException cle){

            System.err.println(cle.getMessage());
            System.err.println();
            System.err.println(USAGE);
            System.exit(1);
        }

        if (consoleArguments.help){
            System.out.println(USAGE);
            System.out.println();
            parser.printUsage(System.out);
            return;
        }

        boolean doLegacy = consoleArguments.legacyJsonOutput != null;
        boolean doModern = consoleArguments.jsonOutput != null;

        if (!doLegacy && !doModern) {
            throw new IllegalArgumentException("At least one output must be specified [-jo,-jlo]");
        }

        Scraper scraper = scraperFactory.getScraper(consoleArguments.school)
                .orElseThrow(() -> new IllegalArgumentException("Unknown school \"" + consoleArguments.school + "\""));

        School school = scraper.getSchool();

        Optional<LegacySchool> optionalLegacyConfig = scraperFactory.getLegacyConfig(consoleArguments.school);
        LegacySchool legacyConfig;

        if (doLegacy) {
            if (!optionalLegacyConfig.isPresent()) {
                throw new UnsupportedOperationException("The school \"" + school.getSchoolName()
                        + "\" does not support legacy mapping");
            }

            legacyConfig = optionalLegacyConfig.get();
        } else {
            legacyConfig = null;
        }

        Path legacyOutputPath = null;

        // Check that creation of the JSON file is feasible.
        if (doLegacy) {
            legacyOutputPath = Paths.get(consoleArguments.legacyJsonOutput);
            setupAndCheckValidity(legacyOutputPath);
        }

        Path jsonOutputPath = null;

        // Check that creation of the legacy JSON file is feasible.
        if (doModern) {
            jsonOutputPath = Paths.get(consoleArguments.jsonOutput);
            setupAndCheckValidity(jsonOutputPath);
        }

        System.out.println("Beginning content extraction for the school \"" + school.getSchoolName() + "\"...");
        System.out.println();

        Set<Term> availableTerms = scraper.findAvailableTerms();

        if (doLegacy && !availableTerms.contains(legacyConfig.getTerm())) {
            throw new IllegalStateException("Expected legacy term " + legacyConfig.getTerm()
                    + " is not available for school \"" + school.getSchoolId() + "\"");
        }

        Map<Term, TimeTable> timeTables = new HashMap<>();

        for (Term term : availableTerms) {

            LOGGER.info("Retrieving timetable for term: " + term);
            timeTables.put(term, scraper.retrieveTimetable(term));
        }

        if (doLegacy) {

            System.out.println("Writing legacy JSON to path [" + legacyOutputPath.toAbsolutePath() + "]...");
            JsonObject rootJson = LegacyJsonGenerator.INSTANCE.toJson(school,
                    optionalLegacyConfig.get(), timeTables.get(legacyConfig.getTerm()));
            Files.write(legacyOutputPath, rootJson.toString().getBytes());
        }

        if (doModern) {

            System.out.println("Writing JSON to path [" + jsonOutputPath.toAbsolutePath() + "]...");
            JsonObject rootJson = JsonGenerator.INSTANCE.toJson(school, timeTables.values());
            Files.write(jsonOutputPath, rootJson.toString().getBytes());
        }
    }

    private static void setupAndCheckValidity(Path p) throws Throwable {

        if (Files.isDirectory(p)) {
            throw new IllegalArgumentException("Output path [" + p + "] already exists as a directory.");
        }

        Files.createDirectories(p.getParent());

        if (Files.exists(p) && !Files.isWritable(p)) {
            throw new IllegalArgumentException("File [" + p + "] cannot be written to.");
        } else if (!Files.exists(p)) {
            Files.createFile(p);
        }
    }
}
