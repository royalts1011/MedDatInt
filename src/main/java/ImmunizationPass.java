import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class ImmunizationPass {
    IGenericClient client;
    Patient patient;


    Composition totalImmunizationPass;
    // Following List shall be retrieved from the server

    ArrayList<PractitionerRole> doctorRoles;


    public ImmunizationPass(IGenericClient client, FhirContext ctx){
        this.client = client;
        this.totalImmunizationPass = new Composition();
    }

    /**
     * This Method serves as the "FHIR"/Fire starter for creating an immunization pass.
     * A patient will be created and necessary information like doctors, immunizations, immune tests
     * will be created or polled from the server.
     */
    public void buildImmunizationPass(){
        //create Patient we want to make the ImmunizationPass for.
        this.patient = newPatient();

        // Create a hospital and doctors
        HospitalBuilder hB = new HospitalBuilder(client);
        hB.buildHospital();
        doctorRoles = hB.getdoctorRoles();

        /*
         * Initiate Composition
         */
        initComposition();
        // Create first section concerning the patient this pass refers to
        this.totalImmunizationPass.addSection(new Composition.SectionComponent()
                .setTitle("Issued to")
                .addEntry(new Reference(this.patient)));
        /*
         * Initialize the Immunization and Observation (tests) builder
         */
        ImmunizationBuilder iB = new ImmunizationBuilder(totalImmunizationPass, this.patient, this.doctorRoles, this.client);
        ObservationBuilder oB = new ObservationBuilder(totalImmunizationPass, this.patient, this.doctorRoles, this.client);

        /*
         * Add Immunization and test sections sequentially
         */

        iB.buildSectionProphylaxis();
        iB.buildSectionStandardImmunizations();
        iB.buildSectionInfluenzaImmunizations();
        iB.buildSectionOtherImmunizations();

        oB.buildSectionTuberculinTest();
        oB.buildSectionRubellaTest();
        oB.buildSectionHepatitisB();
        oB.buildSectionHepatitisA();

        // TODO change to actual resource generation
        this.totalImmunizationPass.addSection(new Composition.SectionComponent()
                .setTitle("Passive immunizations with human (or heterologous) immunoglobulins"));




        /*
         * POST to server and receive ID
         */
        MethodOutcome compositionOutcome = client.create().resource(this.totalImmunizationPass).prettyPrint().encodedJson().execute();
        this.totalImmunizationPass.setId(compositionOutcome.getId());
    }

    /**
     * Thismethid will generate a Composition as our "International Certificates of Vaccination"
     */
    public void initComposition(){
        this.totalImmunizationPass.setStatus(Composition.CompositionStatus.FINAL);
        this.totalImmunizationPass.setType(new CodeableConcept(new Coding("http://loinc.org", "11503-0",
                "Medical records")));
        this.totalImmunizationPass.addCategory(new CodeableConcept(new Coding("http://loinc.org", "11369-6",
                "History of Immunization Narrative")));
        this.totalImmunizationPass.setDate(Calendar.getInstance().getTime());
       // totalImmunizationPass.addAuthor()

        this.totalImmunizationPass.setTitle("International Certificates of Vaccination");
    }

    /**
     * This method creates a new Patient with(Name,Birth,MaritalStatus,Gender) and returns it.
     * @return Patient, the hard-coded patient.
     */
    public Patient newPatient(){
        // Empty Patient Instance
        Patient exPatient = new Patient();

        // declare names for setting and checking (in the conditional create)
        String firstName = "Jekofa3";
        String lastName = "von Krule3";

        // Official Name
        HumanName exName = new HumanName();

        exName.setUse(HumanName.NameUse.OFFICIAL).addGiven(firstName).setFamily(lastName);
        exPatient.addName(exName);
        exPatient.setGender(Enumerations.AdministrativeGender.MALE);

        // Birthday
        Calendar cal = Calendar.getInstance();
        cal.set(1990, Calendar.MAY, 10);
        exPatient.setBirthDate(cal.getTime());

        // MaritalStatus
        exPatient.setMaritalStatus(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/marital-status", "U",
                "unmarried")));

        // Create and add address
        Address patAddress = new Address();
        // BOTH physical and postal address (HOME)
        patAddress.setType(Address.AddressType.BOTH).setUse(Address.AddressUse.HOME);
        patAddress  .setCountry("Germany")
                    .setCity("Hamburg")
        // 1 ei, 4 Zigaretten, 1 Ibuprofen, 1 Rosinenbroetchen mit Leberwurst, dann kommt Bier/Vier ins Spiel
                    .setPostalCode("14114");

        // Adding structured street and number information
        patAddress.setLine(new ArrayList<StringType>() {{
                                add(new StringType("Orgelallee"));
                                add(new StringType("8"));
        }});

        // alternate Textform of the address that could be used if wanted.
        patAddress.setText( "Orgelallee 8, " +
                            "14114 Hamburg, " +
                            "Germany");

        exPatient.addAddress(patAddress);

        MethodOutcome patientOutcome = client.create().resource(exPatient).conditional()
                .where(Practitioner.FAMILY.matches().value(lastName))
                .and(Practitioner.GIVEN.matches().value(firstName)).prettyPrint().encodedJson().execute();

        exPatient.setId(patientOutcome.getId());
        return exPatient;
    }

    /**
     * This method can be called if the list of doctors shall only be this one doctor.
     * E.g. the server got cleaned and no doctors would be found.
     *
     * @return Returns a list containing only one doctor.
     */
    @Deprecated
    private List<Practitioner> setRescueDoc(){
        Practitioner doc = new Practitioner();
        HumanName doctorsName = new HumanName();
        doctorsName.addPrefix("Dr.").addGiven("Arno").setFamily("Dübel");
        doc.addName(doctorsName);
        doc.addQualification().setCode(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/v2-0360|2.7", "MD",
                "Doctor of Medicine")));
        MethodOutcome doctorOutcome = client.create().resource(doc).prettyPrint().encodedJson().execute();
        doc.setId(doctorOutcome.getId());

        List<Practitioner> rescueDoc = new ArrayList<>();
        rescueDoc.add(doc);
        return rescueDoc;
    }

    public Composition getTotalImmunizationPass() {
        return totalImmunizationPass;
    }

}


