import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTMLBuilder {

    FhirContext narrativeCtx;

    public HTMLBuilder()    {
        this.narrativeCtx = FhirContext.forR4();
        String propFile = "file:src/main/resources/ImmuPass.properties";
        CustomThymeleafNarrativeGenerator generator = new CustomThymeleafNarrativeGenerator(propFile);
        this.narrativeCtx.setNarrativeGenerator(generator);
    }

    private String generateBasicsFromBundle(Bundle immunizationPass) {
        return this.narrativeCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(immunizationPass);
    }

    private String searchAndInsert(String searchIn, String searchFor, String toInsert)    {
        int idx = searchIn.indexOf(searchFor) - 2;  // -2 to get before the "<" of the Tag
        String withInsert = searchIn.substring(0, idx) + toInsert + searchIn.substring(idx-1);
        return withInsert;
    }

    private String InsertTitles(String generated)   {

        String tableHeaderImmunisations = "<div>\n" +
                "                   <table border=\"1\">\n" +
                "                     <tr>\n" +
                "                        <td> Date </td>\n" +
                "                        <td> Vaccine Code </td>\n" +
                "                        <td> Disease </td>\n" +
                "                        <td> Performing Doctor</td>\n" +
                "                     </tr>\n";

        String tableHeaderObservations = "<div>\n" +
                "                   <table border=\"1\">\n" +
                "                     <tr>\n" +
                "                        <td> Date </td>\n" +
                "                        <td> Test Code </td>\n" +
                "                        <td> Test </td>\n" +
                "                        <td> Performing Doctor</td>\n" +
                "                     </tr>\n";

        generated = searchAndInsert(generated, "Patient", "\n<h2>Patient Data</h2>\n");
        generated = searchAndInsert(generated, "Observation", "\n</table>\n</div>\n<h2>Immunization Tests</h2>\n" + tableHeaderObservations);
        generated = searchAndInsert(generated, "Immunization xmlns", "\n<h2>Vaccinations</h2>\n" + tableHeaderImmunisations);
        return generated;
    }

    private String enhanceDate(String generated)    {
        generated = generated.replaceAll("\\d\\d:\\d\\d:\\d\\d CET", "");
        generated = generated.replaceAll("\\d\\d:\\d\\d:\\d\\d CEST", "");
        String daypattern = "Mon|Tue|Wed|Thu|Fri|Sat|Sun";
        generated = generated.replaceAll(daypattern, "");
        return generated;
    }


    public String enhancePass(Bundle immunizationPass) {

        // Generate the basic Content
        String genContent = generateBasicsFromBundle(immunizationPass);

        // Some altering of stuff that don t make sense
        genContent = InsertTitles(genContent);
        genContent = enhanceDate(genContent);

        // Load Header
        Path pathToHeaderTemplate = Paths.get("src/main/resources/templ/templ_Header.html");
        String headerTempl = "";
        try {
            headerTempl = Files.readString(pathToHeaderTemplate, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String combinedHTML = headerTempl + genContent + "\n</table>\n</div>\n" + "\n</body>\n" + "</html>";

        return combinedHTML;


    }

}
