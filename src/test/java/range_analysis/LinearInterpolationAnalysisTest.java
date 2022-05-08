package range_analysis;

import common.Utils;
import org.junit.Test;
import soot.Main;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class LinearInterpolationAnalysisTest extends AnalysisTest {
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
    public void testLinearInterpolation() {
        addTestClass("inputs.LinearInterpolationTest");
        Main.main(getArgs());

        System.out.println(Utils.getErrors());
        Utils.resetErrors();
    }
}
