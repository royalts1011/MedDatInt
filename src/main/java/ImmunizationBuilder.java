import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO Methode zur Generierung der passiven Immu ist noch im Observation Builder,
//  wegen der Reihenfolge in der die Resourcen hinzugefügt werden müsssen.



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
    Immunization newImmunization(String occurrenceDate,
                                 CodeableConcept conceptVaccineCode,
                                 PractitionerRole doctor,
                                 String lotNumber,
                                 String doseNumber,
                                 List<CodeableConcept> targetDiseases) {
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
        exImmunization.addProtocolApplied().setDoseNumber(new PositiveIntType(doseNumber)).setTargetDisease(targetDiseases);


        return exImmunization;
    }

    private void addNewImmunizationsToSection(ArrayList<ArrayList<String>> immuInfo, Composition.SectionComponent secComp){
        Immunization immu;
        MethodOutcome methodOutcome;

        List<CodeableConcept> targetDiseases;
        for (ArrayList<String> sublist : immuInfo) {
            targetDiseases = new ArrayList<>();
            for(int i = 6; i < sublist.size(); i+=3){
                targetDiseases.add(new CodeableConcept(new Coding(sublist.get(i), sublist.get(i+1), sublist.get(i+2))));
            }

            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    sublist.get(5),
                    targetDiseases
            );
            methodOutcome = client.create().resource(immu).prettyPrint().encodedJson().execute();
            immu.setId(methodOutcome.getId());
            secComp.addEntry(new Reference(immu));
        }

        this.totalImmunizationPass.addSection(secComp);

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
            add("2009-03-13");
            add("http://hl7.org/fhir/sid/cvx");
            add("184");
            add("Yellow fever, unspecified formulation");
            // chargen nummer
            add("FFY010PD");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("16541001");
            add("yellow fever");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("International certificate of vaccination or prophylaxis");

        List<CodeableConcept> targetDiseases;
        for (ArrayList<String> sublist : immuInfo) {
            targetDiseases = new ArrayList<>();
            for(int i = 6; i < sublist.size(); i+=3){
                targetDiseases.add(new CodeableConcept(new Coding(sublist.get(i), sublist.get(i+1), sublist.get(i+2))));
            }
            immu = newImmunization(
                    sublist.get(0),
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())),
                    sublist.get(4),
                    sublist.get(5),
                    targetDiseases
            );
            immu.setManufacturer(new Reference(new Organization().setName("Sanofi Pasteur")));
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
        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        /*
         * Kombi Impfung Priorix Measles, Mumps, Rubella
         */
        String kombiDocIndex = String.valueOf(new Random().nextInt(this.doctor_roles.size()));
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("MMRSKB");
            add("Priorix");
            // chargen nummer
            add("A69CD425A");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("36989005");
            add("Mumps");
            // target disease
            add("http://snomed.info/sct");
            add("14189004");
            add("Measles");
            // target disease
            add("http://snomed.info/sct");
            add("36653000");
            add("Rubella");
        }});
        /*
         * Single vaccinations
         */
        immuInfo.add(new ArrayList<String>() {{
            add("2001-06-22");
            // description of vaccine
            add("http://snomed.info/sct");
            add("871764007");
            add("Vaccine product containing only HiB antigen");
            // chargen nummer
            add("E0073-3");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("709410003");
            add("Haemophilus influenzae type b infection");
        }});
        /*
         * Kombi Impfung Tetanus, Diphterie, Poliomyelitis, Pertussis
         */
        immuInfo.add(new ArrayList<String>() {{
            add("2001-08-14");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("115");
            add("Tdap");
            // chargen nummer
            add("J0021-1");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("76902006");
            add("Tetanus");
            // target disease
            add("http://snomed.info/sct");
            add("397430003");
            add("Diphtheria due to Corynebacterium diphtheriae");
            // target disease
            add("http://snomed.info/sct");
            add("398102009");
            add("Acute poliomyelitis");
            // target disease
            add("http://snomed.info/sct");
            add("27836007");
            add("Pertussis");
        }});


        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Vaccinations");

        /*
         * Create all immunizations from info, add entries and add section component
         */
        addNewImmunizationsToSection(immuInfo, tmp);
    }

    /**
     * This method will generate hard coded inlfuenza Immunizations by using the method "newImmunization()".
     * All content of the Immunizations is defined in here.
     */
    public void buildSectionInfluenzaImmunizations(){
        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1994-11-14");
            // description of vaccine
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("VAXGRP");
            add("Vaxigrip");
            // chargen nummer
            add("6411-C");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("6142004");
            add("Influenza (disorder)");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Other vaccinations against influenza");
        /*
         * Create all immunizations from info, add entries and add section component
         */
        addNewImmunizationsToSection(immuInfo, tmp);
    }

    public void buildSectionOtherImmunizations(){
        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
                add("2009-06-29");
                // description of vaccine
                add("http://hl7.org/fhir/sid/cvx");
                add("190");
                add("Typhoid conjugate vaccine (TCV)");
                // chargen nummer
                add("AHABB270BG");
                // doseNumber
                add("1");
                // target disease
                add("http://snomed.info/sct");
                add("4834000");
                add("Typhoid fever");
            }});
        immuInfo.add(new ArrayList<String>() {{
                add("2009-06-30");
                // description of vaccine
                add("http://hl7.org/fhir/sid/cvx");
                add("26");
                add("cholera, unspecified formulation");
                // chargen nummer
                add("KO1331B1");
                // doseNumber
                add("1");
                // target disease
                add("http://snomed.info/sct");
                add("63650001");
                add("Cholera (disorder)");
            }});
        immuInfo.add(new ArrayList<String>() {{
            add("2010-06-01");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("18");
            add("rabies, intramuscular injection");
            // chargen nummer
            add("J1128-13");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("14168008");
            add("Rabies (disorder)");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("2010-07-05");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("18");
            add("rabies, intramuscular injection");
            // chargen nummer
            add("J1128-13");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("14168008");
            add("Rabies (disorder)");
        }});
        /*
         * Twinrix HepA/HepB
         */
        immuInfo.add(new ArrayList<String>() {{
            add("2014-09-17");
            // description of vaccine
            add("http://hl7.org/fhir/sid/cvx");
            add("104");
            add("Twinrix Hep A-Hep B");
            // chargen nummer
            add("AHABB310AD");
            // doseNumber
            add("1");
            // target disease
            add("http://snomed.info/sct");
            add("40468003");
            add("Viral hepatitis type A");
            // target disease
            add("http://snomed.info/sct");
            add("66071002");
            add("Viral hepatitis type B");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Other vaccinations");
//        tmp.setTitle("Infection and travel vaccinations, professionally necessary vaccinations");
        /*
         * Create all immunizations from info, add entries and add section component
         */
        addNewImmunizationsToSection(immuInfo, tmp);
    }

    public void buildCorona() {
        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("2020-12-24");
            add("http://hl7.org/fhir/sid/cvx");
            add("840534001");
            add("Severe acute respiratory syndrome coronavirus 2 vaccination (procedure)");
            // chargen nummer
            add("2020LOVE");
            // doseNumber
            add("1");
            // target disease
            add("http://hl7.org/fhir/sid/cvx");
            add("186747009");
            add("Coronavirus infection (disorder)");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Vaccination against COVID-19");
        /*
         * Create all immunizations from info, add entries and add section component
         */
        addNewImmunizationsToSection(immuInfo, tmp);
    }


}

