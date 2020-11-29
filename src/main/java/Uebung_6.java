import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.r4.model.*;

import javax.management.relation.Role;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Uebung_6 {


    public static void main(String[] args) {
        // Create context
        FhirContext ctxR4 = FhirContext.forR4();

        /**
         * Theoretisch für weniger Server Checks bei vertrauten ServerBases. Funktioniert aber leider noch nicht.
         *
         *         // Disable server validation (don't pull the server's metadata first)
         *         ctxR4.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
         *         // configure it for deferred child scanning
         *         ctxR4.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
         */


//        createAntonie(ctxR4);
//        getAllNames(ctxR4);
        makeWholeHospital(ctxR4);

    }

    /**
     * This method creates files with custom endings.
     *
     * @param fileName    String: The name of the file inlcuding the file ending (e.g. .json)
     * @param fileContent String: The content of the file
     */
    private static void writeToFile(String fileName, String fileContent) {
        // write json file to disk
        try (FileWriter file = new FileWriter(fileName)) {

            file.write(fileContent);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * method which creates Antonie Gruenlich
     */
    public static void createAntonie(FhirContext ctx) {
        Patient newPatient = new Patient();

        Identifier patID = newPatient.addIdentifier();
        int randID = new Random().nextInt(10000);
        patID.setValue(String.valueOf(randID));
        patID.setSystem("http://www.kh-uzl.de/fhir/patients");

        //Name
        HumanName name = newPatient.addName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("Gruenlich").addGiven("Antonie");

        HumanName maidenName = newPatient.addName();
        maidenName.setUse(HumanName.NameUse.MAIDEN);
        maidenName.setFamily("Buddenbrooks");

        //Birthday
        newPatient.setBirthDateElement(new DateType("1827-08-06"));

        //Mar Status
        CodeableConcept marStatus = new CodeableConcept();
        marStatus.addCoding().setCode("M").setSystem("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus").setDisplay("Married");
        newPatient.setMaritalStatus(marStatus);

        //Picture
        File file = new File("/Users/falcolentzsch/Develope/FHIR-Project/src/main/resources/PAT_FOTO.png");
        byte[] bytes = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            fileInputStreamReader.read(bytes);
        } catch (IOException e) {
            System.out.println(e);
        }

        Attachment photo = new Attachment();
        photo.setData(bytes);
        newPatient.addPhoto(photo);


        //Write XML and JASON Files
        String outputJSON = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(newPatient);
        writeToFile("newPatientJSON.json", outputJSON);
        String outputXML = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(newPatient);
        writeToFile("newPatientXML.xml", outputXML);

    }

    /*
    Method which returns all Patient Names
     */
    public static void getAllNames(FhirContext ctx) {
        IGenericClient client = ctx.newRestfulGenericClient("https://funke.imi.uni-luebeck.de/public/fhir");

        //create Bundle by searching for Patients
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();

        List<Patient> patients = BundleUtil.toListOfResourcesOfType(ctx, results, Patient.class);

        //Go through bundle and write name for each patient to list
        String allPatientNames = "";
        for (Patient pat : patients) {
            allPatientNames += (pat.getName().get(0).getNameAsSingleString() + '\n');
        }

        //Write Names to file
        writeToFile("allPatientNames.txt", allPatientNames);

    }

    public static void makeWholeHospital(FhirContext ctx) {
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
        hospital.addContained(station);

        hospital.addIdentifier().setValue(String.valueOf(new Random().nextInt(10000)));
        Reference refToHosp = new Reference();
        refToHosp.setId(hospital.getId());
        station.setPartOf(refToHosp); //Station is Part of hospital.

        //station.setPartOfTarget(hospital);

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


        roleOfDoc.setPractitionerTarget(doc);
        roleOfDoc.setOrganization(refToStat); //RoleofDoc(Doc) is Part of station.

        //create Nurse
        Practitioner sis = new Practitioner();
        HumanName sisName = new HumanName();
        sisName.setUse(HumanName.NameUse.OFFICIAL);
        sisName.setFamily("Wackelina");
        sisName.addGiven("Petra");
        sis.addName(sisName);
        PractitionerRole roleOfSis = new PractitionerRole();
        CodeableConcept conceptSis = new CodeableConcept();
        conceptSis.addCoding().setCode("nurse").setSystem("http://terminology.hl7.org/CodeSystem/practitioner-role").setDisplay("Nurse");
        roleOfSis.addCode(conceptSis);

        roleOfSis.setPractitionerTarget(sis);
        roleOfSis.setOrganization(refToStat); //RoleofSis(Sis) is Part of station.

//        Set<String> el = new HashSet<String>();
//        el.add("HumanName");
//        el.add("Practitioner");
//        .setEncodeElements(el)

        List<Resource> all_contained = hospital.getContained();
        Bundle b = new Bundle();

        //Write all to JSON file.
        String outputJSON = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(hospital);
        writeToFile("ALL.json", outputJSON);


    }

    public static void makeWholeHospital(FhirContext ctx, String outputPath) {

        // Set up station and reference
        Organization station = new Organization();
        station.setName("Geburtsstation");
        station.addIdentifier().setValue(String.valueOf(new Random().nextInt(10000)));
        Reference refToStat = new Reference();
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
        roleOfDoc.setPractitionerTarget(doc);
        roleOfDoc.setOrganization(refToStat); //RoleofDoc(Doc) is Part of station.
        Reference refDocStat = new Reference();
        refDocStat.setResource(doc);

        // link doc to station
        station.getEndpoint().add(refDocStat);

        //create Nurse
        Practitioner sis = new Practitioner();
        HumanName sisName = new HumanName();
        sisName.setUse(HumanName.NameUse.OFFICIAL);
        sisName.setFamily("Wackelina");
        sisName.addGiven("Petra");
        sis.addName(sisName);
        PractitionerRole roleOfSis = new PractitionerRole();
        CodeableConcept conceptSis = new CodeableConcept();
        conceptSis.addCoding().setCode("nurse").setSystem("http://terminology.hl7.org/CodeSystem/practitioner-role").setDisplay("Nurse");
        roleOfSis.addCode(conceptSis);
        roleOfSis.setPractitionerTarget(sis);
        roleOfSis.setOrganization(refToStat); //RoleofSis(Sis) is Part of station.
        Reference refSisStat = new Reference();
        refSisStat.setResource(sis);

        // link sis to station
        station.getEndpoint().add(refSisStat);


        // get the patient for the planned visit
        patBuilder patBuild = new patBuilder(ctx);
        Patient mother = patBuild.buildPatient();
        Reference refToMother = new Reference(mother);

        Encounter.EncounterParticipantComponent motherComponent = new Encounter.EncounterParticipantComponent();
        motherComponent.setIndividualTarget(mother);
        Encounter.EncounterParticipantComponent stationComponent = new Encounter.EncounterParticipantComponent();
        stationComponent.setIndividualTarget(station);
        Appointment.AppointmentParticipantComponent mot = new Appointment.AppointmentParticipantComponent();
        mot.setActor(refToMother);

        // Make Appointment Date
        Appointment appoint = new Appointment();
        Calendar cal = Calendar.getInstance();
        cal.set(1846, 10, 1);
        appoint.setStart(cal.getTime());
        Reference refToAppoint = new Reference(appoint);

        // from ValueSet https://fhir-ru.github.io/v3/ActEncounterCode/vs.html
        Coding typeOfEncounter = new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "IMP", "inpatient encounter");
//        Coding typeOfEncounter = new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "PRENC", "pre-admission");
        Encounter doomsday = new Encounter();
        doomsday.setStatus(Encounter.EncounterStatus.PLANNED);
        doomsday.setClass_(typeOfEncounter);


        // Connect substancial Encounter parts
        doomsday.addAppointment(refToAppoint);
        doomsday.addParticipant(motherComponent).addParticipant(stationComponent);
        Reference refToEncounter = new Reference(doomsday);

        station.getEndpoint().add(refToEncounter);


        //Write all to JSON file.
        String outputJSON = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(hospital);
        String outputXML = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(hospital);

        System.out.println(outputJSON);
    }
}