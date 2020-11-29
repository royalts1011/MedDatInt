import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.r4.model.*;

import javax.management.relation.Role;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
        String outputPath = "output/";
        Utils ut = new Utils(ctxR4, outputPath);

        ut.createAntonie();
        ut.getAllNames();
        ut.makeWholeHospital();

    }


}