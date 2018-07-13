package chart.data;

import http.Metric;

/**
 * Interface for different analysing functions in Analyser.
 */
public interface IMetricManipulator {

    void exec(Metric metric);
}
