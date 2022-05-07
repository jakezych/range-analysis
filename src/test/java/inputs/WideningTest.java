package inputs;

public class WideningTest {
    // Test to see if the analysis will terminate
    public static void testTermination() {
        int x, y;
        int[] array = new int[5];
        y = -10;
        x = 0;
        while (x != y) {
            x = x + 1;
        }
        y = 0;
        int ignore = array[y]; // OKAY
        // x should range from [0, inf], implies possible but not guaranteed out of bounds error
        ignore = array[x]; // WARNING
    }

    public static void testForLoop() {
        int x, y;
        int[] array = new int[5];
        x = 0;
        y = 8;
        for (int i = 0; i < y; i++) {
            x = x + 1;
        }
        int ignore = array[y]; // ERROR y ranges from [8,8]
        // x should range from [0, inf], implies possible but not guaranteed out of bounds error
        ignore = array[x]; // WARNING
    }

    // TODO: Test constant value truncation
    public static void testConstantTruncation() {

    }

}
