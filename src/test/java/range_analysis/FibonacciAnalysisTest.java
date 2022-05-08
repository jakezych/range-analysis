package range_analysis;

import common.ErrorMessage;
import common.Utils;
import org.junit.Assert;
import org.junit.Test;
import soot.Main;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class FibonacciAnalysisTest extends AnalysisTest{
    void add_analysis() {
        analysisName = InterAnalysisTransformer.ANALYSIS_NAME;
        PackManager.v().getPack("wjap").add (
                new Transform(analysisName,
                        InterAnalysisTransformer.getInstance())
        );

        // These options are required when analyzing a whole program
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
    }

    @Test
    public void testFibonacciAnalysis() {
        addTestClass("inputs.FibonacciTest");
        Main.main(getArgs());

        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 28);
        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 29);
        Assert.assertEquals(expected, Utils.getErrors());
        Utils.resetErrors();
    }
}
