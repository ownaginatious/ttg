package com.timetablegenerator.scraper.utility;

public final class ParsingTools {

    // Prevent instantiation of this class.
    private ParsingTools() {}

    /**
     * Converts non-breaking spaces into normal spaces and trims the string.
     *
     * @param string String to be sanitized.
     * @return Sanitized string.
     */
    public static String sanitize(String string) {
        return string.replace((char) 160, ' ').trim();
    }
}