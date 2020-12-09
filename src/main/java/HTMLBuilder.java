import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
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
     * @param immuPass
     * @return String of basic XHTML
     */
    private String generateBasicsFromCompositin(Composition immuPass) {
        return this.narrativeCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(immuPass);
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
     *
     * @param immunizationPass
     * @return
     */
    public String fullHTML(Composition immunizationPass) {

        // Generate the basic Content
        String genContent = generateBasicsFromCompositin(immunizationPass);

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

        String combinedHTML = headerTempl + genContent + "\n</body>\n" + "</html>";

        return combinedHTML;


    }
}
