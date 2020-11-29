import ca.uhn.fhir.context.FhirContext;

public class Uebung_6 {

    public static void main(String[] args) {
        // Create context
        FhirContext ctxR4 = FhirContext.forR4();

        /**
         * Theoretisch für weniger Server Checks bei vertrauten ServerBases. Funktioniert aber leider noch nicht.
         *
         *         // Disable server validation (don't pull the server's metadata first)
         *         ctxR4.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
         *         // configure it for deferred child scanning
         *         ctxR4.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
         */
        String outputFolder = "output";
        Utils ut = new Utils(ctxR4, outputFolder);

        ut.createAntonie();
        ut.getAllNames();
        ut.makeWholeHospital();

    }


}