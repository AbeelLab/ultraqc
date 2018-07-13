package analysis;

import chart.data.IMetricManipulator;
import http.Metric;
import http.SampleSet;
import http.data.Data;
import http.data.DataDouble;
import jdistlib.disttest.DistributionTest;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import tsv.Bcbio;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Analyser {

    // Classifies the metric using the maximum element.
    static final IMetricManipulator CLASSIFY_WITH_MAX = metric ->
            metric.setClassification(doubleStreamY(metric).max(Data::compareTo).orElse(new DataDouble(0.0)));

    // Classifies the metric using X value of the maximum Y
    static final IMetricManipulator CLASSIFY_WITH_X_OF_MAX = metric ->
            metric.setClassification(stream(metric).max(compare(el -> el.get(1)))
                    .orElse(Arrays.asList(new DataDouble(0d))).get(0));


    // Classifies the metric using the minimum element.
    static final IMetricManipulator CLASSIFY_WITH_MIN = metric ->
            metric.setClassification(doubleStreamY(metric).min(Data::compareTo).orElse(new DataDouble(0.0)));

    // Classifies the metric using the diptest (to test for normality of the distribution)
    static final IMetricManipulator CLASSIFY_WITH_DIPTEST = metric ->
            metric.setClassification(new DataDouble(DistributionTest.diptest(
                    doubleStreamY(metric).mapToDouble(el -> el.getValue().doubleValue()).toArray())[1]));

    // Classifies the metric using the slope of a linear regression line through the data.
    static final IMetricManipulator CLASSIFY_WITH_REGRESSION = (Metric metric) -> {
        double[] xs = doubleStreamX(metric).mapToDouble(el -> el.getValue().doubleValue()).toArray();
        double[] ys = doubleStreamY(metric).mapToDouble(el -> el.getValue().doubleValue()).toArray();

        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < xs.length; i++) {
            regression.addData(xs[i], ys[i]);
        }
        metric.setClassification(new DataDouble(regression.getSlope()));
    };

    // Classifies the Sequence length metric with a 1 when not all the data has the same length,
    // and with -1 when there is an element with 0 length.
    static final IMetricManipulator CLASSIFY_SEQUENCE_LENGTH = (Metric metric) -> {
        List<Data> values = doubleStreamX(metric).collect(Collectors.toList());
        if (!values.stream().map(el -> el.getValue()).allMatch(values.get(0).getValue()::equals)) {
            metric.setClassification(new DataDouble(1.0));
        }
        if (values.stream().map(el -> el.getValue()).anyMatch(new DataDouble(0.0).getValue()::equals)) {
            metric.setClassification(new DataDouble(-1.0));
        }
    };

    Map<String, IMetricManipulator> classifiers;

    /**
     * Initializes the analyser class.
     */
    public Analyser() {
        classifiers = new HashMap<>();

        classifiers.put("per_base_n_content", CLASSIFY_WITH_MAX);
        classifiers.put("per_sequence_quality_scores", CLASSIFY_WITH_X_OF_MAX);
        classifiers.put("per_base_sequence_quality", CLASSIFY_WITH_REGRESSION);
        classifiers.put("per_sequence_gc_content", CLASSIFY_WITH_DIPTEST);
        classifiers.put("sequence_length_distribution", CLASSIFY_SEQUENCE_LENGTH);
    }

    /**
     * Shorthand for Comparator.comparing(Class::getterMethod) to reduce line length.
     *
     * @param extractor the Class::getterMethod section, aka the comparison key extractor function.
     * @return the comparator object
     */
    private static Comparator<List<Data>> compare(Function<List<Data>, Data> extractor) {
        return Comparator.comparing(extractor);
    }

    /**
     * Return the given metric as a stream of double values, where the double is their Y value.
     */
    private static Stream<Data> doubleStreamY(Metric metric) {
        return stream(metric).map(el -> el.get(1));
    }

    /**
     * Return the given metric as a stream of Data Lists.
     */
    private static Stream<List<Data>> stream(Metric metric) {
        return metric.getData().stream();
    }

    /**
     * Return the given metric as a stream of double values, where the double is their X value.
     */
    private static Stream<Data> doubleStreamX(Metric metric) {
        return stream(metric).map(el -> el.get(0));
    }

    /**
     * This methods analyses all the samples in the given sampleSet and adds the average GC content means.
     */
    public void analyse(SampleSet sampleSet, Map<String, Bcbio> bioMap) {

        analyseBioMap(sampleSet, bioMap);

        sampleSet.getSamples().parallelStream().forEach(
            sample -> sample.getMetrics().forEach((key, value) -> classifiers.getOrDefault(key, p -> {
            }).exec(value))
        );

        analyseMean(sampleSet);

    }

    /**
     * Analyse the biomap of the sampleset.
     * @param sampleSet the sampleset to be analysed
     * @param bioMap biomap to be analysed
     */

    private void analyseBioMap(SampleSet sampleSet, Map<String, Bcbio> bioMap) {
        sampleSet.getSamples().parallelStream().forEach(el -> {
            if (bioMap.get(el.getSampleName()) != null) {
                el.setCoverage(Integer.parseInt(
                        bioMap.get(el.getSampleName()).getMap().get("Avg_coverage")));
                el.setAlignment(Double.parseDouble(
                        bioMap.get(el.getSampleName()).getMap().get("Mapped_reads_pct")));
                el.setDuplication(Double.parseDouble(
                        bioMap.get(el.getSampleName()).getMap().get("Duplicates_pct")
                ));
            } else {
                el.setCoverage(0);
            }
        });
    }

    /**
     * Analyse the mean of gc content and normalise it.
     * @param sampleSet the sampleSet to be normalised
     */

    private void analyseMean(SampleSet sampleSet) {
        sampleSet.getSamples().forEach(el -> {
            double x = el.getMetric("per_sequence_gc_content").getData().stream().mapToDouble(al ->
                    al.get(0).getValue().doubleValue() * al.get(1).getValue().doubleValue()).sum();
            double y = el.getMetric("per_sequence_gc_content").getData().stream().mapToDouble(al ->
                    al.get(1).getValue().doubleValue()).sum();
            el.setAvgGcContent(x / y);
        });

        sampleSet.getSamples().forEach(el -> {
            el.getMetric("per_sequence_gc_content").getData().stream().forEach(data ->
                    data.get(0).setValue(-el.getAvgGcContent().intValue() + data.get(0).getValue().intValue()));
            el.getMetric("per_sequence_gc_content").getData().removeIf(item ->
                    inBound(item.get(0).getValue().intValue()));
        });
    }

    /**
     * Getter for classifiers.
     * @return the classifiers
     */

    public Map<String, IMetricManipulator> getClassifiers() {
        return classifiers;
    }

    /**
     * Check if the number is between 0 and 100.
     * @param numb the number to be checked
     * @return if it's inbound
     */
    private boolean inBound(int numb) {
        return numb < -50 || numb > 50;
    }

}
