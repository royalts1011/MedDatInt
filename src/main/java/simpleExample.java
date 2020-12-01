import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import org.hl7.fhir.r4.model.Patient;

public class simpleExample {
    public static void main(String[] args) {
        Patient patient = new Patient();
        patient.addIdentifier().setSystem("urn:foo").setValue("7000135");
        patient.addName().setFamily("Smith").addGiven("John").addGiven("Edward");
        patient.addAddress().addLine("742 Evergreen Terrace").setCity("Springfield").setState("ZZ");

        FhirContext ctx = FhirContext.forR4();

// Use the narrative generator
        ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());

// Encode the output, including the narrative
        String output = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(output);
    }


}
