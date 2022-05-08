package inputs;

public class FibonacciTest {

    public static int fibonacci(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * fibonacci(n - 1);
        }
    }

    public static void main(String[] args) {
        int x, y, z, w;
        int[] array = new int[5];
        int ignore;
        x = 3;
        // Analysis stack:
        // (fn: fibonacci, ctx: fib@20 fib@20, input: [3, 3]) = [-inf, inf]
        if (fibonacci(x) > 0) {
            y = 3;
            z = -1;
        } else {
            y = -3;
            z = 1;
        }

        ignore = array[y]; // WARNING y = [-3, 3]
        ignore = array[z]; // WARNING z = [-1, 1]

    }
}