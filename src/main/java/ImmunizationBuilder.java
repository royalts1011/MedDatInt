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

    public void generateAllImmunzations(){
        buildYellowFeverImmunization();
    }


    /**
     * This method will generate hard coded Yellow Fever Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildYellowFeverImmunization() {
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
        tmp.setTitle("International certificate of vaccination or revaccination against Yellow Fever");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    sublist.get(0),
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
     * This method will generate hard coded standart Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildStandardImmunizations() {
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("MMRSKB");
            add("Priorix");
            // chargen nummer
            add("690010PD");
            // target disease
            add("http://snomed.info/sct");
            add("36989005");
            add("Mumps");
            // doseNumber
            add("1");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Vaccinations");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    sublist.get(0),
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

    public void buildInfluenzaImmunizations(){
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
            add(" Influenza (disorder)");
            // doseNumber
            add("1");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Influenza Vaccinations");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    sublist.get(0),
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
     * creates a new Immunization by given parameters.
     *
     * @param conceptVaccineCode
     */
    public Immunization newImmunization(CodeableConcept conceptVaccineCode,
                                        String occurrenceDate,
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
        exImmunization.setOccurrence(new DateTimeType(occurrenceDate));

        //perfomer/actor is required
        exImmunization.addPerformer().setActor(new Reference(doctor));

        exImmunization.setLotNumber(lotNumber);
        exImmunization.addProtocolApplied().addTargetDisease(targetDisease);
        exImmunization.addProtocolApplied().setDoseNumber(new StringType(doseNumber));


        return exImmunization;
    }
}

