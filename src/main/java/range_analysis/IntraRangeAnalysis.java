package range_analysis;

import common.ErrorMessage;
import common.Range;
import common.Utils;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;
import java.util.regex.Pattern;

import static common.Operator.*;

/** https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/math/interpolation/LinearInterpolation.java
  * https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/math/interpolation/KochanekBartelsInterpolation.java
  * https://github.com/farhankhwaja/HeapSort/blob/master/HeapSort.java
  * ^ candidates for analyzing for errors
  */

/**
 * notes to self:
 * apply widening operator when detecting a loop? need to see how we can detect a loop
 * keep track of previous lattice value for each variable at every program location
 *
 * widen will probably just be a method here
 * need to instrument program to track previous lattice values
 * map line numbers -> sigma value (previous sigma in, gets saved at the start of flow)
 * find all loops using loop finder,
 */
public class IntraRangeAnalysis extends ForwardFlowAnalysis<Unit, Sigma> {
    // Holds the set of local variables
    private Set<Local> locals = new HashSet<>();

    // The calling context for the analysis
    // Null if no context (e.g., when only running intraprocedurally)
    private Context ctx;

    // The input sigma for this analysis
    private Sigma sigma_i;

    // All the constants in the body of the function
    private Set<Integer> constants = new HashSet<>();

    // Used for the widening operator, keeps track of the previous sigma_i value at each instruction point
    private Map<Integer, Sigma> previousSigma = new HashMap<>();

    // Used for the widening operator to determine whether an if statement is a loop head
    private Set<Stmt> loopHeads = new HashSet<>();

    // Used to identify constants in the program before performing the intraprocedural analysis
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

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

        LoopFinder loopFinder = new LoopFinder();

        // Find all loop guards
        Collection<Loop> loops = loopFinder.getLoops(graph);
        for (Loop l : loops) {
            loopHeads.add(l.getHead());
        }

        // Preprocess all constants to gather the max value any variable is set to.
        for (ValueBox box : graph.getBody().getUseAndDefBoxes()) {
            if (box instanceof ImmediateBox) {
                ImmediateBox constantBox = (ImmediateBox) box;
                String constantExpr = constantBox.getValue().toString();
                if (isNumeric(constantExpr)) {
                    constants.add(Integer.parseInt(constantExpr));
                }
            }
        }
    }

    // https://www.baeldung.com/java-check-string-number
    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
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

    int minWiden(int l1, int l2) {
        if (l1 <= l2) {
            return l1;
        } else {
            int val = Integer.MIN_VALUE;
            for (Integer constant: constants) {
                if (constant <= l2) {
                    if (constant > val) {
                        val = constant;
                    }
                }
            }
            System.out.println("minWiden val: " + val);
            return val;
        }
    }

    int maxWiden(int h1, int h2) {
        if (h1 >= h2) {
            return h1;
        } else {
            int val = Integer.MAX_VALUE;
            for (Integer constant: constants) {
                if (constant >= h2) {
                    if (constant < val) {
                        val = constant;
                    }
                }
            }
            System.out.println("maxWiden val: " + val);
            return val;
        }
    }

    /**
     * Widens every variable's range at a given program point.
     *
     * @param prev sigma value the previous time this program location was evaluated in flowThrough
     * @param current the current sigma value at a program location
     * @param out the resulting sigma after each range was widened.
     * @return
     */
    private void widen(Sigma prev, Sigma current, Sigma out) {
        System.out.println("widen: prev: " + prev.map.toString() + " current: " + current.map.toString());
        for (Map.Entry<Local, Range> entry1 : prev.map.entrySet()) {
            Local l = entry1.getKey();
            Range prevRange = entry1.getValue();
            if (current.map.containsKey(l))  {
                Range currentRange  = current.map.get(l);
                Range widenedRange = new Range(minWiden(prevRange.getLow(), currentRange.getLow()), maxWiden(prevRange.getHigh(), currentRange.getHigh()));
                out.map.put(l, widenedRange);
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
     * Handle evaluating an expression given a current state
     *
     * @param inValue  The initial Sigma at this point
     * @param lhs The local variable being assigned to (if it exists)
     * @param rhs The expression to evaluate the assignment statement for
     * @param outValue The Sigma after evaluating the unit
     * @return the lattice value to map the left hand side variable to
     */
    private Range evaluateExpression(Sigma inValue, Local lhs, Value rhs, Sigma outValue) {
        if (rhs instanceof AddExpr) {
            AddExpr addStmt = (AddExpr) rhs;
            Value op1 = addStmt.getOp1();
            Value op2 = addStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                return Range.combine(v1, v2, ADD);
            } else {
                return outValue.map.get(lhs);
            }
        } else if (rhs instanceof SubExpr) {
            SubExpr subStmt = (SubExpr) rhs;
            Value op1 = subStmt.getOp1();
            Value op2 = subStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                return Range.combine(v1, v2, SUB);
            } else {
                return outValue.map.get(lhs);
            }
        } else if (rhs instanceof MulExpr) {
            MulExpr mulStmt = (MulExpr) rhs;
            Value op1 = mulStmt.getOp1();
            Value op2 = mulStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                return Range.combine(v1, v2, MUL);
            } else {
                return outValue.map.get(lhs);
            }
        } else if (rhs instanceof DivExpr) {
            DivExpr divStmt = (DivExpr) rhs;
            Value op1 = divStmt.getOp1();
            Value op2 = divStmt.getOp2();
            Range v1 = getDomain(inValue, op1);
            Range v2 = getDomain(inValue, op2);
            if (!v1.isBottom() && !v2.isBottom()) {
                return Range.combine(v1, v2, DIV);
            } else {
                return outValue.map.get(lhs);
            }
        } else {
            return getDomain(inValue, rhs);
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
        System.out.print("unit: " + unit.toString());
        if (ctx != null) {
            System.out.println(" context: " + ctx.toString());
        } else {
            System.out.println();
        }
        inValue.copy(outValue);
        if (unit instanceof JAssignStmt) {
            JAssignStmt assignStmt = (JAssignStmt) unit;
            Local lhs = (Local) assignStmt.getLeftOp();
            Value rhs = assignStmt.getRightOp();
            // processes an assign statement containing a function call as long as interprocedural analysis is being run
            if (assignStmt.containsInvokeExpr() && ctx != null) {
                // get the method being invoked
                SootMethod method = assignStmt.getInvokeExpr().getMethod();
                Context calleeCtx = Context.getCtx(method, ctx, unit.getJavaSourceStartLineNumber());
                List<Value> localValues = assignStmt.getInvokeExpr().getArgs();
                // map the return value to the lhs side by getting its lattice value
                for (Map.Entry<Local, Range> entry1 : resultsFor(calleeCtx, inValue, localValues).map.entrySet()) {
                    if (entry1.getKey().getName().equals("return")) {
                        outValue.map.put(lhs, entry1.getValue());
                    }
                }

                // initialize an entry in callers if this context has not been used before
                if (!InterRangeAnalysis.callers.containsKey(calleeCtx)) {
                    InterRangeAnalysis.callers.put(calleeCtx, new HashSet<>());
                }
                InterRangeAnalysis.callers.get(calleeCtx).add(ctx);
            } else {
                // only assign if the left-hand side is a variable
                if (!(assignStmt.getLeftOp() instanceof Local)) {
                    return;
                }
                System.out.println("evaluating expression in assign case: " + unit.toString() + " with inValue: " + inValue);
                Range value = evaluateExpression(inValue, lhs, rhs, outValue);
                outValue.map.put(lhs, value);
            }
        } else if (unit instanceof JIfStmt) {
            Stmt conditional = (Stmt) unit;
            JIfStmt ifStmt = (JIfStmt) unit;
            // loop guard found
            if (loopHeads.contains(conditional)) {
                Value condition = ifStmt.getCondition();
                System.out.println(condition);
                System.out.println("inValue:" + inValue.map.toString());
                Integer lineNumber = unit.getJavaSourceStartLineNumber();
                if (previousSigma.containsKey(lineNumber)) {
                    System.out.println("calling widen");
                    widen(previousSigma.get(lineNumber), inValue, outValue);
                } else {
                    Sigma entry = new Sigma();
                    inValue.copy(entry);
                    previousSigma.put(lineNumber, entry);
                }
            }
            // if the range of the variable in the conditional makes it possible to make it true, don't widen and instead
            // update that variable in sigma_out
            // need to extract value to assign to (IntConstant) and Local variable somehow
            ConditionExpr guard = (ConditionExpr) ifStmt.getCondition();

            Local l;
            if (guard.getOp1() instanceof Local) {
                l = (Local) guard.getOp1();
            } else if (guard.getOp2() instanceof Local) {
                l = (Local) guard.getOp2();
            } else {
                // no update necessary
                return;
            }
            Range lRange = outValue.map.get(l);
            IntConstant intExpr;
            Value rhs;
            if (guard.getOp1() instanceof IntConstant) {
                intExpr = (IntConstant) guard.getOp1();
                rhs = guard.getOp1();
            } else if (guard.getOp2() instanceof IntConstant) {
                intExpr = (IntConstant) guard.getOp2();
                rhs = guard.getOp2();
            } else {
                // no constant found
                return;
            }
            System.out.println("l: " + l + " rhs: " + rhs);
            // if n is within range, assign it
            if (lRange.getLow() <= intExpr.value && intExpr.value <= lRange.getHigh()) {
                System.out.println("Assigning " + l + " to " + rhs);
                System.out.println("evaluating expression in loop: " + unit.toString());
                Range value = evaluateExpression(inValue, l, rhs, outValue);
                outValue.map.put(l, value);
            }
        } else if (unit instanceof JReturnStmt) {
            JReturnStmt returnStmt = (JReturnStmt) unit;
            Local result = Jimple.v().newLocal("return", returnStmt.getOp().getType());
            Value expression = returnStmt.getOp();
            System.out.println("evaluating expression in return statement: " + unit.toString());
            Range value = evaluateExpression(inValue, result, expression, outValue);
            // perform a merge over the return values for the result being returned
            if (outValue.map.containsKey(result)) {
                Range previousValue = outValue.map.get(result);
                outValue.map.put(result, Sigma.join(previousValue, value));
            } else {
                outValue.map.put(result, value);
            }
        }
        if (ctx != null) {
            InterRangeAnalysis.results.get(ctx).output = outValue;
        }
        System.out.println("outValue:" + outValue.map.toString());
    }

    /**
     * Returns the results when running the analysis given the current context and input state.
     *
     * @param ctx context to check the current results for
     * @param sigma_i input state to check the current results
     * @param localValues values of the local variables being passed in as parameters to ctx.fn
     * @return the resulting state given the context and input state
     */
    public Sigma resultsFor(Context ctx, Sigma sigma_i, List<Value> localValues) {
        Map<Context, Summary> results = InterRangeAnalysis.results;
        Set<Context> analyzing = InterRangeAnalysis.analyzing;
        if (results.containsKey(ctx)) {
            if (InterRangeAnalysis.isLessPreciseThan(sigma_i, results.get(ctx).input)) {
                return results.get(ctx).output;
            }
            else {
                Sigma joined = new Sigma();
                merge(sigma_i, results.get(ctx).input, joined);
                results.get(ctx).input = joined;
            }
        } else {
            // create new inValue that contains the parameters of the method with their mappings from the previous analysis
            Sigma newInput = new Sigma(ctx.fn.getActiveBody().getParameterLocals(), new Range(Integer.MIN_VALUE, Integer.MAX_VALUE));
            List<Local> locals = ctx.fn.getActiveBody().getParameterLocals();
            for(int i = 0; i < locals.size(); i++) {
                Local parameter = (Local) localValues.get(i);
                Local variable = locals.get(i);
                // map the local variable to the parameter's domain value
                newInput.map.put(variable, sigma_i.map.get(parameter));
            }
            Sigma newOutput = new Sigma(ctx.fn.getActiveBody().getParameterLocals(), new Range(Integer.MAX_VALUE, Integer.MIN_VALUE));
            Summary summary = new Summary(newInput, newOutput);
            results.put(ctx, summary);
        }
        if(analyzing.contains(ctx)) {
            return results.get(ctx).output;
        } else {
            return InterRangeAnalysis.analyze(ctx, results.get(ctx).input);
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
            if (in2.map.containsKey(l)) {
                Range v2 = in2.map.get(l);
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
