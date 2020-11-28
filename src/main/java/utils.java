import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class utils {

    /*
    Method which returns all Patient Names
     */
    public static void getAllNames(FhirContext ctx, String outputPath){

        IGenericClient client = ctx.newRestfulGenericClient("https://funke.imi.uni-luebeck.de/public/fhir");

        //create Bundle by searching for Patients
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();


        //Go through bundle and write name for each patient to list
        ArrayList<String> allPatientNames = new ArrayList<>();

        for(Bundle.BundleEntryComponent tmp:results.getEntry()) {
            Patient currentPatient = (Patient) tmp.getResource();
            allPatientNames.add(currentPatient.getName().get(0).getNameAsSingleString());
        }

        //Write Names to txt file
        try (FileWriter fileTXT = new FileWriter(outputPath + "allPatientNamesList.txt")) {

            for (String s : allPatientNames) {
                fileTXT.write(s + "\n");
            }
            fileTXT.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void anotherTryForHospital(FhirContext ctx, String outputPath)    {

        // Create an organization
        Organization org = new Organization();
        org.setId("Organization/65546");
        org.setName("Test Organization");

        Organization station = new Organization();
        station.setId("Station/1234");
        station.setName("Geburtsstation");

        // Create a patient
        Patient patient = new Patient();
        patient.setId("Patient/1333");
        patient.addIdentifier().setSystem("urn:mrns").setValue("253345");
        patient.getManagingOrganization().setResource(org);

        // Here we return only the patient object, which has links to other resources
        List<IBaseResource> retVal = new ArrayList<IBaseResource>();
        retVal.add(patient);

        IParser parser =  ctx.newJsonParser();
        parser.setPrettyPrint(true);

        String encode = parser.encodeResourceToString(retVal.get(0));
        System.out.println(encode);



    }


    public static void makeWholeHospital(FhirContext ctx, String outputPath){

        // Set up station and reference
        Organization station = new Organization();
        station.setName("Geburtsstation");
        station.addIdentifier().setValue(String.valueOf(new Random().nextInt(10000)));
        Reference refToStat = new Reference();
        //refToStat.setId(station.getId());
        refToStat.setResource(station);


        //Set up hospital and reference
        Organization hospital = new Organization();
        Address adressOfHospital = new Address();
        adressOfHospital.setCity("Lübeck");
        hospital.setName("MIO");
        hospital.addAddress(adressOfHospital);
        hospital.addIdentifier().setValue(String.valueOf(new Random().nextInt(10000)));
        Reference refToHosp = new Reference();
        refToHosp.setResource(hospital);


        // link staton and hospital
        hospital.getEndpoint().add(refToStat); //Station is Part of hospital.
        station.setPartOfTarget(hospital);

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
        Reference refDocStat = new Reference();
        refDocStat.setResource(doc);

        // link doc to station
        station.getEndpoint().add(refDocStat);

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
        Reference refSisStat = new Reference();
        refSisStat.setResource(sis);

        // link sis to station
        station.getEndpoint().add(refSisStat);


        //Write all to JSON file.
        String outputJSON = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(hospital);
        String outputXML = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(hospital);

        System.out.println(outputJSON);

        try
            (FileWriter fileJSON = new FileWriter(outputPath + "hospital.json")) {

            fileJSON.write(outputJSON);
            fileJSON.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try
                (FileWriter fileXML = new FileWriter(outputPath + "hospital.xml")) {

            fileXML.write(outputXML);
            fileXML.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

