import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Reference;

import java.io.File;
import java.io.FileWriter;

public class Main {

    public static void main(String[] args){
        // Set up FhirContext and Client for the IMI Server

        FhirContext ctx = FhirContext.forR4();
        String serverBase = "https://funke.imi.uni-luebeck.de/public/fhir";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        // Make a new ImmunizationPass which has Patient, Immunizations,.....
        ImmunizationPass I = new ImmunizationPass(client, ctx);
        I.buildImmunizationPass();

        // get the bundle to be able to generate visualisation
        Composition immuPass = I.getTotalImmunizationPass();
        HTMLBuilder builder = new HTMLBuilder();
        String genNarr = builder.fullHTML(immuPass);


        // write HTML File to output directory

        File file = new File("output/pass_visu.html");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(genNarr);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e)   {
            e.printStackTrace();
        }

        // Generating some Docs for better overview for presentation
         String genJson = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(immuPass);

                File file2 = new File("output/pass.json");
                try (FileWriter fileWriter = new FileWriter(file2)) {
                    fileWriter.write(genJson);
                    fileWriter.flush();
                    fileWriter.close();
                } catch (Exception e)   {
                    e.printStackTrace();
                }

                String sectionComps = "";

                for(Composition.SectionComponent section : immuPass.getSection()) {
                    sectionComps = sectionComps + "\n" + section.getTitle() + "\n";
                    for(Reference entry : section.getEntry()) {
                        IBaseResource resource = entry.getResource();
                        String tmpComp = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
                        sectionComps = sectionComps + tmpComp + "\n";
                    }
                }



    }

}
