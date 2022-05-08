package range_analysis;

import common.ErrorMessage;
import common.Utils;
import org.junit.Assert;
import org.junit.Test;
import soot.Main;
import soot.PackManager;
import soot.Transform;

public class IntraAnalysisSimpleTest extends AnalysisTest {
    void add_analysis() {
        analysisName = IntraAnalysisTransformer.ANALYSIS_NAME;
        PackManager.v().getPack("jap").add(
                new Transform(analysisName,
                        IntraAnalysisTransformer.getInstance())
        );
    }

    @Test
    public void testIntraAnalysisSimple() {
        addTestClass("inputs.IntraTestSimple");
        Main.main(getArgs());

        addExpected(ErrorMessage.OUT_OF_BOUNDS_INDEX_ERROR, 11);
        addExpected(ErrorMessage.OUT_OF_BOUNDS_INDEX_ERROR, 23);
        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 35);
        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 44);
        Assert.assertEquals(expected, Utils.getErrors());
        Utils.resetErrors();
    }
}
