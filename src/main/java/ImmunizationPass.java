import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ImmunizationPass {
    FhirContext ctx;
    IGenericClient client;
    Patient patient;
    Bundle wholeImmunizationPass;

    // Following List shall be retrieved from the server
    List<Practitioner> doctors_withQuali;

    public ImmunizationPass(IGenericClient client, FhirContext ctx){
        this.client = client;
        this.ctx = ctx;
    }

    /**
     * This Method creates a whole ImmunizationPass for Patient exPatient as a Bundle.
     * @return
     */
    public void buildImmunizationPass(){
        //create Patient we want to make the ImmunizationPass for.
        this.patient = newPatient();
        retrieveDoctors();

        // make content
        buildImmunizations();
        buildObservations();
    }



    private void buildImmunizations() {
        Immunization immu;
        MethodOutcome methodOutcome;

        // Get Calendar
        Calendar cal = Calendar.getInstance();


        /*
         * create new Immunization for Patient
         */
        // set Date
        cal.set(1991, Calendar.JANUARY, 01);
        // call creation of Immunization
        immu = newImmunization(
                new CodeableConcept(new Coding("http://hl7.org/fhir/sid/cvx", "37",
                        "yellow fever")),
                cal,
                this.doctors_withQuali.get(new Random().nextInt(this.doctors_withQuali.size())));
//        MethodOutcome immunizationOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
//        immu.setId(immunizationOutcome.getId());
    }

    private void buildObservations() {
        Observation ob;
        MethodOutcome methodOutcome;

        // Get Calendar
        Calendar cal = Calendar.getInstance();

        /*
         *  Observation/Test: TINE test
         */
        cal.set(1999,Calendar.SEPTEMBER,25);
        ob = newObservation(
                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-codes.html",
                        "10402-6", "Immune serum globulin given [Volume]")),
                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-methods.html",
                        "28163009", "Skin test for tuberculosis, Tine test")),
                new DateTimeType(cal.getTime()),
//                new DateTimeType("1999-09-25"),
                this.doctors_withQuali.get(new Random().nextInt(this.doctors_withQuali.size()))
        );
//        methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
//        ob.setId(methodOutcome.getId());

        /*
         *  Observation/Test: Hepatitis B Schutzimpfung
         */
        cal.set(2002, Calendar.MAY, 11);
        ob = newObservation(
                new CodeableConcept(new Coding("https://www.hl7.org/fhir/valueset-observation-codes.html",
                        "10397-8", "Hepatitis B immune globulin given [Volume]")),
                null,
                new DateTimeType(cal.getTime()),
//                new DateTimeType("2002-05-11"),
                this.doctors_withQuali.get(new Random().nextInt(this.doctors_withQuali.size()))
        );
//        methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
//        ob.setId(methodOutcome.getId());
    }

    /**
     * This method creates a new Patient with(Name,Birth,MaritalStatus) and returns it.
     * @return
     */
    public Patient newPatient(){
        // Empty Patient Instance
        Patient exPatient = new Patient();
        // Official Name
        HumanName exName = new HumanName();
        exName.addGiven("Jekofa");
        exName.setFamily("von Krule");
        exName.setUse(HumanName.NameUse.OFFICIAL);
        exPatient.addName(exName);

        // Birthday
        Calendar cal = Calendar.getInstance();
        cal.set(1992, Calendar.NOVEMBER, 10);
        exPatient.setBirthDate(cal.getTime());

        // MaritalStatus
        exPatient.setMaritalStatus(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/marital-status", "U",
                "unmarried")));

//        MethodOutcome patientOutcome = client.create().resource(exPatient).prettyPrint().encodedJson().execute();
//        exPatient.setId(patientOutcome.getId());
        return exPatient;
    }

    /**
     * creates a new Immunization by given parameters.
     * @param conceptVaccineCode
     */
    public Immunization newImmunization(CodeableConcept conceptVaccineCode, Calendar occurrenceDate, Practitioner doctor){
        Immunization exImmunization = new Immunization();

        //status is required
        exImmunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

        //vaccineCode is required
        exImmunization.setVaccineCode(conceptVaccineCode);

        //patient is required
        exImmunization.setPatient(new Reference(this.patient));

        //occurence is required
        exImmunization.setOccurrence(new DateTimeType(occurrenceDate.getTime()));

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

    public void retrieveDoctors(){
        List<Practitioner> doctors = new ArrayList<>();
//        Bundle bundle = client.search().forResource(Practitioner.class).returnBundle(Bundle.class).execute();
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
                // there is only one Nurse among these
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
}


