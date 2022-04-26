package range_analysis;

import common.ErrorMessage;
import common.Range;
import common.Utils;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static common.Operator.*;

/** https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/math/interpolation/LinearInterpolation.java
  * https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/math/interpolation/KochanekBartelsInterpolation.java
  * https://github.com/farhankhwaja/HeapSort/blob/master/HeapSort.java
  * ^ candidates for analyzing for errors
  */
public class IntraRangeAnalysis extends ForwardFlowAnalysis<Unit, Sigma> {
    // Holds the set of local variables
    private Set<Local> locals = new HashSet<>();

    // The calling context for the analysis
    // Null if no context (e.g., when only running intraprocedurally)
    private Context ctx;

    // The input sigma for this analysis
    private Sigma sigma_i;

    /**
     * Constructor with no context. This is useful for testing the intraprocedural
     * analysis on its own.
     */
    IntraRangeAnalysis(UnitGraph graph) {
        // Note the construction of a default Sigma
        this(graph, null, null);
    }

    /**
     * Allows creating an intra analysis with just the context and the input sigma,
     * since the unit graph can be grabbed from the function in the context.
     */
    IntraRangeAnalysis(Context ctx, Sigma sigma_i) {
        this(new ExceptionalUnitGraph(ctx.fn.getActiveBody()), ctx, sigma_i);
    }

    IntraRangeAnalysis(UnitGraph graph, Context ctx, Sigma sigma_i) {
        super(graph);
        this.ctx = ctx;
        this.sigma_i = sigma_i;

        // Collect locals
        this.locals.addAll(graph.getBody().getLocals());
    }

    // Runs the analysis
    public void run() {
        this.doAnalysis();
    }

    // an index is for sure out of range if it's lower and higher interval are both less than 0 or greater than the length
    private boolean isOutsideRange(Range r, int len) {
       return r.getLow() < 0 && r.getHigh() < 0 || r.getLow() >= len && r.getHigh() >= len;
    }

    /**
     * Report warnings. This will use the analysis results collected by the constructor.
     */
    public void reportWarnings() {
        Map<String, Integer> arrayLengths = new HashMap<String, Integer>();
        for (Unit u : this.graph) {
            Sigma sigmaBefore = this.getFlowBefore(u);
            // maps the most recent declaration of an array to its length to later be referenced
            if (u instanceof JAssignStmt) {
                JAssignStmt stmt = ((JAssignStmt) u);
                Value rightOp = stmt.getRightOp();
                if(rightOp.toString().contains("newarray")) {
                    arrayLengths.put(stmt.getLeftOp().toString(), rightOp.getUseBoxes().get(0).getValue().hashCode());
                }
            }
            if (u instanceof JAssignStmt && ((JAssignStmt) u).containsArrayRef()) {
                ArrayRef ref = ((JAssignStmt) u).getArrayRef();
                Value index = ref.getIndex();
                Range domain = getDomain(sigmaBefore, index);
                int len = 0;
                // pull the length from the map
                if (arrayLengths.containsKey(ref.getBase().toString())) {
                    len = arrayLengths.get(ref.getBase().toString());
                }
                if (isOutsideRange(domain, len)) {
                    // Reports an error for an index definitely being out of bounds
                    Utils.reportWarning(u, ErrorMessage.OUT_OF_BOUNDS_INDEX_ERROR);
                } else if (domain.getLow() < 0 || domain.getHigh() >= len) {
                    // Reports a warning for index being possibly out of bounds
                    Utils.reportWarning(u, ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING);
                }
            }
        }
    }

    /**
     *
     *  The abstraction function for an integer
     *
     * @param n the constant value for which to extract the sign
     * @return the abstracted value
     */
    private Range alpha(IntConstant n) {
        return new Range(n.value, n.value);
    }

    /**
     *
     *  Gets the domain value of an operand
     *
     * @param op the operand to get the domain value for
     * @return the domain value for the operand
     */
    private Range getDomain(Sigma inValue, Value op) {
        if (op instanceof IntConstant) {
            return alpha((IntConstant) op);
        }
        else if (op instanceof Local) {
            return inValue.map.get(op);
        }
        else {
            return new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }

    /**
     * Handle flowing the state when a variable is being assigned
     *
     * @param inValue  The initial Sigma at this point
     * @param unit     The current Unit
     * @param outValue The updated Sigma after the flow function
     */
    private void handleAssign(Sigma inValue, Unit unit, Sigma outValue) {
        JAssignStmt stmt = (JAssignStmt) unit;
        Local lhs = (Local) stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (rhs instanceof AddExpr) {
            AddExpr addStmt = (AddExpr) rhs;
            Value op1 = addStmt.getOp1();
            Value op2 = addStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                outValue.map.put(lhs, Range.combine(v1, v2, ADD));
            }
        } else if (rhs instanceof SubExpr) {
            SubExpr subStmt = (SubExpr) rhs;
            Value op1 = subStmt.getOp1();
            Value op2 = subStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                outValue.map.put(lhs, Range.combine(v1, v2, SUB));
            }
        } else if (rhs instanceof MulExpr) {
            MulExpr mulStmt = (MulExpr) rhs;
            Value op1 = mulStmt.getOp1();
            Value op2 = mulStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                outValue.map.put(lhs, Range.combine(v1, v2, MUL));
            }
        } else if (rhs instanceof DivExpr) {
            DivExpr divStmt = (DivExpr) rhs;
            Value op1 = divStmt.getOp1();
            Value op2 = divStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                outValue.map.put(lhs, Range.combine(v1, v2, DIV));
            }
        } else {
            outValue.map.put(lhs, getDomain(inValue, rhs));
        }
    }

    /**
     * Run flow function for this unit
     *
     * @param inValue  The initial Sigma at this point
     * @param unit     The current Unit
     * @param outValue The updated Sigma after the flow function
     */
    @Override
    protected void flowThrough(Sigma inValue, Unit unit, Sigma outValue) {
        inValue.copy(outValue);
        if (unit instanceof JAssignStmt) {
            handleAssign(inValue, unit, outValue);
        }
    }

    /**
     * Initial flow information at the start of a method
     */
    @Override
    protected Sigma entryInitialFlow() {
        if (this.sigma_i != null) {
            return this.sigma_i;
        } else {
            // initialize to Top
            return new Sigma(this.locals, new Range(Integer.MIN_VALUE, Integer.MAX_VALUE));
        }
    }

    /**
     * Initial flow information at each other program point
     */
    @Override
    protected Sigma newInitialFlow() {
        // initialize to Bottom
        return new Sigma(this.locals, new Range(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

    /**
     * Join at a program point lifted to sets
     */
    @Override
    protected void merge(Sigma in1, Sigma in2, Sigma out) {
        for (Map.Entry<Local, Range> entry1 : in1.map.entrySet()) {
            Local l = entry1.getKey();
            Range v1 = entry1.getValue();
            if (in2.map.containsKey(l))  {
                Range v2  = in2.map.get(l);
                out.map.put(l, Sigma.join(v1, v2));
            }
        }
    }

    /**
     * Copy for sets
     */
    @Override
    protected void copy(Sigma source, Sigma dest) {
        source.copy(dest);
    }
}
