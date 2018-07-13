package tsv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Parse the whole folder to a map.
 * The map is filled with the all the samples.
 */
public class FastQcParser {
    private String name;
    private Qc qc;

    /**
     * Create a parsed fastqc tsv.
     * @param data the file to be parsed
     */
    public FastQcParser(File data) {

        this.qc = new Qc(createTsvMap(data));
    }

    /**
     * Creates a map with all the TSV data.
     * @param data the item to create data
     * @return map with data
     */
    private Map<String, Tsv> createTsvMap(File data) {

        Queue<String> lines = getLines(data.toPath());

        if (lines.isEmpty()) {
            throw new FastQcParserException("Error no test found", new IOException("No test found"));
        }

        Map<String, Tsv> tsvMap = new HashMap<>();

        while (!lines.isEmpty()) {
            String line = lines.poll();
            if (line.charAt(0) == '#') {
                continue;
            } else {
                String header = getHeader(lines);
                if (header == null) {
                    tsvMap.put(getTestName(line), null);
                } else {
                    tsvMap.put(getTestName(line), new Tsv(header.substring(1), getData(lines)));
                }
            }
        }

        return tsvMap;
    }

    /**
     * Loads a file into a stream of lines.
     */
    static Queue<String> getLines(Path path) {
        try {
            return new LinkedList<>(Files.lines(path).collect(Collectors.toList()));
        } catch (NullPointerException | IOException ex) {
            throw new FastQcParserException("Error reading data file", new FileNotFoundException(path.toString()));
        }
    }

    /**
     * Get the header of the data.
     * @param lines the data
     * @return the header of the data
     */
    private String getHeader(Queue<String> lines) {
        String header = lines.poll();

        if (header == null) {
            throw new FastQcParserException("Error no data found", new IOException("No data found"));
        }

        if (header.equals(">>END_MODULE")) {
            return null;
        }

        while (lines.peek().charAt(0) == '#') {
            header = lines.poll();
        }

        if (header.charAt(0) != '#') {
            throw new FastQcParserException("Error header not found", new IOException("Header not found"));
        }

        return header;
    }

    /**
     * Change the name to make it easier to find.
     * @param name test file name
     * @return changed name
     */
    private String getTestName(String name) {
        return name.split("\t")[0].replaceAll(" ", "_").toLowerCase().substring(2);
    }

    /**
     * Get data of file.
     * @param lines the lines of the file
     * @return the data
     */
    private List<String> getData(Queue<String> lines) {
        ArrayList<String> data = new ArrayList<>();
        boolean foundEnd = false;

        while (!lines.isEmpty()) {
            String line = lines.poll();
            if (line.equals(">>END_MODULE")) {
                foundEnd = true;
                break;
            } else {
                data.addAll(createDate(line));
            }
        }

        if (!foundEnd) {
            throw new FastQcParserException("Error end of data not found", new IOException("End of data not found"));
        }

        return data;
    }

    /**
     * Parse a single line.
     * @param line the line that needs to be parsed
     * @return the parsed data
     */
    private List<String> createDate(String line) {
        ArrayList<String> data = new ArrayList<>();
        String[] splitLine = line.split("\t");
        String readNumb = splitLine[0];

        if (readNumb.toLowerCase().contains("filename")) {
            this.name = splitLine[1];
        }

        if (readNumb.contains("-")) {
            String[] split = readNumb.split("-");
            int from = Integer.parseInt(split[0]);
            int to = Integer.parseInt(split[1]);

            for (int i = from; i <= to; i++) {
                data.add(i + "\t" + line.replace(readNumb, "").substring(1));
            }
        } else {
            data.add(line);
        }

        return data;
    }

    /**
     * Get the name of the sample.
     * @return the name of the sample
     */
    public String getName() {
        return name;
    }

    /**
     * Get the qc data.
     * @return the qc data
     */
    public Qc getQc() {
        return qc;
    }
}
