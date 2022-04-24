package common;

public class Range {
    private int low;
    private int high;

    /**
     * a range from low to high
     */
    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
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
