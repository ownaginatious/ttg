package com.timetablegenerator.scraper.school.mcmaster;

import com.timetablegenerator.model.Department;
import com.timetablegenerator.scraper.SupportingScraper;
import com.timetablegenerator.scraper.utility.ParsingTools;
import com.timetablegenerator.scraper.utility.network.RestResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public enum McMasterDepartmentScraper implements SupportingScraper {

    INSTANCE;

    private final Logger LOGGER = LogManager.getLogger();

    private Collection<Department> cachedDepartments;

    public synchronized Collection<Department> getDepartments() throws IOException {

        if (this.cachedDepartments == null) {
            this.cachedDepartments = retrieveDepartments();
        }

        return this.cachedDepartments;
    }

    private Collection<Department> retrieveDepartments() throws IOException {

        LOGGER.info("Navigating to resolve department information.");
        McMasterScraper scraper = new McMasterScraper();
        RestResponse rr = scraper.performAuthentication();

        String icsid = Jsoup.parse(rr.getResponseString()).getElementById("ICSID").attr("value");

        rr = rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.SSS_STUDENT_CENTER.GBL")
                .setFormParameter("ICAJAX", "1")
                .setFormParameter("ICNAVTYPEDROPDOWN", "1")
                .setFormParameter("ICType", "Panel")
                .setFormParameter("ICElementNum", "0")
                .setFormParameter("ICStateNum", "12")
                .setFormParameter("ICAction", "DERIVED_SSS_SCR_SSS_LINK_ANCHOR1")
                .setFormParameter("ICXPos", "0")
                .setFormParameter("ICYPos", "0")
                .setFormParameter("ResponsetoDiffFrame", "-1")
                .setFormParameter("TargetFrameName", "None")
                .setFormParameter("FacetPath", "None")
                .setFormParameter("ICFocus", "")
                .setFormParameter("ICSaveWarningFilter", "0")
                .setFormParameter("ICChanged", "-1")
                .setFormParameter("ICResubmit", "0")
                .setFormParameter("ICSID", icsid)
                .setFormParameter("ICActionPrompt", "false")
                .setFormParameter("ICFind", "")
                .setFormParameter("ICAddCount", "")
                .setFormParameter("ICAPPCLSDATA", "").run()
                .nextGet("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                .setQueryParameter("Page", "SSR_CLSRCH_ENTRY")
                .setQueryParameter("Action", "U")
                .setQueryParameter("ExactKeys", "Y")
                .setQueryParameter("TargetFrameName", "None").run();

        // Retrieve all the department types.
        rr = rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                .setFormParameter("ICAction", "CLASS_SRCH_WRK2_SSR_PB_SUBJ_SRCH$0").run();

        Collection<Department> departments = new TreeSet<>();

        String sectionPrefixes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < sectionPrefixes.length(); i++){

            rr = rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                    .setFormParameter("ICAction", "SSR_CLSRCH_WRK2_SSR_ALPHANUM_" + sectionPrefixes.substring(i, i + 1)).run();

            Document page = Jsoup.parse(rr.getResponseString());

            Element departmentBlock = page.getElementById("ACE_SSR_CLSRCH_SUBJ$0");

            if (departmentBlock == null)
                continue;

            int probableNumberOfSubjects = departmentBlock.select("tr").size();

            for (int j = 0; j < probableNumberOfSubjects; j++){

                Element subjectElement = page.getElementById("SSR_CLSRCH_SUBJ_SUBJECT$" + j);

                if (subjectElement != null){

                    String departmentKey = ParsingTools.sanitize(subjectElement.ownText());
                    String departmentName =
                            ParsingTools.sanitize(page.getElementById("SUBJECT_TBL_DESCRFORMAL$" + j).ownText());

                    departments.add(new Department(departmentKey, departmentName));

                    LOGGER.info("Discovered department: " + departmentKey + " -> " + departmentName);
                }
            }
        }

        // TODO: Figure out how to parse the grad course departments.
        departments.removeIf(x -> x.getCode().equals("CHILDLS"));
        return departments;
    }

    @Override
    public void updateSupportingData() {
        throw new UnsupportedOperationException("Supporting scraper support not yet implemented");
    }
}
