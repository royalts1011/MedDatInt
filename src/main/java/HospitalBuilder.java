import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class builds a hospital as an organization with different doctors (which can immunize people)
 */
public class HospitalBuilder {

    IGenericClient client;
    ArrayList<PractitionerRole> doctorRoles;
    Organization myHospital;


    public HospitalBuilder(IGenericClient client){
        this.client = client;
    }

    public void buildHospital(){
        doctorRoles = new ArrayList<>();
        makeOrganizations();
        makeDoctors();
    }

    public void makeOrganizations() {

        myHospital = new Organization();
        myHospital.setName("Klinikum Hamburg-Eppendorf");

        // Create and add address
        Address hospAddress = new Address();
        // BOTH physical and postal address (HOME)
//        hospAddress.setType(Address.AddressType.BOTH).setUse(Address.AddressUse.WORK);
        hospAddress.setCountry("Germany")
                .setCity("Hamburg")
                .setPostalCode("20251");

        // Adding structured street and number information
        hospAddress.addLine("Martinistraße").addLine("52");

        // alternate Textform of the address that could be used if wanted.
        hospAddress.setText( "Martinistraße 52, " +
                "20251 Hamburg, " +
                "Germany");

        myHospital.addAddress(hospAddress);

        MethodOutcome orgaOutcome = this.client.create().resource(myHospital).prettyPrint().encodedJson().execute();
        myHospital.setId(orgaOutcome.getId());
    }

    public void makeDoctors(){
        /*
         * Make a doctor
         */
        Practitioner doctor = new Practitioner();
        HumanName doctorsName = new HumanName();
        doctorsName.addPrefix("Dr.").addGiven("Waltraud").setFamily("Lemmy");
        doctor.addName(doctorsName);

        MethodOutcome doctorOutcome = client.create().resource(doctor).conditional()
                .where(Practitioner.FAMILY.matches().value("Waltraud")).and(Practitioner.GIVEN.matches().value("Lemmy"))
                .prettyPrint().encodedJson().execute();
        doctor.setId(doctorOutcome.getId());

        PractitionerRole doctorRole = new PractitionerRole();
        doctorRole.setPractitionerTarget(doctor).setPractitioner(new Reference(doctor));
        doctorRole.setOrganizationTarget(this.myHospital).setOrganization(new Reference(this.myHospital));
        doctorRole.addCode(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/practitioner-role", "doctor",
                "A qualified/registered medical practitioner")));

        MethodOutcome doctorRoleOutcome = client.create().resource(doctorRole).prettyPrint().encodedJson().execute();
        System.out.println("DoctorRole with ID: " + doctorRoleOutcome.getId());
        doctorRole.setId(doctorRoleOutcome.getId());

        // add to list
        doctorRoles.add(doctorRole);

        /*
         * Next doctor
         */
        doctor = new Practitioner();
        doctorsName = new HumanName();
        doctorsName.addPrefix("Dr.").addGiven("Arno").setFamily("Dübels");
        doctor.addName(doctorsName);

        doctorOutcome = client.create().resource(doctor).conditional()
                .where(Practitioner.FAMILY.matches().value("Waltraud")).and(Practitioner.GIVEN.matches().value("Lemmy"))
                .prettyPrint().encodedJson().execute();
        doctor.setId(doctorOutcome.getId());

        doctorRole = new PractitionerRole();
        doctorRole.setPractitionerTarget(doctor).setPractitioner(new Reference(doctor));
        doctorRole.setOrganizationTarget(this.myHospital).setOrganization(new Reference(this.myHospital));
        doctorRole.addCode(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/practitioner-role", "doctor",
                "A qualified/registered medical practitioner")));

        doctorRoleOutcome = client.create().resource(doctorRole).prettyPrint().encodedJson().execute();
        System.out.println("DoctorRole with ID: " + doctorRoleOutcome.getId());
        doctorRole.setId(doctorRoleOutcome.getId());

        // add to list
        doctorRoles.add(doctorRole);

    }

    public ArrayList<PractitionerRole> getdoctorRoles(){
        return this.doctorRoles;
    }
}
