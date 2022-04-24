package range_analysis;

import soot.*;

import java.util.*;

public class InterAnalysisTransformer extends SceneTransformer {
    public static final String ANALYSIS_NAME = "wjap.intersignanalysis";

    private static InterAnalysisTransformer theInstance =
            new InterAnalysisTransformer();

    public static InterAnalysisTransformer getInstance() {
        return theInstance;
    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        System.out.println("Starting interprocedural analysis");
        InterSignAnalysis analysis = InterSignAnalysis.getInstance();
        // Analyze whole program
        analysis.analyzeProgram(Scene.v().getMainMethod());
        // Report all warnings
        analysis.reportWarnings();
        System.out.println("Done");
    }
}
