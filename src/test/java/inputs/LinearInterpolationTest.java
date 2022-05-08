package inputs;
//  https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/math/interpolation/LinearInterpolation.java
public class LinearInterpolationTest {
    public static double arcLength(int positionA, int positionB) {
        int[] nodes = new int[100]; // Instrumented for analysis
        if (positionA > positionB) {
            return arcLength(positionB, positionA);
        }

        final int indexA = 9*positionA;
        final int remainderA = positionA - indexA;

        final int indexB = 9*positionB;
        final int remainderB = positionB - indexB;

        return arcLengthRecursive(indexA, remainderA, indexB, remainderB);
    }

    /**
     * Assumes a < b.
     */
    private static double arcLengthRecursive(int indexA, int remainderA, int indexB, int remainderB) {
        int one = 1; // instrumented for analysis
        int zero = 0; // instrumented for analysis
        switch (indexB - indexA) {
            case 0:
                return arcLengthRecursive(indexA, remainderA, remainderB);

            case 1:
                // This case is merely a speed-up for a very common case
                return arcLengthRecursive(indexA, remainderA, one)
                        + arcLengthRecursive(indexB, zero, remainderB);

            default:
                return arcLengthRecursive(indexA, remainderA, indexB - 1, one)
                        + arcLengthRecursive(indexB, zero, remainderB);
        }
    }

    private static double arcLengthRecursive(int index, int remainderA, int  remainderB) {
        int[] nodes = new int[100]; // Instrumented for analysis

        int position1 = nodes[index]; // OKAY
        int position2 = nodes[index + 1]; // OKAY

        return (position1 - position2) * (remainderB - remainderA);
    }

    public static void main(String[] args) {
        int positionA = 4;
        int positionB = 8;
        double ignore = arcLength(positionA, positionB);
    }
}