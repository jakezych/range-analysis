package range_analysis;

import common.Range;
import soot.Local;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * A class to represent abstract values at a program point.
 */
public class Sigma {

    // Maps locals to abstract values
    public Map<Local, Range> map;

    /**
     * An empty sigma
     */
    public Sigma() {
        this.map = new HashMap<>();
    }

    /**
     * An initialized sigma
     * @param locals the locals at this point
     * @param initialVal initial value to use
     */
    public Sigma(Iterable<Local> locals, Range initialVal) {
        this.map = new HashMap<>();
        for (Local l : locals) {
            this.map.put(l, initialVal);
        }
    }

    /**
     * Join for two abstract values
     */
    public static Range join(Range v1, Range v2) {
        return new Range(min(v1.getLow(), v2.getLow()), max(v1.getHigh(), v2.getHigh()));
    }
    /**
     * Returns whether or not the lattice value v1 is at least as precise as the lattice value v2
     *
     * @param v1 left operand lattice value
     * @param v2 right operand lattice value
     * @return true if v1 is at least as precise as v2, false otherwise
     */
    public static boolean isLessThan(Range v1, Range v2) {
        return v2.getLow() <= v1.getLow() && v1.getHigh() <= v2.getHigh();
    }

    public String toString() {
        Set<Local> keys = map.keySet();
        StringBuilder str = new StringBuilder("[ ");
        for (Local key : keys) {
            str.append(key.getName()).append(": ").append(map.get(key)).append("; ");
        }
        return str + " ]";
    }

    public void copy(Sigma dest) {
        dest.map = new HashMap<>(map);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sigma)) {
            return false;
        }

        return map.equals(((Sigma) obj).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}