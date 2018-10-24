package com.timetablegenerator.scraper.school.lakehead;

import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TimeTable;
import com.timetablegenerator.scraper.utility.ParsingTools;
import com.timetablegenerator.scraper.utility.network.RestRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LakeheadScraper {

    private static final String TERM_URL_ORILLIA = "http://timetable.lakeheadu.ca/index_oril.html";
    private static final String TERM_URL_THBAY = "http://timetable.lakeheadu.ca/index_tbay.html";

    public enum Campus {ORILLIA, THUNDERBAY}

    public TimeTable retrieveTimetable(Term term) {
        throw new UnsupportedOperationException("Lakehead support has not been implemented yet");
    }

    public Set<Term> findAvailableTerms(Campus c) throws IOException {

        Set<Term> terms = new HashSet<>();

        String timetableUrl = (c == Campus.ORILLIA) ? TERM_URL_ORILLIA : TERM_URL_THBAY;

        Elements listingBlocks = Jsoup.parse(
                RestRequest.get(timetableUrl).run().getResponseString()
            ).select("#copy > div > ul");

        for (Element listingBlock : listingBlocks)
            for (Element e : listingBlock.select("li > a")) {

                String link = ParsingTools.sanitize(e.attr("href"));
                String text = ParsingTools.sanitize(e.text());

                terms.add(Term.findProbableTerm(text, link));
            }

        return terms;
    }
}
