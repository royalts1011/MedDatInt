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
    FhirContext ctx;
    IGenericClient client;
    Patient patient;
    Bundle wholeImmunizationPass;

    // Following List shall be retrieved from the server
    List<Practitioner> doctors_withQuali;

    Immunization testImmu;


    public ImmunizationPass(IGenericClient client, FhirContext ctx){
        this.client = client;
        this.ctx = ctx;

        this.wholeImmunizationPass = new Bundle();
        this.wholeImmunizationPass.setType(Bundle.BundleType.DOCUMENT);
    }

    /**
     * This Method serves as the "FHIR"/Fire starter for creating an immunization pass.
     * A patient will be created and necessary information like doctors, immunizations, immune tests
     * will be created or polled from the server.
     */
    public void buildImmunizationPass(){
        //create Patient we want to make the ImmunizationPass for.
        this.patient = newPatient();

        Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
        entry.setResource(this.patient);
        this.wholeImmunizationPass.addEntry(entry) ;

        // Poll doctors from the server
        retrieveDoctors();

        // The following code line overwrites the retrieved doctors.
//        this.doctors_withQuali = setRescueDoc();


        // make content
        buildImmunizations();
        buildObservations();
    }


    /**
     * This method will generate hard coded Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildImmunizations() {
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1991-01-01"); add("http://hl7.org/fhir/sid/cvx"); add("37"); add("yellow fever");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-14"); add("http://hl7.org/fhir/sid/cvx"); add("18"); add("rabies, intramuscular injection");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-11-10"); add("urn:oid:1.2.36.1.2001.1005.17"); add("GNFLU"); add("Influenza");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-08-10"); add("urn:oid:1.2.36.1.2001.1005.17"); add("GNMUM"); add("Mumps");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-08-10"); add("urn:oid:1.2.36.1.2001.1005.17"); add("GNMEA"); add("Measles");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1992-03-01"); add("urn:oid:1.2.36.1.2001.1005.17"); add("GNTET"); add("Tetanus");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1992-05-25"); add("urn:oid:1.2.36.1.2001.1005.17"); add("GNRUB"); add("Rubella");
        }});

        for (ArrayList<String> sublist : immuInfo){
            immu = newImmunization(
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    sublist.get(0),
                    this.doctors_withQuali.get(new Random().nextInt(this.doctors_withQuali.size())));
//            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
//            immu.setId(methodOutcome.getId());
            this.wholeImmunizationPass.addEntry(new Bundle.BundleEntryComponent().setResource(immu));
        }

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
        obs.add( newObservation(
                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-codes.html",
                        "10402-6", "Immune serum globulin given [Volume]")),
                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-methods.html",
                        "28163009", "Skin test for tuberculosis, Tine test")),
                new DateTimeType("1999-09-25"),
                this.doctors_withQuali.get(new Random().nextInt(this.doctors_withQuali.size()))
                )
        );

        /*
         *  Observation/Test: Hepatitis B Schutzimpfung
         */
        obs.add(newObservation(
                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-codes.html",
                        "10397-8", "Hepatitis B immune globulin given [Volume]")),
                null,
                new DateTimeType("2002-05-11"),
                this.doctors_withQuali.get(new Random().nextInt(this.doctors_withQuali.size()))
                )
        );


        for( Observation ob : obs){
            // Put on server and receive ID
//            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
//            ob.setId(methodOutcome.getId());
            // add to immunization pass bundle
            this.wholeImmunizationPass.addEntry(new Bundle.BundleEntryComponent().setResource(ob));
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
     * creates a new Immunization by given parameters.
     * @param conceptVaccineCode
     */
    public Immunization newImmunization(CodeableConcept conceptVaccineCode, String occurrenceDate, Practitioner doctor){
        Immunization exImmunization = new Immunization();

        //status is required
        exImmunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

        //vaccineCode is required
        exImmunization.setVaccineCode(conceptVaccineCode);

        //patient is required
        exImmunization.setPatient(new Reference(this.patient));

        //occurence is required
        exImmunization.setOccurrence(new DateTimeType(occurrenceDate));

        //perfomer/actor is required
        exImmunization.addPerformer().setActor(new Reference(doctor));


        return exImmunization;
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

    /**
     * This method retrieves all Practitioner objects from the server and converts them to a list.
     * It will then further filter this list to receive a list of all Practitioners whose Practitioner.qualification
     * has been set and whose qualification is described with code "MD".
     * The filtered list will be saved in a Practitioner-List as a class variable
     */
    public void retrieveDoctors(){
        List<Practitioner> doctors = new ArrayList<>();
        Bundle bundle = client.search().forResource(Practitioner.class).returnBundle(Bundle.class).execute();
        doctors.addAll(BundleUtil.toListOfResourcesOfType(this.ctx, bundle, Practitioner.class));

        // Load the subsequent pages
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = client.loadPage().next(bundle).execute();
            doctors.addAll(BundleUtil.toListOfResourcesOfType(this.ctx, bundle, Practitioner.class));
        }

        // only choose all doctors with qualifications
        this.doctors_withQuali = doctors.stream()
                .filter(d -> d.hasQualification())
                // Drop all objects that are not 'MD'
                .filter(d -> d.getQualification().get(0).getCode().getCoding().get(0).getCode().equals("MD"))
                .collect(Collectors.toList());

        System.out.println("Doctor Count: " + doctors.size());
        System.out.println("Doctor with Quali 'MD' Count: " + this.doctors_withQuali.size());

    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    public Patient getExPatient() {
        return patient;
    }

    public Bundle getWholeImmunizationPass() {
        return wholeImmunizationPass;
    }

    public Immunization getTestImmu()   {
        return this.testImmu;
    }


}


