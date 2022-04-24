package range_analysis;

import common.ErrorMessage;
import common.ErrorReport;
import common.Utils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import soot.G;
import soot.Scene;
import soot.options.Options;

import java.util.HashSet;
import java.util.Set;

import static soot.SootClass.SIGNATURES;

public abstract class AnalysisTest {
    private String testClass;
    String analysisName;
    Set<ErrorReport> expected;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(600);

    /**
     * Reset Soot and errors, then reinitialize Soot
     */
    @Before
    public void initTest() {
        System.out.println("Initializing tests");
        G.reset();
        Utils.resetErrors();
        expected = new HashSet<>();
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_J);
        Options.v().set_prepend_classpath(true);
        Options.v().set_output_dir("sootOutput");
        String sep = System.getProperty("file.separator");
        String sootClasspath = "build" + sep + "classes" + sep + "java" + sep + "test";
        Options.v().set_soot_classpath(sootClasspath);
        add_analysis();
        System.out.println("Done initializing");
    }

    abstract void add_analysis();

    String[] getArgs() {
        return new String[]{ "-p", analysisName, "on", testClass };
    }

    void addTestClass(String testClass) {
        this.testClass = testClass;
        Scene.v().addBasicClass(testClass, SIGNATURES);
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

    void addExpected(ErrorMessage m, int line) {
        expected.add(new ErrorReport(m, line));
    }
}
