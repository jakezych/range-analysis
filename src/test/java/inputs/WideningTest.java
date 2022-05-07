package inputs;

public class WideningTest {
    // Test to see if the analysis will terminate
    public static void testTermination() {
        int x, y;
        int[] array = new int[5];
        y = 10;
        x = 0;
        while (x != y) {
            x = x + 1;
        }
        y = 0;
        int ignore = array[y]; // OKAY
        ignore = array[x]; // WARNING
    }

    // TODO: Test how it affects the precision
    public static void testPrecision() {

    }

    // TODO: Test constant value truncation
    public static void testConstantTruncation() {

    }

}
