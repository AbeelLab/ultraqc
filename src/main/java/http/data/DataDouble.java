package http.data;

import java.util.Objects;

/**
 * A Data class that acts as a Double.
 */
public class DataDouble implements Data<Double> {

    private double d;

    /**
     * Create a new DataDouble.
     * @param d double
     */
    public DataDouble(double d) {
        this.d = d;
    }

    /**
     * Get the double.
     * @return double
     */
    public Double getValue() {
        return d;
    }

    /**
     * Set the value to d.
     * @param d the double.
     */
    public void setValue(Double d) {
        this.d = d;
    }

    /**
     * Hash this object.
     * @return object's hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(d);
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
        DataDouble that = (DataDouble) o;
        return compareTo(that) == 0;
    }

    /**
     * Compare double.
     * @param o the other Data object
     * @return difference of double
     */
    @Override
    public int compareTo(Data<Double> o) {
        return Double.compare(this.d, o.getValue());
    }
}
