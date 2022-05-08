package range_analysis;

import common.ErrorMessage;
import common.Utils;
import org.junit.Assert;
import org.junit.Test;
import soot.Main;
import soot.PackManager;
import soot.Transform;

public class WideningOperatorTest extends AnalysisTest {
    void add_analysis() {
        analysisName = IntraAnalysisTransformer.ANALYSIS_NAME;
        PackManager.v().getPack("jap").add(
                new Transform(analysisName,
                        IntraAnalysisTransformer.getInstance())
        );
    }

    @Test
    public void testWideningOperator() {
        addTestClass("inputs.WideningTest");
        Main.main(getArgs());

        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 16);
        addExpected(ErrorMessage.OUT_OF_BOUNDS_INDEX_ERROR, 27);
        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 29);
        addExpected(ErrorMessage.POSSIBLE_OUT_OF_BOUNDS_INDEX_WARNING, 44);
        Assert.assertEquals(expected, Utils.getErrors());
        Utils.resetErrors();
    }

}
