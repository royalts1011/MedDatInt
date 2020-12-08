import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * - Test Immu kann später wieder raus.
 */


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
        doctorRoles = hB.getdoctorRoles();

        // make content
        buildComposition();
        ImmunizationBuilder iB = new ImmunizationBuilder(totalImmunizationPass, this.patient, this.doctorRoles, this.client);
    }

    /**
     * Thismethid will generate a Composition as our "International Certificates of Vaccination"
     */
    public void buildComposition(){
        totalImmunizationPass.setStatus(Composition.CompositionStatus.FINAL);
        totalImmunizationPass.setType(new CodeableConcept(new Coding("http://loinc.org", "11503-0",
                "Medical records")));
        totalImmunizationPass.setCategory((List<CodeableConcept>) new CodeableConcept(new Coding("http://loinc.org", "11369-6",
                "History of Immunization Narrative")));
        totalImmunizationPass.setDate(Calendar.getInstance().getTime());
       // totalImmunizationPass.addAuthor()

        totalImmunizationPass.setTitle("International Certificates of Vaccination");
        totalImmunizationPass.addSection(new Composition.SectionComponent()
                .setTitle("Name of the cardholder")
                .addEntry(new Reference(this.patient)));

    }



    /**
     * This method will generate hard coded Observations (here immune tests) by using the method "newObservation()".
     * All content of the immune tests is defined in here.
     */
    private void buildObservations() {

        MethodOutcome methodOutcome;
        ArrayList<Observation> obs = new ArrayList<>();

        /*
         *  Observation/Test: TINE test
         */
//        obs.add( newObservation(
//                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-codes.html",
//                        "10402-6", "Immune serum globulin given [Volume]")),
//                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-methods.html",
//                        "28163009", "Skin test for tuberculosis, Tine test")),
//                new DateTimeType("1999-09-25"),
//                this.doctorRoles.get(new Random().nextInt(this.doctorRoles.size()))
//                )
//        );

        /*
         *  Observation/Test: Hepatitis B Schutzimpfung
         */
//        obs.add(newObservation(
//                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-codes.html",
//                        "10397-8", "Hepatitis B immune globulin given [Volume]")),
//                null,
//                new DateTimeType("2002-05-11"),
//                this.doctorRoles.get(new Random().nextInt(this.doctorRoles.size()))
//                )
//        );


        for( Observation ob : obs){
            // Put on server and receive ID
//            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
//            ob.setId(methodOutcome.getId());
            // add to immunization pass bundle
//            this.wholeImmunizationPass.addEntry(new Bundle.BundleEntryComponent().setResource(ob));
        }

    }

    /**
     * This method creates a new Patient with(Name,Birth,MaritalStatus) and returns it.
     * @return
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
     * This method creates a new Observation/Test by given parameters. Some tests, for example the tuberculosis skin test
     * (tine-test) does not appear in the Observation.code ValueSet. Therefore an Observation.method must be chosen to
     * fit the tine-test and thus the Observation.code a normal procedure, e.g. "Immune serum globulin given [Volume]"
     * If the immune test can be portrayed by the Observation.code or another simple code, the Observation.method shall
     * be set to null.
     *
     * @param conceptTestCode   The code describing the test or the type of treatment
     * @param conceptMethodCode The code describing the method (e.g. tine-test),
     *                          set to NULL if not needed
     * @param dateOfTest        The date of the Observation/test
     * @param doctor            The doctor leading the performance
     * @return                  Returns the complete Observation/test
     */
    public Observation newObservation(CodeableConcept conceptTestCode, CodeableConcept conceptMethodCode,
                                      DateTimeType dateOfTest, Practitioner doctor) {
        Observation exObservation = new Observation();

        // status required: FINAL = observation is complete
        exObservation.setStatus(Observation.ObservationStatus.FINAL);

        // set the performed direct test or treatment type
        exObservation.setCode(conceptTestCode);

        // Set method (titer skin) if not null
        if (conceptMethodCode != null) exObservation.setMethod(conceptMethodCode);

        // connect patient to test
        exObservation.setSubject(new Reference(this.patient));

        // when the test occurred
        exObservation.setEffective(dateOfTest);

        // who performed the test
        exObservation.addPerformer(new Reference(doctor));

        // what is the outcome
//        exObservation.setValue(new BooleanType(true));

        return exObservation;
    }

    /**
     * This method can be called if the list of doctors shall only be this one doctor.
     * E.g. the server got cleaned and no doctors would be found.
     */
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


    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    public Patient getExPatient() {
        return patient;
    }

}


