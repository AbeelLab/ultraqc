package chart.data;

import http.Metric;
import http.Sample;
import http.data.Data;
import http.data.DataDouble;
import http.data.DataInteger;
import http.data.MetricIdentifier;
import tsv.Qc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to provide methods that convert raw data to various charts.
 */
public final class ChartBuilder {

    /**
     * Metric manipulator that normalizes two dimensional data to a x axis from 0 to 100
     */
    static final IMetricManipulator NORMALIZE_2D = metric -> {
        int highest = metric.getData().stream().mapToInt(el ->
                ((DataInteger) el.get(0)).getValue()).max().orElse(1);

        if (highest == 100) {
            return;
        }
        metric.getData().forEach(el -> {
            int val = ((DataInteger) el.get(0)).getValue();
            el.set(0, new DataInteger((int) (val / (highest / 100.0))));
        });

        Map<Data, List<List<Data>>> map = metric.getData().stream().collect(Collectors.groupingBy(el -> el.get(0)));
        List<List<Data>> newData = map.entrySet().stream().map(el ->
                Arrays.asList(el.getKey(), new DataDouble(el.getValue().stream().mapToDouble(x ->
                        x.get(1).getValue().doubleValue()
                ).max().orElse(0.0)))
        ).collect(Collectors.toList());
        metric.setData(newData);
    };

    HashMap<String, IMetricManipulator> modifiers;

    /**
     * Constructor of ChartBuilder.
     */
    public ChartBuilder() {
        this.modifiers = new HashMap<>();

        modifiers.put("per_sequence_gc_content", metric -> {
            double totalY = metric.getData().stream().mapToDouble(el -> ((DataDouble) el.get(1)).getValue()).sum();

            metric.getData().forEach(el -> {
                double val = ((DataDouble) el.get(1)).getValue();
                el.add(1, new DataDouble(val / totalY * 100));
            });
        });
        modifiers.put("sequence_length_distribution", NORMALIZE_2D);
    }

    /**
     * A method that creates line charts for simple 2d data without modifying it.
     * @param metrics The paths of the corresponding tsv files
     */
    public List<Sample> buildDataGraph(Map<String, Qc> map, List<MetricIdentifier> metrics) {
        return map.entrySet().parallelStream()
            .map(el -> {
                Sample sample = new Sample(el.getKey());

                for (MetricIdentifier db : metrics) {
                    String path = db.getName();
                    Metric metric = createMetric(db, el);
                    sample.addMetric(path, metric);
                }
                return sample;
            })
            .collect(Collectors.toList());
    }

    /**
     * Create data sample from string.
     * @param row the string to be sampled
     * @param amount amount of data
     * @return the data sample
     */
    private List<Data> createDataSample(String[] row, int amount) {
        List<Data> dataSample = new ArrayList<>();
        dataSample.add(new DataInteger(Integer.parseInt(row[0])));
        dataSample.add(new DataDouble(Double.parseDouble(row[1])));
        for (int a = 2; a < amount; a++) {
            dataSample.add(new DataDouble(Double.parseDouble(row[a])));
        }
        return dataSample;
    }

    /**
     * Create a metric from data.
     * @param db the database of metric identifiers
     * @param el get data from element
     * @return the metric
     */
    private Metric createMetric(MetricIdentifier db, Map.Entry<String, Qc> el) {
        String path = db.getName();
        int amount = db.getVars();
        List<List<Data>> data;
        if (el.getValue().getTsvMap().containsKey(path)) {
            String[][] contents = el.getValue().getTsvMap().get(path).getData();
            data = Arrays.stream(contents)
                    .map(row -> createDataSample(row, amount))
                    .collect(Collectors.toList());
        } else {
            data = new ArrayList<>();
        }
        Metric metric = new Metric(data);
        if (modifiers.containsKey(path)) {
            modifiers.get(path).exec(metric);
        }

        return metric;
    }
}
