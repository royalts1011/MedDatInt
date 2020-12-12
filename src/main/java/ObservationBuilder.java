import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.checkerframework.checker.units.qual.C;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.NarrativeStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;
import org.hl7.fhir.r4.model.codesystems.ObservationRangeCategory;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import java.util.ArrayList;
import java.util.List;
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
        // TODO Is the category correct for ALL tests? Rather no category!!
//        exObservation.addCategory(new CodeableConcept(new Coding(
//                "http://terminology.hl7.org/CodeSystem/observation-category",
//                "laboratory",
//                "Laboratory")) );

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
            add("Mantoux test");
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
            /**
            ob.addInterpretation(new CodeableConcept(new Coding(
                    "2.16.840.1.113883.6.96",
                    "268376005",
                    "Mantoux: negative")));
            */
            ob.addInterpretation(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", "NEG", "Negative")));
            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
            ob.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(ob));
        }

        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionRubellaTest(){
        Observation ob;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> obInfo = new ArrayList<>();
        obInfo.add(new ArrayList<String>() {{
            add("1999-10-10");
            // observation code
            add("http://loinc.org");
            add("22496-4");
            add("Rubella virus Ab [Titer] in Serum");
            // observation method
            add("http://snomed.info/sct");
            add("1358008");
            add("Anti-human globulin test, enzyme technique, titer");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Rubella antibody assays");

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
            ob.setValue(new StringType("yes"));
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

    public void buildSectionHepatitisB(){
        Observation ob;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> obInfo = new ArrayList<>();
        obInfo.add(new ArrayList<String>() {{
            add("1996-12-02");
            // observation code
            add("http://loinc.org");
            add("5195-3");
            add("Hepatitis B virus surface Ag [Presence] in Serum");
            // observation method
            add("http://snomed.info/sct");
            add("65911000");
            add("Hepatitis B surface antibody measurement");
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


        // TODO
        Narrative n = new Narrative();
        n.setStatus(Narrative.NarrativeStatus.EMPTY);
        n.setDiv(new XhtmlNode().setValue(XhtmlNode.XMLNS));
        tmp.setText(n);
        tmp.setEmptyReason(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/list-empty-reason","nilknown","Nil Known")));

        this.totalImmunizationPass.addSection(tmp);


    }

    /**
     * Builds the passive immunization. Still here because of the order in the HTML-Templates
     */
    public void buildSectionPassiveImmunizations() {
        Immunization immu = new Immunization();
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1998-01-04");
            // observation code
            add("http://snomed.info/sct");
            add("117103007");
            add("Administration of human immune globulin product");
            // observation method
//            add("http://snomed.info/sct");
//            add("117093001");
//            add("Intramuscular injection of Tetanus immune globulin, human");
        }});

        List<CodeableConcept> targetDiseases = new ArrayList<CodeableConcept>();

        //TODO besser als route einfügen?
//        targetDiseases.add(new CodeableConcept(new Coding("http://snomed.info/sct",
//                "117093001", "Intramuscular injection of Tetanus immune globulin, human")));

        targetDiseases.add(new CodeableConcept(new Coding("http://snomed.info/sct",
                "76902006", "Tetanus (disorder)")));


        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Passive immunizations with human (or heterologous) immunoglobulins");

        for (ArrayList<String> sublist : immuInfo) {

            ImmunizationBuilder immuBuilder = new ImmunizationBuilder(this.totalImmunizationPass, this.patient, this.doctorRoles, this.client);

            immu = immuBuilder.newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1),
                            sublist.get(2), sublist.get(3))),
                            this.doctorRoles.get(new Random().nextInt(this.doctorRoles.size())),
                    "lotNumber123",
                    "1",
                    targetDiseases
            );
            /*
             * Individual observation specification
             */
            immu.addNote(new Annotation((new MarkdownType("Passive immunization"))));

            // add Quantity
            immu.getDoseQuantity().setValue(5);
            immu.getDoseQuantity().setUnit("ml");
            immu.getDoseQuantity().setSystem("[\\d*.?\\dml]");

            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(immu));
        }

        this.totalImmunizationPass.addSection(tmp);

    }

    /**
     * below the old Method with passive immunization as observation
     **/

//    public void buildSectionPassiveImmunizations(){
//        Observation ob;
//        MethodOutcome methodOutcome;
//
//        ArrayList<ArrayList<String>> obInfo = new ArrayList<>();
//        obInfo.add(new ArrayList<String>() {{
//            add("1998-01-04");
//            // observation code
//            add("http://snomed.info/sct");
//            add("117103007");
//            add("Administration of human immune globulin product");
//            // observation method
//            add("http://snomed.info/sct");
//            add("117093001");
//            add("Intramuscular injection of Tetanus immune globulin, human");
//        }});
//
//        Composition.SectionComponent tmp = new Composition.SectionComponent();
//        tmp.setTitle("Passive immunizations with human (or heterologous) immunoglobulins");
//
//        for (ArrayList<String> sublist : obInfo) {
//            ob = newBasicObservation(
//                    sublist.get(0),
//                    new CodeableConcept(new Coding(sublist.get(1),
//                            sublist.get(2), sublist.get(3))),
//                    new CodeableConcept(new Coding(sublist.get(4),
//                            sublist.get(5), sublist.get(6))),
//                    this.doctorRoles.get(new Random().nextInt(this.doctorRoles.size()))
//            );
//            /*
//             * Individual observation specification
//             */
//            ob.setDataAbsentReason(new CodeableConcept(new Coding(
//                    "http://terminology.hl7.org/CodeSystem/data-absent-reason",
//                    "not-applicable",
//                    "Not Applicable"
//            )));
//            ob.addCategory(new CodeableConcept(new Coding(
//                    "http://snomed.info/sct",
//                    "51116004",
//                    "Passive immunization"
//            )));
//            ob.addNote(new Annotation(new MarkdownType("5ml")));
//
//            methodOutcome = client.create().resource(ob).prettyPrint().encodedJson().execute();
//            ob.setId(methodOutcome.getId());
//            tmp.addEntry(new Reference(ob));
//        }
//
//        this.totalImmunizationPass.addSection(tmp);
//
//
//    }




}
