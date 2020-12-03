import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.*;

public class ImmunizationPass {
    IGenericClient client;
    Patient patient;
    Bundle wholeImmunizationPass;

    public ImmunizationPass(IGenericClient client){
        this.client = client;
    }

    /**
     * This Method creates a whole ImmunizationPass for Patient exPatient as a Bundle.
     * @return
     */
    public void buildImmunizationPass(){
        //create Patient we want to make the ImmunizationPass for.
        this.patient = newPatient();
        //Date of Immunization.
        Calendar cal_1 = Calendar.getInstance();
        cal_1.set(1991, Calendar.JANUARY, 01);
        //Doc who performed Immunization
        Practitioner doc_1 = new Practitioner();
        HumanName doctorsName_1 = new HumanName();
        doctorsName_1.addPrefix("Dr.");
        doctorsName_1.addGiven("Arno");
        doctorsName_1.setFamily("Dübel");
        doc_1.addName(doctorsName_1);
        doc_1.addQualification().setCode(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/v2-0360|2.7", "MD",
                "Doctor of Medicine")));
        MethodOutcome doctorOutcome = client.create().resource(doc_1).conditional()
                .where(Practitioner.NAME.matches().value("Dr. Arno Dübel")).prettyPrint().encodedJson().execute();
        doc_1.setId(doctorOutcome.getId());

        // Which Immunization was done.
        CodeableConcept cod_1 = new CodeableConcept(new Coding("http://hl7.org/fhir/sid/cvx", "37",
                "yellow fever"));

        //create new Immunization for Patient
        Immunization Immunization_1 = newImmunization(cod_1, cal_1, doc_1);
        MethodOutcome immunizationOutcome = client.create().resource(Immunization_1).conditional()
                .where(Immunization.VACCINE_CODE.exactly().code("37")).prettyPrint().encodedJson().execute();
        Immunization_1.setId(immunizationOutcome.getId());
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

        MethodOutcome patientOutcome = client.create().resource(exPatient).prettyPrint().encodedJson().execute();
        exPatient.setId(patientOutcome.getId());
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
     * creates a new Observation/Test by given parameters.
     * @param
     */
    public Observation newObservation() {
        Observation exObservation = new Observation();
        return null;
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


