package common;

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
