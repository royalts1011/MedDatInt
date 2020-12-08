import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTMLBuilder {

    FhirContext narrativeCtx;

    /**
     * Construktor of HTML-Builder. Sets properties File for the custom narrative generator
     */
    public HTMLBuilder()    {
        this.narrativeCtx = FhirContext.forR4();
        String propFile = "file:src/main/resources/ImmuPass.properties";
        CustomThymeleafNarrativeGenerator generator = new CustomThymeleafNarrativeGenerator(propFile);
        this.narrativeCtx.setNarrativeGenerator(generator);
    }

    /**
     * Invokes the generation of the basic HTML for the Immunization pass from the given Composition
     * @param immunizationPass
     * @return String of basic XHTML
     */
    private String generateBasicsFromCompositin(Composition immunizationPass) {
        return this.narrativeCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(immunizationPass);
    }

    /**
     * Method that searches for a String and inserts another String right before that
     * @param searchIn
     * @param searchFor
     * @param toInsert
     * @return modified String
     */
    private String searchAndInsert(String searchIn, String searchFor, String toInsert)    {
        int idx = searchIn.indexOf(searchFor) - 2;  // -2 to get before the "<" of the Tag
        String withInsert = searchIn.substring(0, idx) + toInsert + searchIn.substring(idx-1);
        return withInsert;
    }

    /**
     * Fixes the Tables by first removing all div- and table-tags und sets them new at the right places.
     * Also Inserts the missing titles for the different Tables
     * @param generated
     * @return
     */
    private String InsertTitles(String generated)   {
        generated = generated.replaceAll("<table>|</table>|<div>|</div>", "");

        String tableHeaderImmunisations = "<div>\n" +
                "                   <table border=\"1\">\n" +
                "                     <tr style=\"font-weight: bold;\">\n" +
                "                        <td> Date </td>\n" +
                "                        <td> Vaccine Code </td>\n" +
                "                        <td> Disease </td>\n" +
                "                        <td> Performing Doctor</td>\n" +
                "                     </tr>\n";

        String tableHeaderObservations = "<div>\n" +
                "                   <table border=\"1\">\n" +
                "                     <tr style=\"font-weight: bold;\">\n" +
                "                        <td> Date </td>\n" +
                "                        <td> Test Code </td>\n" +
                "                        <td> Test </td>\n" +
                "                        <td> Performing Doctor</td>\n" +
                "                     </tr>\n";

        generated = searchAndInsert(generated, "Patient", "\n<h2>Patient Data</h2>\n" + "<div>\n" + "<table border=\"1\">\n");
        generated = searchAndInsert(generated, "Observation", "\n</table>\n</div>\n<h2>Immunization Tests</h2>\n" + tableHeaderObservations);
        generated = searchAndInsert(generated, "Immunization xmlns", "\n</table>\n</div>\n<h2>Vaccinations</h2>\n" + tableHeaderImmunisations);
        return generated;
    }

    /**
     * removes day and time from the time stamps
     * @param generated
     * @return
     */
    private String enhanceDate(String generated)    {
        generated = generated.replaceAll("\\d\\d:\\d\\d:\\d\\d CET", "");
        generated = generated.replaceAll("\\d\\d:\\d\\d:\\d\\d CEST", "");
        String daypattern = "Mon|Tue|Wed|Thu|Fri|Sat|Sun";
        generated = generated.replaceAll(daypattern, "");
        return generated;
    }

    /**
     * method that uses all the previous methods to achieve a renderable HTML-file for the Immunization pass.
     * It also adds a new header Segment which contains styling etc. and the who logo.
     *
     * @param immunizationPass
     * @return
     */
    public String enhancePass(Composition immunizationPass) {

        // Generate the basic Content
        String genContent = generateBasicsFromCompositin(immunizationPass);

        // Some altering of stuff that don t make sense
       //genContent = InsertTitles(genContent);
       genContent = enhanceDate(genContent);

        // Load Header
        Path pathToHeaderTemplate = Paths.get("src/main/resources/templ/templ_Header.html");
        String headerTempl = "";
        try {
            headerTempl = Files.readString(pathToHeaderTemplate, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Stitching everything together, also adding last closing tags

        String combinedHTML = headerTempl + genContent + "\n</table>\n</div>\n" + "\n</body>\n" + "</html>";

        return combinedHTML;


    }

}
