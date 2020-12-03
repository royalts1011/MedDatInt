import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class Main {

    public static void main(String[] args){
        // Set up FhirContext and Client for the IMI Server
        FhirContext ctx = FhirContext.forR4();
        String serverBase = "https://funke.imi.uni-luebeck.de/public/fhir";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        // Make a new ImmunizationPass which has Patient, Immunizations,.....
        ImmunizationPass I = new ImmunizationPass(client, ctx);

        I.buildImmunizationPass();
//        I.retrieveDoctors();
    }



}
