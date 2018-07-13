package analysis;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import chart.data.ChartBuilder;
import http.Metric;
import http.SampleSet;
import http.data.Data;
import http.data.DataDouble;
import http.data.DataInteger;
import http.data.MetricIdentifier;
import jdistlib.disttest.DistributionTest;
import org.junit.jupiter.api.Test;
import tsv.Parser;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyserTest {

    @Test
    void analyseGcTest() {
        double[] data = {2.0, 3.0, 4.0, 6.0};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataInteger(1));
            add(new DataDouble(x));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_WITH_DIPTEST.exec(metric);
        double classification = DistributionTest.diptest(data)[1];

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(classification);
    }

    @Test
    void classifyRegressionTest() {
        List<Data> point1 = Arrays.asList(new DataInteger(1), new DataDouble(2.0));
        List<Data> point2 = Arrays.asList(new DataInteger(2), new DataDouble(4.0));
        List<Data> point3 = Arrays.asList(new DataInteger(3), new DataDouble(6.0));
        List<List<Data>> data = Arrays.asList(point1, point2, point3);

        Metric metric = new Metric(data);
        Analyser.CLASSIFY_WITH_REGRESSION.exec(metric);

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(2.0);
    }

    @Test
    void classifyMaximumTest() {
        double[] data = {2.0, 3.0, 4.0, 6.0};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataInteger(1));
            add(new DataDouble(x));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_WITH_MAX.exec(metric);

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(6.0);
    }

    @Test
    void classifyXOfMaximumTest() {
        double[] data = {2.0, 3.0, 4.0, 6.0};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataInteger((int) (x * 2)));
            add(new DataDouble(x));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_WITH_X_OF_MAX.exec(metric);

        assertThat(metric.getClassification().getValue().intValue()).isEqualTo(12);
    }

    @Test
    void classifyMinimumTest() {
        double[] data = {2.0, 3.0, 4.0, 6.0};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataInteger(1));
            add(new DataDouble(x));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_WITH_MIN.exec(metric);

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(2.0);
    }

    @Test
    void analyseSequenceLengthPass() {
        double[] data = {3.0, 3.0, 3.0, 3.0};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataDouble(x));
            add(new DataInteger(1));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_SEQUENCE_LENGTH.exec(metric);

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(0.0);
    }

    @Test
    void analyseSequenceLengthWarning() {
        int[] data = {2, 3, 4, 6};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataInteger(x));
            add(new DataInteger(1));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_SEQUENCE_LENGTH.exec(metric);

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(1.0);
    }

    @Test
    void analyseSequenceLengthFailure() {
        double[] data = {2.0, 0.0, 4.0, 6.0};
        Metric metric = new Metric(Arrays.stream(data).mapToObj(x -> new ArrayList<Data>() {{
            add(new DataDouble(x));
            add(new DataInteger(1));
        }}).collect(Collectors.toList()));
        Analyser.CLASSIFY_SEQUENCE_LENGTH.exec(metric);

        assertThat(metric.getClassification().getValue().doubleValue()).isEqualTo(-1.0);
    }

    @Test
    void analyser() {
        Analyser analyser = new Analyser();
        assertThat(analyser.getClassifiers().size()).isEqualTo(5);
    }

    @Test
    void analyse() throws IOException {
        Parser parser = new Parser(Paths.get("src/test/resources/parser"));
        Analyser analyser = new Analyser();

        List<MetricIdentifier> METRICS = Arrays.asList(
                new MetricIdentifier("per_sequence_gc_content", 2),
                new MetricIdentifier("per_base_n_content", 2)

                );

        LocalDate date = LocalDate.now();
        String species = "-";
        String seqTech = "Default";
        SampleSet sampleSet = new SampleSet(new ChartBuilder().buildDataGraph(parser.getFastQcs(), METRICS), date, species, seqTech);
        analyser.analyse(sampleSet, parser.getBcbios());
        assertThat(sampleSet.getSamples().get(1).getAvgGcContent()).isEqualTo(1.0);
    }
}
