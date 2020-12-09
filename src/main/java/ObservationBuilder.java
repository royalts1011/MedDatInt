import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;

public class ObservationBuilder {

    Composition totalImmunizationPass;
    Patient patient;
    ArrayList<PractitionerRole> doctor_roles;
    IGenericClient client;

    public ObservationBuilder(Composition totalImmunizationPass, Patient patient,
                               ArrayList<PractitionerRole> doctor_roles,
                               IGenericClient client) {
        this.totalImmunizationPass = totalImmunizationPass;
        this.patient = patient;
        this.doctor_roles = doctor_roles;
        this.client = client;
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

    public void buildSectionRubellaTest(){

        /*
         * TODO: Gather all Observation (test) info for the creation of the observation below
         */

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        // TODO a) before immunization, b) 10 weeks or more after immunization
        tmp.setTitle("Rubella antibody assays");

        /*
         * TODO: Call Observation creation, put on server (ID retrieval) and add section entry
         */

        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionHepatitisB(){
        /*
         * TODO: Gather all Observation (test) info for the creation of the observation below
         */

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Hepatitis B: Result of antibody assays (Anti-Hbs)");

        /*
         * TODO: Call Observation creation, put on server (ID retrieval) and add section entry
         */

        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionTuberculinTest(){
        /*
         * TODO: Gather all Observation (test) info for the creation of the observation below
         */

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Tuberculin-test results");

        /*
         * TODO: Call Observation creation, put on server (ID retrieval) and add section entry
         */

        this.totalImmunizationPass.addSection(tmp);
    }

    /**
     * This method will generate hard coded Observations (here immune tests) by using the method "newObservation()".
     * All content of the immune tests is defined in here.
     */
    @Deprecated
    public void buildObservations() {

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



}
