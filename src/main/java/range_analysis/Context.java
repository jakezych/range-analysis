package range_analysis;

import soot.SootMethod;
import java.util.ArrayList;
import java.util.List;

public class Context {
    static int CALL_STRING_CUTOFF = 3;
    // The function being called
    SootMethod fn;
    // The call string. A list of integers corresponding to call sites. Each call site should be assigned
    // a unique integer. For example, the statement `x().y().x()` has three different call sites, even
    // though `x()` is called twice on the same line.
    List<Integer> string;

    public Context(SootMethod fn, List<Integer> string) {
        this.fn = fn;
        this.string = string;
    }

    /**
     * Generates the new call string based on the old call string and the new call site.
     * Truncates to length CALL_STRING_CUTOFF
     *
     * @param callingCtx the current context
     * @param n the location of the current call
     * @return a new context, truncated to length
     */
    private static List<Integer> getSuffix(Context callingCtx, Integer n) {
        List<Integer> result;
        if (callingCtx != null) {
            result = new ArrayList<>(callingCtx.string);
            result.add(n);
            int i = result.size() - 1 - CALL_STRING_CUTOFF;
            while (result.size() > CALL_STRING_CUTOFF) {
                result.remove(i);
            }
        } else {
            result = new ArrayList<>();
            result.add(n);
        }
        return result;
    }

    /**
     * From the chapter on interprocedural analysis in the text.
     * Note that the signature is slightly different than
     * in the notes, because getCtx in the notes takes a sigma as well, but since it isn't used
     * with call strings we omit it here.
     * @param fn the method being called
     * @param callingCtx the current context
     * @param n the location of the current call
     * @return a new context, truncated to length
     */
    public static Context getCtx(SootMethod fn, Context callingCtx, Integer n) {
        List<Integer> newStr = getSuffix(callingCtx, n);
        return new Context(fn, newStr);
    }

    public String toString() {
        return "(fn: " + fn.getName() + ", string: " + string.toString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        return (o instanceof Context)
                && (this.fn.equals(((Context) o).fn))
                && (this.string.equals(((Context) o).string));
    }

    @Override
    public int hashCode() {
        return fn.getName().hashCode() + string.hashCode();
    }
}