import analysis.Analyser;
import chart.data.ChartBuilder;
import http.HttpRequest;
import http.SampleSet;
import http.data.MetricIdentifier;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import tsv.Bcbio;
import tsv.Parser;
import tsv.Qc;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main class of the application.
 *
 * @author Laurens Gerlach: Secretary, Historic Data, Additional Metrics
 * @author Sam Vijlbrief: Project Representative, Metric Developer, Documenter
 * @author Mirco Kroon: Lead Developer, Documenter
 * @author Boris Janssen: Historic Data, Queries
 * @author Jacky Lam: Parsers, Testing
 *
 */
public class Main {
    /**
     * List of metrics to be submitted as data graphs with the number of variables.
     */
    private static final List<MetricIdentifier> METRICS = Arrays.asList(
        new MetricIdentifier("per_sequence_gc_content", 2),
        new MetricIdentifier("per_base_n_content", 2),
        new MetricIdentifier("per_base_sequence_quality", 7),
        new MetricIdentifier("per_sequence_quality_scores", 2),
        new MetricIdentifier("sequence_length_distribution", 2)
    );

    /**
     * Main method.
     */
    public static void main(String[] args) throws IOException {
        CommandLine cmd = parseCmd(args);
        if (cmd == null) { System.exit(1); }

        System.out.println("Reading files...");

        Parser parser = new Parser(Paths.get(cmd.getOptionValue("files")));

        System.out.println("Constructing JSON file...");

        Map<String, Qc> map = parser.getFastQcs();

        LocalDate date = cmd.hasOption("date") ? LocalDate.parse(cmd.getOptionValue("date")) : LocalDate.now();
        String species = cmd.hasOption("species") ? cmd.getOptionValue("species") : "-";
        String seqTech = cmd.hasOption("seq_tech") ? cmd.getOptionValue("seq_tech") : "Default";
        SampleSet sampleSet = new SampleSet(new ChartBuilder().buildDataGraph(map, METRICS), date, species, seqTech);

        System.out.println("Analysing data...");

        Map<String, Bcbio> bioMap = parser.getBcbios();

        new Analyser().analyse(sampleSet, bioMap);

        System.out.println("Submitting data...");

        if (cmd.hasOption("url")) { HttpRequest.submitSamples(sampleSet, cmd.getOptionValue("url")); } else {
            HttpRequest.submitSamples(sampleSet);
        }

        System.out.println("Done!");
    }

    /**
     * Method that parses the given command line arguments to a Commandline object.
     */
    protected static CommandLine parseCmd(String[] args) {
        Options options = new Options();

        Option files = new Option("f", "files", true, "The path to the files to read in");
        files.setRequired(true);
        options.addOption(files);

        Option url = new Option("u", "url", true, "URL of the web server");
        url.setRequired(true);
        options.addOption(url);

        options.addOption(new Option("s", "species", true, "The species of the read in sample"));
        options.addOption(new Option("t", "seq_tech", true, "The sequencing technology used to acquire the data."));
        options.addOption(new Option("d", "date", true, "The date to assign to the samples (YYYY-MM-DD)"));

        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("ultraQC", options);
            return null;
        }
    }
}
