package inputs;

public class IntraTestSimple {
    public static void test1() {
        int y, z;
        int[] array = new int[5];
        y = -5;
        z = -3;
        y = y * y;
        z = z - z;
        int ignore = array[y]; // ERROR
        ignore = array[z];
    }

    public static void test2() {
        int y, z, w;
        int[] array = new int[5];
        y = 1;
        z = -3;
        w = y - z;
        int v = z - y;
        int ignore = array[w];
        ignore = array[v]; // ERROR
    }

    public static void test3() {
        int y, z, w;
        int[] array = new int[5];
        y = 4;
        z = -3;
        if (condition())
            w = y;
        else
            w = z;
        int ignore = array[w]; // WARNING
    }

    public static void test4() {
        int x, y, z;
        int[] array = new int[5];
        x = getInt();
        y = 8;
        z = x * y;
        int ignore = array[z]; // WARNING
    }

    public static int getInt() {
        return 0;
    }

    public static boolean condition() {
        return true;
    }
}
