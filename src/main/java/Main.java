import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args){
        // Set up FhirContext and Client for the IMI Server

        FhirContext ctx = FhirContext.forR4();
        String serverBase = "https://funke.imi.uni-luebeck.de/public/fhir";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        // Make a new ImmunizationPass which has Patient, Immunizations,.....
        ImmunizationPass I = new ImmunizationPass(client, ctx);
//        I.buildImmunizationPass();

        List<DiagnosticReport> reports = new ArrayList<>();
        Bundle bundle = client.search().forResource(DiagnosticReport.class).returnBundle(Bundle.class).execute();
        reports.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, DiagnosticReport.class));

        // Load the subsequent pages
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = client.loadPage().next(bundle).execute();
            reports.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, DiagnosticReport.class));
        }

        List<DiagnosticReport> selected = new ArrayList<>();
        selected.add(reports.get(4000));
        selected.add(reports.get(4100));
        selected.add(reports.get(4130));
        selected.add(reports.get(4099));
        System.out.println("Patient Count: " + reports.size());


//        // get the bundle to be able to generate visualisation
//        Composition immuPass = I.getTotalImmunizationPass();
//        HTMLBuilder builder = new HTMLBuilder();
//        String genNarr = builder.enhancePass(immuPass);
//
//        // write HTML File to output directory
//
//        File file = new File("output/pass_visu.html");
//        try (FileWriter fileWriter = new FileWriter(file)) {
//            fileWriter.write(genNarr);
//            fileWriter.flush();
//            fileWriter.close();
//        } catch (Exception e)   {
//            e.printStackTrace();
//        }

    }



}
