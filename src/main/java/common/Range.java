package common;

import javax.lang.model.type.IntersectionType;

public class Range {
    private final int low;
    private final int high;

    /**
     * a range from low to high
     */
    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int getLow() {
        return this.low;
    }


    public int getHigh() {
        return this.high;
    }

    public boolean isBottom() {
        return this.low == Integer.MAX_VALUE && this.high == Integer.MIN_VALUE;
    }

    // TODO: Documentation
    // TODO: also try and clean up the code duplication here
    private static int sentinelAdd(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 + v2;
        }
    }

    private static int sentinelSub(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 - v2;
        }
    }

    private static int sentinelMul(int v1, int v2) {
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (v1 == Integer.MAX_VALUE || v2 == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return v1 * v2;
        }
    }

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
     * @param r1
     * @param r2
     * @param op
     * @return
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
                return new Range(sentinelMul(r1.low, r2.low), sentinelMul(r1.high, r2.high));
            }
            case DIV -> {
                return new Range(sentinelDiv(r1.low, r2.low), sentinelDiv(r1.high, r2.high));
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
}
