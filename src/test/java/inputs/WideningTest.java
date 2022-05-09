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

    // Tests the widening operator making use of constants in the program to bound values
    public static void testConstantTruncation() {
        int x, y;
        int[] array = new int[10];
        x = 1;
        y = 1;
        while (x != 10) {
            x = x + 1;
            y = y - 1;
        }

        int ignore = array[x - 1]; // OKAY x - 1 = [9, 9]
        ignore = array[y]; // WARNING y = [-inf, 1]
    }

}
