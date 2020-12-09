import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Random;

public class ImmunizationBuilder {

    Composition totalImmunizationPass;
    Patient patient;
    ArrayList<PractitionerRole> doctor_roles;
    IGenericClient client;

    public ImmunizationBuilder(Composition totalImmunizationPass, Patient patient,
                               ArrayList<PractitionerRole> doctor_roles,
                               IGenericClient client) {
        this.totalImmunizationPass = totalImmunizationPass;
        this.patient = patient;
        this.doctor_roles = doctor_roles;
        this.client = client;
    }

    /**
     * creates a new Immunization by given parameters.
     *
     * @param conceptVaccineCode
     */
    public Immunization newImmunization(String occurrenceDate,
                                        CodeableConcept conceptVaccineCode,
                                        PractitionerRole doctor,
                                        String lotNumber,
                                        CodeableConcept targetDisease,
                                        String doseNumber) {
        Immunization exImmunization = new Immunization();

        //status is required
        exImmunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

        //vaccineCode is required
        exImmunization.setVaccineCode(conceptVaccineCode);

        //patient is required
        exImmunization.setPatient(new Reference(this.patient));

        //occurence is required
        exImmunization.setOccurrence(new DateType(occurrenceDate));

        //perfomer/actor is required
        exImmunization.addPerformer().setActor(new Reference(doctor));

        exImmunization.setLotNumber(lotNumber);
        exImmunization.addProtocolApplied().addTargetDisease(targetDisease).setDoseNumber(new StringType(doseNumber));


        return exImmunization;
    }

    /**
     * This method will generate hard coded Yellow Fever Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildSectionProphylaxis() {
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1991-01-01");
            add("http://hl7.org/fhir/sid/cvx");
            add("184");
            add("Yellow fever, unspecified formulation");
            // chargen nummer
            add("690010PD");
            // target disease
            add("http://hl7.org/fhir/sid/cvx");
            add("37");
            add("yellow fever");
            // doseNumber
            add("1");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("International certificate of vaccination or prophylaxis");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    new CodeableConcept(new Coding(sublist.get(5), sublist.get(6), sublist.get(7))),
                    sublist.get(8)
            );
            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(immu));
        }

        this.totalImmunizationPass.addSection(tmp);
    }

    /**
     * This method will generate hard coded standard Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildSectionStandardImmunizations() {
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        /*
         * Kombi Impfung Priorix Measles, Mumps, Rubella
         */
        immuInfo.add(new ArrayList<String>() {{
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("MMRSKB");
            add("Priorix");
            // chargen nummer
            add("A69CD425A");
            // target disease
            add("http://snomed.info/sct");
            add("36989005");
            add("Mumps");
            // doseNumber
            add("1");
        }});
            add("1993-07-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("MMRSKB");
            add("Priorix");
            // chargen nummer
            add("A69CD425A");
            // target disease
            add("http://snomed.info/sct");
            add("14189004");
            add("Measles");
            // doseNumber
            add("1");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("MMRSKB");
            add("Priorix");
            // chargen nummer
            add("A69CD425A");
            // target disease
            add("http://snomed.info/sct");
            add("36653000");
            add("Rubella");
            // doseNumber
            add("1");
        }});
        /*
         * Kombi Impfung Tetanus, Diphterie, Poliomyelitis, Pertussis
         */
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-23");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("115");
            add("Tdap");
            // chargen nummer
            add("J0021-1");
            // target disease
            add("http://snomed.info/sct");
            add("76902006");
            add("Tetanus");
            // doseNumber
            add("1");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-23");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("115");
            add("Tdap");
            // chargen nummer
            add("J0021-1");
            // target disease
            add("http://snomed.info/sct");
            add("397430003");
            add("Diphtheria due to Corynebacterium diphtheriae");
            // doseNumber
            add("1");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-23");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("115");
            add("Tdap");
            // chargen nummer
            add("J0021-1");
            // target disease
            add("http://snomed.info/sct");
            add("398102009");
            add("Acute poliomyelitis");
            // doseNumber
            add("1");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-23");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("115");
            add("Tdap");
            // chargen nummer
            add("J0021-1");
            // target disease
            add("http://snomed.info/sct");
            add("27836007");
            add("Pertussis");
            // doseNumber
            add("1");
        }});


        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Vaccinations");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    new CodeableConcept(new Coding(sublist.get(5), sublist.get(6), sublist.get(7))),
                    sublist.get(8)
            );
            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(immu));
        }

        this.totalImmunizationPass.addSection(tmp);
    }

    /**
     * This method will generate hard coded inlfuenza Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildSectionInfluenzaImmunizations(){
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1994-11-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("VAXGRP");
            add("Vaxigrip");
            // chargen nummer
            add("6411-C");
            // target disease
            add("http://snomed.info/sct");
            add("6142004");
            add("Influenza (disorder)");
            // doseNumber
            add("1");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Other vaccinations against influenza");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    new CodeableConcept(new Coding(sublist.get(5), sublist.get(6), sublist.get(7))),
                    sublist.get(8)
            );
            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(immu));
        }

        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildSectionOtherImmunizations(){
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
                add("2009-06-29");
                // description of vaccine
                add("urn:oid:1.2.36.1.2001.1005.17");
                add("MMRSKB");
                add("Priorix");
                // chargen nummer
                add("AHABB270BG");
                // target disease
                add("http://snomed.info/sct");
                add("709410003");
                add("Haemophilus influenzae type b infection");
                // doseNumber
                add("1");
            }});
        immuInfo.add(new ArrayList<String>() {{
                add("2009-06-30");
                // description of vaccine
                add("http://hl7.org/fhir/sid/cvx");
                add("26");
                add("cholera, unspecified formulation");
                // chargen nummer
                add("KO1331B1");
                // target disease
                add("http://snomed.info/sct");
                add("63650001");
                add("Cholera (disorder)");
                // doseNumber
                add("1");
            }});
        immuInfo.add(new ArrayList<String>() {{
            add("2010-06-01");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("18");
            add("rabies, intramuscular injection");
            // chargen nummer
            add("J1128-13");
            // target disease
            add("http://snomed.info/sct");
            add("14168008");
            add("Rabies (disorder)");
            // doseNumber
            add("1");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("2010-07-05");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("18");
            add("rabies, intramuscular injection");
            // chargen nummer
            add("J1128-13");
            // target disease
            add("http://snomed.info/sct");
            add("14168008");
            add("Rabies (disorder)");
            // doseNumber
            add("1");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Other vaccinations");
//        tmp.setTitle("Infection and travel vaccinations, professionally necessary vaccinations");
        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    new CodeableConcept(new Coding(sublist.get(5), sublist.get(6), sublist.get(7))),
                    sublist.get(8)
            );
            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(immu));
        }
        this.totalImmunizationPass.addSection(tmp);
    }

    public void buildCorona() {
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("2020-12-24");
            add("http://hl7.org/fhir/sid/cvx");
            add("840534001");
            add("Severe acute respiratory syndrome coronavirus 2 vaccination (procedure)");
            // chargen nummer
            add("2020LOVE");
            // target disease
            add("http://hl7.org/fhir/sid/cvx");
            add("186747009");
            add("Coronavirus infection (disorder)");
            // doseNumber
            add("1");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Vaccination against COVID-19");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    new CodeableConcept(new Coding(sublist.get(5), sublist.get(6), sublist.get(7))),
                    sublist.get(8)
            );
            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            tmp.addEntry(new Reference(immu));
        }

        this.totalImmunizationPass.addSection(tmp);
    }
}

