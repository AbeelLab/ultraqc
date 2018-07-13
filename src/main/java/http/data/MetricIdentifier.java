package http.data;

/**
 * A class that holds the name and number of variables for a certain metric.
 */
public class MetricIdentifier {

    private String name;
    private int vars;
    /**
     * Create a new metric identifier.
     * @param n name of metric
     * @param v number of variables
     */
    public MetricIdentifier(String n, int v) {
        this.name = n;
        this.vars = v;
    }

    /**
     * Get the name of metric.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the amount of variables.
     * @return the amount of variables
     */
    public int getVars() {
        return vars;
    }
}
