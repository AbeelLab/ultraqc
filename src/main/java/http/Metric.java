package http;

import http.data.Data;
import http.data.DataDouble;

import java.util.List;

/**
 * A Metric class that holds the data of a certain metrics and a classification.
 */
public class Metric {

    private List<List<Data>> data;
    private Data classification;

    /**
     * Create a new metric.
     * @param data data of metric
     */
    public Metric(List<List<Data>> data) {
        this.data = data;
        classification = new DataDouble(0.0);
    }

    /**
     * Get the data of metric.
     * @return the data
     */
    public List<List<Data>> getData() {
        return data;
    }

    /**
     * Get the classification of metric.
     * @return the classification
     */
    public Data getClassification() {
        return classification;
    }

    /**
     * Set a new classification.
     * @param classification the classification
     */
    public void setClassification(Data classification) {
        this.classification = classification;
    }

    /**
     * Set a new dataSet.
     * @param data the data
     */
    public void setData(List<List<Data>> data) {
        this.data = data;
    }
}
