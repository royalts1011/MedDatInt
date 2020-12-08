import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImmunizationBuilder {

    Composition totalImmunizationPass;
    Patient patient;
    List<PractitionerRole> doctor_roles;
    IGenericClient client;

    public ImmunizationBuilder(Composition totalImmunizationPass, Patient patient,
                               List<PractitionerRole> doctor_roles,
                               IGenericClient client) {
        this.totalImmunizationPass = totalImmunizationPass;
        this.patient = patient;
        this.doctor_roles = doctor_roles;
        this.client = client;
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
            add("37");
            add("yellow fever");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("International certificate of vaccination or revaccination against Yellow Fever");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    sublist.get(0),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())));
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
    public void buildStandartImmunizations() {
        Immunization immu;
        MethodOutcome methodOutcome;

        ArrayList<ArrayList<String>> immuInfo = new ArrayList<>();
        immuInfo.add(new ArrayList<String>() {{
            add("1993-07-14");
            add("http://hl7.org/fhir/sid/cvx");
            add("18");
            add("rabies, intramuscular injection");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-11-10");
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("GNFLU");
            add("Influenza");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-08-10");
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("GNMUM");
            add("Mumps");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1993-08-10");
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("GNMEA");
            add("Measles");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1992-03-01");
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("GNTET");
            add("Tetanus");
        }});
        immuInfo.add(new ArrayList<String>() {{
            add("1992-05-25");
            add("urn:oid:1.2.36.1.2001.1005.17");
            add("GNRUB");
            add("Rubella");
        }});

        Composition.SectionComponent tmp = new Composition.SectionComponent();
        tmp.setTitle("Vaccination");


        for (ArrayList<String> sublist : immuInfo) {
            immu = newImmunization(
                    new CodeableConcept(new Coding(sublist.get(1), sublist.get(2), sublist.get(3))),
                    sublist.get(0),
                    this.doctor_roles.get(new Random().nextInt(this.doctor_roles.size())));
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
    public Immunization newImmunization(CodeableConcept conceptVaccineCode, String occurrenceDate, Practitioner doctor) {
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
}

