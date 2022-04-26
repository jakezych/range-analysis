package range_analysis;

import soot.SootMethod;

import java.util.*;

public class InterRangeAnalysis {
    public static Set<Context> worklist;
    public static Set<Context> analyzing;
    public static Map<Context, Summary> results;
    public static Map<Context, Set<Context>> callers;
    private static InterRangeAnalysis theInstance = new InterRangeAnalysis();

    static {
        worklist = new HashSet<>();
        analyzing = new HashSet<>();
        results = new HashMap<>();
        callers = new HashMap<>();
    }

    /**
     * We're using a singleton pattern here so that analysis results can be
     * looked up as they're collected.
     * e.g.,
     *   InterSignAnalysis.getInstance().callers can be accessed from IntraSignAnalysis
     */
    public static InterRangeAnalysis getInstance() {
        return theInstance;
    }

    public static void resetAnalysis() {
        theInstance = new InterRangeAnalysis();
    }

    public void analyzeProgram(SootMethod main) {
        // TODO: Implement me!
    }

    Sigma analyze(Context ctx, Sigma sigma_i) {
        // TODO: Implement me!
        // A good strategy will likely involve passing the context and the input
        // sigma to the SignAnalysis and then looking at the results to extract
        // the new sigma.
        return null;
    }

    public void reportWarnings() {
        // TODO: Implement me!
        // You may need to keep track of intra-analysis objects for each context
        // in order to go over their dataflow values and report warnings as needed
    }
}
