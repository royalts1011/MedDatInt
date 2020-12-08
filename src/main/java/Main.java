import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;

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
        Bundle immuPass = I.getWholeImmunizationPass();
        HTMLBuilder builder = new HTMLBuilder();
        String genNarr = builder.enhancePass(immuPass);


        // write HTML File to output directory
        File file = new File("output/pass_visu.html");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(genNarr);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e)   {
            e.printStackTrace();
        }

    }



}
