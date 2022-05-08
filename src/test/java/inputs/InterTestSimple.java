package inputs;

public class InterTestSimple {
    public static int neg(int n) {
        return -1 * n;
    }

    public static int more_neg(int n) {
        return neg(n) - 1;
    }

    public static int even_more_neg(int n) {
        return more_neg(n) - 1;
    }

    public static void main(String[] args) {
        int x, y, z, w;
        int[] array = new int[5];
        int ignore;

        if (condition()) {
            y = 1;
            z = -1;
        } else {
            y = 2;
            z = -2;
        }

        ignore = array[y]; // OKAY y = [2, 2]
        ignore = array[z]; // ERROR Z = [-2, -2]

        // Analysis stack:
        // (fn: neg, ctx: neg@35, input: 2) = -2
        w = neg(y);

        // Analysis stack:
        // (fn: neg, ctx: neg@39, input: -2) = 2
        x = neg(z);

        ignore = array[w]; // ERROR w = [-2, -2]
        ignore = array[x]; // OKAY x = [2, 2]

        // Analysis stack:
        // (fn: more_neg, ctx: more_neg@45, input: 2) = [-3. -3]
        // (fn: neg, ctx: more_neg@45, neg@10, input: 2) = [-2, -2]
        w = more_neg(y);

        // Analysis stack:
        // (fn: more_neg, ctx: more_neg@52, input: -2) = [-2, 2]
        // (fn: neg, ctx: more_neg@52, neg@10, input: -2) = [2, 2]
        x = more_neg(z);

        ignore = array[w]; // ERROR w = [-2, -2]
        ignore = array[x]; // WARNING x = [-2, 2]

        // Analysis stack:
        // (fn: even_more_neg, ctx: even_more_neg@61, input: 2) = [-4,-4]
        // (fn: more_neg, ctx: even_more_neg@61, more_neg@14, input: 2) = [-3,-3]
        // (fn: neg, ctx: more_neg@14, neg@10, input: 2) = [-2,-2]
        w = even_more_neg(y);

        // Analysis stack:
        // (fn: even_more_neg, ctx: even_more_neg@68, input: -2) = [-4, 0]
        // (fn: more_neg, ctx: even_more_neg@68, more_neg@14, input: -2) = [1, 1]
        // (fn: neg, ctx: more_neg@14, neg@10, input: -2) = [2, 2]
        // join(P, N) = T (because we have seen this context before and its result was N)
        x = even_more_neg(z);

        ignore = array[w]; // ERROR w = [-4, -4]
        ignore = array[x]; // WARNING x = [-4, 0]
    }
    public static boolean condition() { return false; }
}
