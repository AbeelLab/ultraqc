package http.data;

import java.util.Objects;

/**
 * A Data class that acts as an Integer.
 */
public class DataInteger implements Data<Integer> {

    private int i;

    /**
     * Create a new DataInteger.
     * @param i int
     */
    public DataInteger(int i) {
        this.i = i;
    }

    /**
     * Get int.
     * @return int
     */
    public Integer getValue() {
        return i;
    }

    /**
     * Set int.
     * @param i int
     */
    public void setValue(Integer i) {
        this.i = i;
    }

    /**
     * Hash this object.
     * @return object's hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(i);
    }

    /**
     * Equals method.
     * @param o the other object
     * @return if he objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        DataInteger that = (DataInteger) o;
        return compareTo(that) == 0;
    }

    /**
     * Compare int.
     * @param o the other Data object
     * @return difference of int
     */
    @Override
    public int compareTo(Data<Integer> o) {
        return Integer.compare(this.i, o.getValue());
    }
}
