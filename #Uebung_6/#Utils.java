import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.r4.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Utils {
    private FhirContext ctx;
    private String outputFolder;
    private Patient pat;

    public Utils(FhirContext ctx, String outputFolder){
        this.ctx = ctx;
        this.outputFolder = outputFolder;
    }

    /**
     * This method creates files with custom endings.
     *
     * @param fileName    String: The name of the file inlcuding the file ending (e.g. .json)
     * @param fileContent String: The content of the file
     */
    private void writeToFile(String fileName, String fileContent) {
        File dir = new File(this.outputFolder);
        if(!dir.exists())   {
            dir.mkdir();
        }
        File file = new File(outputFolder + "/" + fileName);

        try {
            FileWriter writer = new FileWriter(file ,false);
            writer.write(fileContent);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * method which creates Antonie Gruenlich
     */
    public void createAntonie() {

        Patient antonie = new Patient();

        Identifier patID = antonie.addIdentifier();
        patID.setValue(String.valueOf(new Random().nextInt(10000)));
        patID.setSystem("http://www.kh-uzl.de/fhir/patients");

        //Name
        HumanName name = antonie.addName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("Gruenlich").addGiven("Antonie");

        HumanName maidenName = antonie.addName();
        maidenName.setUse(HumanName.NameUse.MAIDEN);
        maidenName.setFamily("Bruddenbrooks");

        //Birthday
        antonie.setBirthDateElement(new DateType("1827-08-06"));

        //Mar Status
        CodeableConcept marStatus = new CodeableConcept();
        marStatus.addCoding().setCode("M").setSystem("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus").setDisplay("Married");
        antonie.setMaritalStatus(marStatus);

        // Uncomment if the picture is not available or is not wanted in the output
        // You MUST change the path to the actual full path to the file.
        String imagePath = "C:\\Users\\kvkue\\git\\MedDatInt\\src\\main\\resources\\PAT_FOTO.png";
        //Picture
        File file = new File(imagePath);
        byte[] bytes = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            fileInputStreamReader.read(bytes);
        } catch (IOException e) {
            System.out.println(e);
        }

        Attachment photo = new Attachment();
        photo.setData(bytes);
        antonie.addPhoto(photo);


        //Write XML and JASON Files
        String outputJSON = this.ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(antonie);
        writeToFile("AntonieGruenlich.json", outputJSON);
        String outputXML = this.ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(antonie);
        writeToFile("AntonieGruenlich.xml", outputXML);

        this.pat = antonie;
    }

    /*
    Method which returns all Patient Names
     */
    public void getAllNames() {
        IGenericClient client = this.ctx.newRestfulGenericClient("https://funke.imi.uni-luebeck.de/public/fhir");

        //create Bundle by searching for Patients
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();

        List<Patient> patients = BundleUtil.toListOfResourcesOfType(this.ctx, results, Patient.class);

        //Go through bundle and write name for each patient to list
        String allPatientNames = "";
        for (Patient pat : patients) {
            allPatientNames += (pat.getName().get(0).getNameAsSingleString() + '\n');
        }

        //Write Names to file
        writeToFile("allPatientNames.txt", allPatientNames);

    }

    public void makeWholeHospital() {

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
        Patient mother = this.pat;
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

        // ReasonCode Birth
        CodeableConcept reasonBirth = new CodeableConcept();
        reasonBirth.addCoding().setCode("3950001").setSystem("http://hl7.org/fhir/ValueSet/encounter-reason").setDisplay("Birth");

        // from ValueSet https://fhir-ru.github.io/v3/ActEncounterCode/vs.html
//        Coding typeOfEncounter = new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "IMP", "inpatient encounter");
        Coding typeOfEncounter = new Coding("http://terminology.hl7.org/ValueSet/v3-ActEncounterCode", "PRENC", "pre-admission");
        Encounter doomsday = new Encounter();
        doomsday.setStatus(Encounter.EncounterStatus.PLANNED);
        doomsday.setClass_(typeOfEncounter);
        // add birth reason
        doomsday.addReasonCode(reasonBirth);


        // Connect substancial Encounter parts
        doomsday.addAppointment(refToAppoint);
        doomsday.addParticipant(motherComponent).addParticipant(stationComponent);
        Reference refToEncounter = new Reference(doomsday);

        station.getEndpoint().add(refToEncounter);


        //Write all to JSON file.
        String outputJSON = this.ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(hospital);
        String outputXML = this.ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(hospital);

        System.out.println(outputJSON);

        writeToFile( "hospital.json", outputJSON);
        writeToFile( "hospital.xml", outputXML);
    }

}
