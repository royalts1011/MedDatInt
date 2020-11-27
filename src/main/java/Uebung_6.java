import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import javax.management.relation.Role;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Uebung_6 {


    public static void main(String[] args) {
        //createNewPatient();
        //getAllNames();
        makeWholeHospital();



    }
    /*
    method which creates new patient
     */
    public static void createNewPatient(){
        FhirContext ctx = FhirContext.forR4();
        Patient newPatient = new Patient();

        Identifier patID = newPatient.addIdentifier();
        int randID = new Random().nextInt(10000);
        patID.setValue(String.valueOf(randID));
        patID.setSystem("http://www.kh-uzl.de/fhir/patients");

        //Name
        HumanName name = newPatient.addName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("Gruenlich");
        name.addGiven("Antonie");

        HumanName maidenName = newPatient.addName();
        maidenName.setUse(HumanName.NameUse.MAIDEN);
        maidenName.setFamily("Bruddenbrooks");

        //Birthday
        newPatient.setBirthDateElement(new DateType("1827-08-06"));

        //Mar Status
        CodeableConcept marStatus = new CodeableConcept();
        marStatus.addCoding().setCode("M").setSystem("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus").setDisplay("Married");
        newPatient.setMaritalStatus(marStatus);

        //Picture
        File file = new File("/Users/falcolentzsch/Develope/FHIR-Project/src/main/resources/PAT_FOTO.png");
        byte[] bytes = new byte[(int)file.length()];
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            fileInputStreamReader.read(bytes);
        } catch(IOException e)    {
            System.out.println(e);
        }

        Attachment photo = new Attachment();
        photo.setData(bytes);
        newPatient.addPhoto(photo);


        //Write XML and JASON Files
        String outputJSON = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newPatient);
        String outputXML = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(newPatient);
        //Write JSON file
        try (FileWriter fileJSON = new FileWriter("newPatientJSON.json")) {

            fileJSON.write(outputJSON);
            fileJSON.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Write XML file
        try (FileWriter fileXML = new FileWriter("newPatientXML.xml")) {

            fileXML.write(outputXML);
            fileXML.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    Method which returns all Patient Names
     */
    public static void getAllNames(){
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient("https://funke.imi.uni-luebeck.de/public/fhir");

        //create Bundle by searching for Patients
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();




        //Go through bundle and write name for each patient to list
        ArrayList <String> allPatientNames = new ArrayList<>();

        for(Bundle.BundleEntryComponent tmp:results.getEntry()) {
            Patient currentPatient = (Patient) tmp.getResource();
            allPatientNames.add(currentPatient.getName().get(0).getGivenAsSingleString());
        }

        //System.out.println(ctx.newXmlParser().encodeResourceToString(results));

        //Write Names to JSON file
        try (FileWriter fileTXT = new FileWriter("allPatientNamesJSON.txt")) {

            for (String s : allPatientNames) {
                fileTXT.write(s + "\n");
            }
            fileTXT.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void makeWholeHospital(){
        FhirContext ctx = FhirContext.forR4();
        // Set up station and reference
        Organization station = new Organization();
        station.setName("Geburtsstation");
        station.addIdentifier().setValue(String.valueOf(new Random().nextInt(10000)));
        Reference refToStat = new Reference();
        refToStat.setId(station.getId());


        //Set up hospital and reference
        Organization hospital = new Organization();
        Address adressOfHospital = new Address();
        adressOfHospital.setCity("Lübeck");
        hospital.setName("MIO");
        hospital.addAddress(adressOfHospital);
        hospital.addIdentifier().setValue(String.valueOf(new Random().nextInt(10000)));
        Reference refToHosp = new Reference();
        refToHosp.setId(hospital.getId());

        station.setPartOfTarget(hospital); //Station is Part of hospital.

        //create Doctor
        Practitioner doc = new Practitioner();
        HumanName docName = new HumanName();
        docName.setUse(HumanName.NameUse.OFFICIAL);
        docName.setFamily("Wackel");
        docName.addGiven("Peter");
        doc.addName(docName);
        PractitionerRole roleOfDoc = new PractitionerRole();
        CodeableConcept conceptDoc = new CodeableConcept();
        conceptDoc.addCoding().setCode("doctor").setSystem("http://terminology.hl7.org/CodeSystem/practitioner-role").setDisplay("Doctor");
        roleOfDoc.addCode(conceptDoc);
        roleOfDoc.setOrganization(refToStat);
        roleOfDoc.setPractitionerTarget(doc);
        roleOfDoc.setOrganization(refToStat); //RoleofDoc(Doc) is Part of station.

        //create Nurse
        Practitioner sis = new Practitioner();
        HumanName sisName =  new HumanName();
        sisName.setUse(HumanName.NameUse.OFFICIAL);
        sisName.setFamily("Wackelina");
        sisName.addGiven("Petra");
        sis.addName(sisName);
        PractitionerRole roleOfSis = new PractitionerRole();
        CodeableConcept conceptSis = new CodeableConcept();
        conceptSis.addCoding().setCode("nurse").setSystem("http://terminology.hl7.org/CodeSystem/practitioner-role").setDisplay("Nurse");
        roleOfSis.addCode(conceptSis);
        roleOfSis.setOrganization(refToStat);
        roleOfSis.setPractitionerTarget(sis);
        roleOfSis.setOrganization(refToStat); //RoleofSis(Sis) is Part of station.





        //Write all to JSON file.
        String outputJSON = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(roleOfSis);
        try (FileWriter fileJSON = new FileWriter("ALL.json")) {

            fileJSON.write(outputJSON);
            fileJSON.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}


