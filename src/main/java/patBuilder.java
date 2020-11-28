import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class patBuilder {
    FhirContext ctx;

    public patBuilder(FhirContext ctx)  {
        this.ctx = ctx;
    }

    public Patient buildPatient()    {

        Patient pat = new Patient();

        Identifier id = pat.addIdentifier();
        id.setSystem("http://www.kh-uzl.de/fhir/patients");
        int patIdVal = new Random().nextInt(10000);
        id.setValue(String.valueOf(patIdVal));

        // set name
        HumanName name = pat.addName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("Grünlichr");
        name.addGiven("Antonie");

        //set maiden name
        HumanName maidenName = pat.addName();
        maidenName.setUse(HumanName.NameUse.MAIDEN);
        maidenName.setFamily("Bruddenbrooks");

        // set Birthdate
        pat.setBirthDateElement(new DateType("1827-08-06"));

        // set marital Status
        Coding maritalCoding = new Coding();
        maritalCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus");
        maritalCoding.setCode("M");
        maritalCoding.setDisplay("married");
        CodeableConcept maritalStat = new CodeableConcept();
        maritalStat.addCoding(maritalCoding);

        pat.setMaritalStatus(maritalStat);

        // Set Photo
        File file = new File("src/main/resources/PAT_FOTO.png");
        byte[] bytes = new byte[(int)file.length()];
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            fileInputStreamReader.read(bytes);
        } catch(
                IOException e)    {
            System.out.println(e);
        }

        Attachment photo = new Attachment();
        photo.setData(bytes);
        pat.addPhoto(photo);

        String encoded = this.ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(pat);
        storeMsg(encoded, "Patient.xml");

        encoded = this.ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(pat);
        storeMsg(encoded, "Patient.json");

        return pat;
    }

    public static void storeMsg(String msg, String name) {

        File dir = new File("output");
        if(!dir.exists())   {
            dir.mkdir();
        }
        File file = new File("output/" + name);

        try {
            FileWriter writer = new FileWriter(file ,false);
            writer.write(msg);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
