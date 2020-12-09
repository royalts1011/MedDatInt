import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;
import org.hl7.fhir.r4.model.codesystems.ObservationRangeCategory;

import java.util.ArrayList;
import java.util.Random;

public class ObservationBuilder {

    Composition totalImmunizationPass;
    Patient patient;
    ArrayList<PractitionerRole> doctorRoles;
    IGenericClient client;

    public ObservationBuilder(Composition totalImmunizationPass, Patient patient,
                               ArrayList<PractitionerRole> doctorRoles,
                               IGenericClient client) {
        this.totalImmunizationPass = totalImmunizationPass;
        this.patient = patient;
        this.doctorRoles = doctorRoles;
        this.client = client;
    }

    /**
     * This method creates a new Observation/Test by given parameters. Some tests, for example the tuberculosis skin test
     * (tine-test) does not appear in the Observation.code ValueSet. Therefore an Observation.method must be chosen to
     * fit the tine-test and thus the Observation.code a normal procedure, e.g. "Immune serum globulin given [Volume]"
     * If the immune test can be portrayed by the Observation.code or another simple code, the Observation.method shall
     * be set to null.
     *
     * @param dateOfTest        The date of the Observation/test
     * @param conceptTestCode   The code describing the test or the type of treatment
     * @param conceptMethodCode The code describing the method (e.g. tine-test),
     *                          set to NULL if not needed
     * @param doctorRole        The doctor (role) leading the performance
     * @return                  Returns the complete Observation/test
     */
    public Observation newBasicObservation(String dateOfTest, CodeableConcept conceptTestCode,
                                      CodeableConcept conceptMethodCode, PractitionerRole doctorRole) {
        Observation exObservation = new Observation();

        // status required: FINAL = observation is complete
        exObservation.setStatus(Observation.ObservationStatus.FINAL);
        // TODO Is the category correct for ALL tests?
        exObservation.addCategory(new CodeableConcept(new Coding(
                "http://terminology.hl7.org/CodeSystem/observation-category",
                "laboratory",
                "Laboratory")) );

        // set the performed direct test or treatment type
        exObservation.setCode(conceptTestCode);

        // Set method (titer skin) if not null
        if (conceptMethodCode != null) exObservation.setMethod(conceptMethodCode);

        // connect patient to test
        exObservation.setSubject(new Reference(this.patient));

        // when the test occurred
        exObservation.setEffective(new DateTimeType(dateOfTest));

        // who performed the test
        exObservation.addPerformer(new Reference(doctorRole));

        // what is the outcome
//        exObservation.setValue(new BooleanType(true));

        return exObservation;
    }

    public void buildSectionTuberculinTest(){
        Observation ob;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> obInfo = new ArrayList<>();
        obInfo.add(new ArrayList<String>() {{
            add("1999-09-25");
            // observation code
            add("http://loinc.org");
            add("10402-6");
            add("Immune serum globulin given [Volume]");
            // observation method
            add("http://snomed.info/sct");
            add("424489006");
            add("Mantoux test (procedure)");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Tuberculin-test results");

        for (ArrayList<String> sublist : obInfo) {
            ob = newBasicObservation(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1),
                            sublist.get(2), sublist.get(3))),
                    new CodeableConcept(new Coding(sublist.get(4),
                            sublist.get(5), sublist.get(6))),
                    this.doctorRoles.get(new Random().nextInt(this.doctorRoles.size()))
            );
            ob.setValue(new StringType("negative"));
            ob.addInterpretation(new CodeableConcept(new Coding(
                    "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                    "L",
                    "Low")));

            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
            ob.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(ob));
        }

        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionRubellaTest(){

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        // TODO a) before immunization, b) 10 weeks or more after immunization
        tmp.setTitle("Rubella antibody assays");


        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionHepatitisB(){
        Observation ob;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> obInfo = new ArrayList<>();
        obInfo.add(new ArrayList<String>() {{
            add("1996-12-02");
            // observation code
            add("http://loinc.org");
            add("10397-8");
            add("Hepatitis B immune globulin given [Volume]");
            // observation method
            add("http://snomed.info/sct");
            add("65911000");
            add("Hepatitis B surface antibody measurement (procedure)");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Hepatitis B: Result of antibody assays (Anti-Hbs)");

        for (ArrayList<String> sublist : obInfo) {
            ob = newBasicObservation(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1),
                            sublist.get(2), sublist.get(3))),
                    new CodeableConcept(new Coding(sublist.get(4),
                            sublist.get(5), sublist.get(6))),
                    this.doctorRoles.get(new Random().nextInt(this.doctorRoles.size()))
            );
            /*
             * Individual TEST RESULT specification
             */
            ob.setValue(new StringType("immune"));
            ob.addInterpretation(new CodeableConcept(new Coding(
                    "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                    "HX",
                    "above high threshold")));

            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
            ob.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(ob));
        }

        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionHepatitisA(){
        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Hepatitis A: Result of antibody assays (Anti-Hbs)");
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


        for( Observation ob : obs){
            // Put on server and receive ID
//            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
//            ob.setId(methodOutcome.getId());
            // add to immunization pass bundle
//            this.wholeImmunizationPass.addEntry(new Bundle.BundleEntryComponent().setResource(ob));
        }

    }



}
