package chart.data;

import static org.assertj.core.api.Assertions.assertThat;

import http.Metric;
import http.Sample;
import http.SampleSet;
import http.data.Data;
import http.data.DataDouble;
import http.data.DataInteger;
import http.data.MetricIdentifier;
import org.junit.jupiter.api.Test;
import tsv.Qc;
import tsv.Tsv;

import java.time.LocalDate;
import java.util.*;

public class ChartBuilderTest {

    private DataDouble dd = new DataDouble(8.0);
    private DataDouble dd2 = new DataDouble(7.0);
    private DataDouble dd3 = new DataDouble(8.0);
    private DataInteger di = new DataInteger(1);
    private DataInteger di2 = new DataInteger(2);
    private DataInteger di3 = new DataInteger(1);

    @Test
    void testSimpleLineGraph() {
        List<Sample> samples = new ArrayList<>();
        samples.add(new Sample("x").addMetric("y", new Metric(new ArrayList<>())));
        SampleSet sampleSet = new SampleSet(samples, LocalDate.now(), "z", "s");
        assertThat(sampleSet.getSamples()).isInstanceOf(List.class);
        assertThat(samples.get(0).getSampleName()).isEqualTo("x");
        assertThat(samples.get(0).getMetrics()).isInstanceOf(Map.class);
        assertThat(samples.get(0).getMetric("y")).isInstanceOf(Metric.class);
    }

    @Test
    void dataEqualsTest(){
        assertThat(dd).isEqualTo(dd3);
        assertThat(dd).isNotEqualTo(dd2);
        assertThat(di).isEqualTo(di3);
        assertThat(di).isNotEqualTo(di2);
    }

    @Test
    void dataHashcodeTest(){
        assertThat(dd.hashCode()).isEqualTo(dd3.hashCode());
        assertThat(dd.hashCode()).isNotEqualTo(dd2.hashCode());
        assertThat(di.hashCode()).isEqualTo(di3.hashCode());
        assertThat(di.hashCode()).isNotEqualTo(di2.hashCode());
    }

    @Test
    void emptyGraph() {
        assertThat(new ChartBuilder().buildDataGraph(new HashMap<>(), new ArrayList<>()))
            .isInstanceOf(List.class);
    }

    @Test
    void singleMetricTest() {
        List<MetricIdentifier> paths = new ArrayList<>();
        paths.add(new MetricIdentifier("per_sequence_gc_content", 2));
        SampleSet sampleSet = new SampleSet(
            new ChartBuilder().buildDataGraph(getQcMap(1), paths),
            LocalDate.of(1, 1, 1),
            "broccoli",
            "USB"
        );
        String json = sampleSet.toJson();
        assertThat(json).isEqualTo(
            "{\"samples\":[{\"alignment\":0,\"coverage\":0,\"duplication\":0,\"sampleName\":\"x\",\"metrics\":{\"per_sequence_gc_content\":{\"data\":[[10,28.57,20],[10,71.43,50]],\"classification\":0}},\"avgGcContent\":0}],\"date\":\"0001-01-01\",\"species\":\"broccoli\",\"seqTech\":\"USB\"}");
    }

    private Map<String, Qc> getQcMap(double multiply) {
        List<String> data = new ArrayList<>();
        data.add("10\t" + 20 * multiply + "\t30");
        data.add("10\t" + 50 * multiply + "\t30");

        Map<String, Tsv> innerMap = new HashMap<>();
        innerMap.put("per_sequence_gc_content", new Tsv("a\tb\tc", data));
        innerMap.put("per_base_n_content", new Tsv("a\tb\tc", data));
        innerMap.put("per_sequence_quality_score", new Tsv("a\tb\tc\td", data));


        Map<String, Qc> map = new HashMap<>();
        map.put("x", new Qc(innerMap));

        return map;
    }

    @Test
    void singleMetricTripleVariableTest() {
        List<MetricIdentifier> paths = new ArrayList<>();
        paths.add(new MetricIdentifier("per_sequence_quality_score", 3));
        SampleSet sampleSet = new SampleSet(
            new ChartBuilder().buildDataGraph(getQcMap(1), paths),
            LocalDate.of(1, 1, 1),
            "broccoli",
            "Manual"
        );
        String json = sampleSet.toJson();
        assertThat(json).isEqualTo(
            "{\"samples\":[{\"alignment\":0,\"coverage\":0,\"duplication\":0,\"sampleName\":\"x\",\"metrics\":{\"per_sequence_quality_score\":{\"data\":[[10,20,30],[10,50,30]],\"classification\":0}},\"avgGcContent\":0}],\"date\":\"0001-01-01\",\"species\":\"broccoli\",\"seqTech\":\"Manual\"}");
    }

    @Test
    void doubleMetricTest() {
        List<MetricIdentifier> paths = new ArrayList<>();
        paths.add(new MetricIdentifier("per_sequence_gc_content", 2));
        paths.add(new MetricIdentifier("per_base_n_content", 2));
        SampleSet sampleSet = new SampleSet(
            new ChartBuilder().buildDataGraph(getQcMap(1), paths),
            LocalDate.of(1, 1, 1),
            "broccoli",
            "USB"
        );
        String json = sampleSet.toJson();
        assertThat(json).isEqualTo(
            "{\"samples\":[{\"alignment\":0,\"coverage\":0,\"duplication\":0,\"sampleName\":\"x\",\"metrics\":{\"per_base_n_content\":{\"data\":[[10,20],[10,50]],\"classification\":0},\"per_sequence_gc_content\":{\"data\":[[10,28.57,20],[10,71.43,50]],\"classification\":0}},\"avgGcContent\":0}],\"date\":\"0001-01-01\",\"species\":\"broccoli\",\"seqTech\":\"USB\"}");
    }

    @Test
    void normalizeTest() {
        List<List<Data>> data = Arrays.asList(Arrays.asList(new DataInteger(100), new DataDouble(42)), Arrays.asList(new DataInteger(200), new DataDouble(69)));
        Metric metric = new Metric(data);

        ChartBuilder.NORMALIZE_2D.exec(metric);
        List<List<Data>> newData = Arrays.asList(Arrays.asList(new DataInteger(50), new DataDouble(42)), Arrays.asList(new DataInteger(100), new DataDouble(69)));
        assertThat(metric.getData()).isEqualTo(newData);
    }

    @Test
    void normalizeNormalizedTest() {
        List<List<Data>> data = Arrays.asList(Arrays.asList(new DataInteger(50), new DataDouble(42)), Arrays.asList(new DataInteger(100), new DataDouble(69)));
        Metric metric = new Metric(data);

        ChartBuilder.NORMALIZE_2D.exec(metric);
        List<List<Data>> sameData = Arrays.asList(Arrays.asList(new DataInteger(50), new DataDouble(42)), Arrays.asList(new DataInteger(100), new DataDouble(69)));
        assertThat(metric.getData()).isEqualTo(sameData);
    }
}
