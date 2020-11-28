import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import javax.management.relation.Role;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Uebung_6 {


    public static void main(String[] args) {

        FhirContext ctx = FhirContext.forR4();

        File dir = new File("output");
        if (!dir.exists()) {
            dir.mkdir();
        }

        String outputPath = "output/";

        patBuilder patBuild = new patBuilder(ctx);
        patBuild.buildPatient();
        utils.getAllNames(ctx, outputPath);
        utils.makeWholeHospital(ctx, outputPath);
        // utils.anotherTryForHospital(ctx, outputPath);

    }

}



