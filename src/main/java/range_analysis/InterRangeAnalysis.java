package range_analysis;

import common.Range;
import soot.Local;
import soot.SootMethod;

import java.util.*;

public class InterRangeAnalysis {
    public static Set<Context> worklist;
    public static Set<Context> analyzing;
    public static Map<Context, Summary> results;
    public static Map<Context, Set<Context>> callers;
    public static Map<Context, IntraRangeAnalysis> analyses;
    private static InterRangeAnalysis theInstance = new InterRangeAnalysis();

    static {
        worklist = new HashSet<>();
        analyzing = new HashSet<>();
        results = new HashMap<>();
        callers = new HashMap<>();
        analyses = new HashMap<>();
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
        Context initialContext = Context.getCtx(main, null, 0);
        worklist.add(initialContext);
        Iterable<Local> locals = main.getActiveBody().getLocals();
        // Initial is Top, rest are Bottom
        results.put(initialContext, new Summary(new Sigma(locals, new Range(Integer.MIN_VALUE, Integer.MAX_VALUE)), new Sigma(locals,  new Range(Integer.MAX_VALUE, Integer.MIN_VALUE))));

        while (worklist.size() > 0) {
            Context context = worklist.iterator().next();
            worklist.remove(context);
            analyze(context, results.get(context).input);
        }
    }


    /**
     * Returns whether the state s1 is at least as precise as s2 where the "is at least as precise" operator
     * is defined point wise.
     * @param s1 the left operand state
     * @param s2 the right operand state
     * @return true if s1 is at least as precise as s2, false otherwise
     */
    public static boolean isLessPreciseThan(Sigma s1, Sigma s2) {
        for (Map.Entry<Local, Range> entry1 : s1.map.entrySet()) {
            Local l = entry1.getKey();
            Range v1 = entry1.getValue();
            if (s2.map.containsKey(l))  {
                Range v2 = s2.map.get(l);
                if(!(Sigma.isLessThan(v1, v2))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Sigma analyze(Context ctx, Sigma sigma_i) {
        Sigma sigma_o = results.get(ctx).output;
        analyzing.add(ctx);
        IntraRangeAnalysis analysis = new IntraRangeAnalysis(ctx, sigma_i);
        // run the analysis to get the new outputted sigma
        analysis.run();
        analyses.put(ctx, analysis);
        Sigma sigma_o_new = results.get(ctx).output;
        analyzing.remove(ctx);
        if (!(isLessPreciseThan(sigma_o_new, sigma_o))) {
            Sigma joined = new Sigma();
            analysis.merge(sigma_o, sigma_o_new, joined);
            results.put(ctx, new Summary(sigma_i, joined));
            if (callers.containsKey(ctx)) {
                for (Context c : callers.get(ctx)) {
                    worklist.add(c);
                }
            }
        }
        return sigma_o_new;
    }

    public void reportWarnings() {
        for (IntraRangeAnalysis analysis : analyses.values()) {
            analysis.reportWarnings();
        }
    }
}
