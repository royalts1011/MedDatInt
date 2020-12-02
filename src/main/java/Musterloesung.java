import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;

public class Musterloesung {

	public static void main(String[] args) {

		System.out.println("--- FHIR Übung 6 ---");

		// FHIR Context
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		parser.setPrettyPrint(true);

		// Client for the IMI Server
		String serverBase = "https://funke.imi.uni-luebeck.de/public/fhir";
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// Empty Patient Instance
		System.out.println("--- FHIR Übung 2.1 ---");
		Patient antonie = new Patient();

		// Identifier
		Identifier identifier = new Identifier();
		identifier.setValue("1234567890");
		identifier.setSystem("http://kh-uzl.de/fhir/patients");
		antonie.addIdentifier(identifier);

		// Official Name
		HumanName gruenlich = new HumanName();
		gruenlich.addGiven("Antonie");
		gruenlich.setFamily("Grünlich");
		gruenlich.setUse(NameUse.OFFICIAL);
		antonie.addName(gruenlich);

		// Maiden Name
		HumanName bruddenbooks = new HumanName();
		bruddenbooks.addGiven("Antonie");
		bruddenbooks.setFamily("Bruddenbooks");
		bruddenbooks.setUse(NameUse.MAIDEN);
		antonie.addName(bruddenbooks);

		// Birthday
		Calendar cal = Calendar.getInstance();
		// CAVE: Java integer starts by 0!
		cal.set(1827, 7, 6);
		antonie.setBirthDate(cal.getTime());

		// MaritalStatus - see http://hl7.org/fhir/valueset-marital-status.html
		antonie.setMaritalStatus(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/marital-status", "M",
				"A current marriage contract is active")));

		MethodOutcome patientOutcome = client.create().resource(antonie).prettyPrint().encodedJson().execute();
		System.out.println("Patient with ID: " + patientOutcome.getId());
		antonie.setId(patientOutcome.getId());

		System.out.println("--- FHIR Übung 2.2 ---");

		List<Patient> patients = new ArrayList<>();
		Bundle bundle = client.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
		patients.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, Patient.class));

		// Load the subsequent pages
		while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
			bundle = client.loadPage().next(bundle).execute();
			patients.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, Patient.class));
		}

		System.out.println("Patient Count: " + patients.size());
		for (Patient patient : patients) {
			if (patient.getName().size() > 0) {
				System.out.println(patient.getName().get(0).getNameAsSingleString());
			}
		}

		System.out.println("--- FHIR Übung 2.3 ---");

		Bundle organizations = client.search().forResource(Organization.class)
				.where(Organization.NAME.contains().value("MIO")).returnBundle(Bundle.class).execute();

		Organization mioHospital = null;

		if (organizations.getEntry().size() == 0) {
			mioHospital = new Organization();
			mioHospital.setName("MIO Krankenhaus Lübeck");
			MethodOutcome orgaOutcome = client.create().resource(mioHospital).prettyPrint().encodedJson().execute();
			mioHospital.setId(orgaOutcome.getId());
		}

		if (organizations.getEntry().size() == 1) {
			mioHospital = (Organization) organizations.getEntry().get(0).getResource();
		}

		if (organizations.getEntry().size() > 1) {
			int size = organizations.getEntry().size();
			int randomNum = ThreadLocalRandom.current().nextInt(0, size + 1);
			mioHospital = (Organization) organizations.getEntry().get(randomNum).getResource();
		}

		Organization birthStation = new Organization();
		birthStation.setName("MIO Birth Station");
		birthStation.setPartOfTarget(mioHospital);
		birthStation.setPartOf(new Reference(mioHospital));

		// Create BirthStation if it is not on the Server
		MethodOutcome birthStationOutcome = client.create().resource(birthStation).conditional()
				.where(Organization.NAME.matches().value("MIO Birth Station")).prettyPrint().encodedJson().execute();
		System.out.println("BirthStation with ID: " + birthStationOutcome.getId());
		birthStation.setId(birthStationOutcome.getId());

		System.out.println("--- FHIR Übung 2.4 ---");

		Practitioner doctor = new Practitioner();
		HumanName doctorsName = new HumanName();
		doctorsName.addPrefix("Dr.");
		doctorsName.addGiven("Frauke");
		doctorsName.setFamily("Lehmann");
		doctor.addName(doctorsName);

		MethodOutcome doctorOutcome = client.create().resource(doctor).conditional()
				.where(Practitioner.FAMILY.matches().value("Lehmann")).and(Practitioner.GIVEN.matches().value("Frauke"))
				.prettyPrint().encodedJson().execute();
		System.out.println("Doctor with ID: " + doctorOutcome.getId());
		doctor.setId(doctorOutcome.getId());

		PractitionerRole doctorRole = new PractitionerRole();
		doctorRole.setPractitionerTarget(doctor).setPractitioner(new Reference(doctor));
		doctorRole.setOrganizationTarget(birthStation).setOrganization(new Reference(birthStation));
		doctorRole.addCode(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/practitioner-role", "doctor",
				"A qualified/registered medical practitioner" + "")));

		MethodOutcome doctorRoleOutcome = client.create().resource(doctorRole).prettyPrint().encodedJson().execute();
		System.out.println("DoctorRole with ID: " + doctorRoleOutcome.getId());
		doctorRole.setId(doctorRoleOutcome.getId());
		
		Practitioner nurse = new Practitioner();
		HumanName nurseName = new HumanName();
		nurseName.addGiven("Harald");
		nurseName.setFamily("Meier");
		nurse.addName(nurseName);

		MethodOutcome nurseOutcome = client.create().resource(nurse).conditional()
				.where(Practitioner.FAMILY.matches().value("Meier")).and(Practitioner.GIVEN.matches().value("Harald"))
				.prettyPrint().encodedJson().execute();
		System.out.println("Nurse with ID: " + nurseOutcome.getId());
		nurse.setId(nurseOutcome.getId());

		PractitionerRole nurseRole = new PractitionerRole();
		nurseRole.setPractitionerTarget(doctor).setPractitioner(new Reference(doctor));
		nurseRole.setOrganizationTarget(birthStation).setOrganization(new Reference(birthStation));
		nurseRole.addCode(new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/practitioner-role", "nurse",
				"A practitioner with nursing experience that may be qualified/registered")));

		MethodOutcome nurseRoleOutcome = client.create().resource(doctorRole).prettyPrint().encodedJson().execute();
		System.out.println("NurseRole with ID: " + nurseRoleOutcome.getId());

		System.out.println("--- FHIR Übung 2.5 ---");

		Appointment birthAppointment = new Appointment();
		birthAppointment.setStatus(AppointmentStatus.BOOKED);
		Calendar birthDateCal = Calendar.getInstance();
		// CAVE: Java integer starts by 0!
		birthDateCal.set(1846, 9, 1);
		birthAppointment.setStart(birthDateCal.getTime());
		birthAppointment.addParticipant(new AppointmentParticipantComponent().setActor(new Reference(antonie)));
		birthAppointment.addParticipant(new AppointmentParticipantComponent().setActor(new Reference(doctor)));
		birthAppointment.addParticipant(new AppointmentParticipantComponent().setActor(new Reference(nurse)));
		MethodOutcome appointmentOutcome = client.create().resource(birthAppointment).prettyPrint().encodedJson().execute();
		System.out.println("Appointment with ID: " + appointmentOutcome.getId());
		birthAppointment.setId(appointmentOutcome.getId());

		Encounter birthEncoutner = new Encounter();
		birthEncoutner.setStatus(EncounterStatus.PLANNED);
		birthEncoutner.setClass_(new Coding("http://terminology.hl7.org/ValueSet/v3-ActEncounterCode", "IMP",
				"A patient encounter where a patient is admitted by a hospital or equivalent facility, assigned to a location where patients generally stay at least overnight and provided with room, board, and continuous nursing service."));
		birthEncoutner.setServiceType(
				new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/service-type", "263", "Birth")));
		birthEncoutner.setSubjectTarget(antonie).setSubject(new Reference(antonie));
		birthEncoutner.addParticipant(new EncounterParticipantComponent().setIndividual(new Reference(doctor)));
		birthEncoutner.addParticipant(new EncounterParticipantComponent().setIndividual(new Reference(nurse)));
		birthEncoutner.addReasonCode(
				new CodeableConcept(new Coding("http://hl7.org/fhir/ValueSet/encounter-reason", "3950001", "Birth")));
		birthEncoutner.addAppointment(new Reference(birthAppointment));
		MethodOutcome entcounterOutcome = client.create().resource(birthEncoutner).prettyPrint().encodedJson()
				.execute();
		System.out.println("Entcounter with ID: " + entcounterOutcome.getId());
	}

}
