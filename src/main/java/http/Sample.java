package http;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent a sample along with metrics in pair form.
 */
public class Sample {

    double alignment;
    int coverage;
    double duplication;
    private String sampleName;
    private Map<String, Metric> metrics;
    private double avgGcContent;

    public Sample(String sampleName) {
        this.sampleName = sampleName;
        this.metrics = new HashMap<>();
    }

    /**
     * Fills the metrics variable with metrics.
     */
    public Sample addMetric(String key, Metric metric) {
        metrics.put(key, metric);
        return this;
    }

    /**
     * Gets the metric corresponding to key from the metrics map.
     */
    public Metric getMetric(String key) {
        return metrics.get(key);
    }

    /**
     * Get the name of sample.
     * @return the name of sample
     */
    public String getSampleName() {
        return sampleName;
    }

    /**
     * Get the avgGcContent.
     * @return the avgGcContent
     */

    public Double getAvgGcContent() {
        return avgGcContent;
    }

    /**
     * Set the avgGcContent.
     * @param mean the mean for avgGcContent
     */

    public void setAvgGcContent(Double mean) {
        this.avgGcContent = mean;
    }

    /**
     * Get the metrics.
     * @return the metrics
     */
    public Map<String, Metric> getMetrics() {
        return metrics;
    }

    /**
     * Get the alignment.
     * @param alignment the alignment
     */
    public void setAlignment(double alignment) {
        this.alignment = alignment;
    }
    /**
     * Set the coverage.
     * @param coverage the coverage
     */
    public void setCoverage(int coverage) {
        this.coverage = coverage;
    }

    /**
     * Get the duplication.
     * @param duplication the duplication
     */
    public void setDuplication(double duplication) {
        this.duplication = duplication;
    }
}
