package common;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class Range {
    private final int low;
    private final int high;

    /**
     * a range from low to high [low, high]
     */
    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int getLow() { return this.low; }

    public int getHigh() { return this.high; }

    public boolean isBottom() { return this.low == Integer.MAX_VALUE && this.high == Integer.MIN_VALUE; }

    /**
     * Adds together the two input parameters. If either is infinity or negative infinity, return
     * infinity or negative infinity. Otherwise, sum them normally.
     * @param v1 first value
     * @param v2 second value
     * @return the sum of v1 and v2 accounting for infinity and negative infinity
     */
    private static int sentinelAdd(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 + v2;
        }
    }

    /**
     * subtracts the two input parameters. If either is infinity or negative infinity, return
     * infinity or negative infinity. Otherwise, subtract them normally.
     * @param v1 first value
     * @param v2 second value
     * @return the difference of v1 and v2 accounting for infinity and negative infinity
     */
    private static int sentinelSub(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 - v2;
        }
    }

    /**
     * multiplies the two input parameters. If either is infinity or negative infinity, return
     * infinity or negative infinity. Otherwise, multiply them normally.
     * @param v1 first value
     * @param v2 second value
     * @return the product of v1 and v2 accounting for infinity and negative infinity
     */
    private static int sentinelMul(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 * v2;
        }
    }

    /**
     * divides the two input parameters. If either is infinity or negative infinity, return
     * infinity or negative infinity. Otherwise, divide them normally.
     * @param v1 first value
     * @param v2 second value
     * @return the quotient of v1 and v2 accounting for infinity and negative infinity
     */
    private static int sentinelDiv(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 / v2;
        }
    }

    /**
     *
     * combines the two ranges using the specified operator which is one of the four following operations:
     * addition, subtraction, multiplication, division
     * @param r1 a range
     * @param r2 a range
     * @param op an operator denoting how the two input ranges should be combined.
     * @return the combination of the two ranges using the inputted operator
     */
    public static Range combine(Range r1, Range r2, Operator op) {
        switch (op) {
            case ADD -> {
                return new Range(sentinelAdd(r1.low, r2.low), sentinelAdd(r1.high, r2.high));
            }
            case SUB -> {
                return new Range(sentinelSub(r1.low, r2.low), sentinelSub(r1.high, r2.high));
            }
            case MUL -> {
                // Multiplying by negative flips the range
                int low = sentinelMul(r1.low, r2.low);
                int high = sentinelMul(r1.high, r2.high);
                return new Range(min(low, high), max(low, high));
            }
            case DIV -> {
                // Dividing by negative flips the range
                int low = sentinelDiv(r1.low, r2.low);
                int high = sentinelDiv(r1.high, r2.high);
                return new Range(min(low, high), max(low, high));
            }
        }
        // return Top
        return new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Range)) {
            return false;
        } else {
            return this.low == ((Range) obj).low && this.high == ((Range) obj).high;
        }
    }

    @Override
    public String toString() {
        return "[" + this.low + ", " + this.high + "]";
    }
}
