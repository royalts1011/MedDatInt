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
        generated = searchAndInsert(generated, "Observation", "\n<h2>Observations</h2>\n");
        generated = searchAndInsert(generated, "Immunization xmlns", "\n<h2>Vaccinations</h2>\n");
        return generated;
    }


    public String enhancePass(Bundle immunizationPass) {

        // Generate the basic Content
        String genContent = generateBasicsFromBundle(immunizationPass);

        // Insert titles
        genContent = InsertTitles(genContent);

        // Load Header
        Path pathToHeaderTemplate = Paths.get("src/main/resources/templ/templ_Header.html");
        String headerTempl = "";
        try {
            headerTempl = Files.readString(pathToHeaderTemplate, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String combinedHTML = headerTempl + genContent + "\n</body>\n" + "</html>";

        return combinedHTML;


    }

}
