package tsv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Parse the whole folder to a map.
 * The map is filled with the all the samples.
 */
public class BcbioParser {
    private String name;
    private Bcbio bcbio;

    /**
     * Create a parsed bcbio.
     * @param data the file to be parsed
     */
    public BcbioParser(File data) {

        this.name = getFileName(data);
        this.bcbio = createBcBio(data);
    }

    /**
     * Get the file name without extension.
     * @param file file to be read
     * @return the file name without extension
     */
    private String getFileName(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf("_bcbio"));
    }

    /**
     * Read data and create the Bcbio.
     * @param data the data to be parsed
     * @return the Bcbio of the data
     */
    private Bcbio createBcBio(File data) {

        Queue<String> lines = getLines(data.toPath());

        if (lines.isEmpty()) {
            throw new BcbioParserException("Error no information found", new IOException("No information found"));
        }

        Bcbio bcbio = new Bcbio();

        while (!lines.isEmpty()) {
            String line = lines.poll();
            if (line.charAt(0) == '%') {
                continue;
            } else {
                String[] split = line.split("\t");
                bcbio.addInfo(split[0], split[1]);
            }
        }

        return bcbio;
    }

    /**
     * Loads a file into a stream of lines.
     */
    static Queue<String> getLines(Path path) {
        try {
            return new LinkedList<>(Files.lines(path).collect(Collectors.toList()));
        } catch (NullPointerException | IOException ex) {
            throw new BcbioParserException("Error reading data file", new FileNotFoundException(path.toString()));
        }
    }

    /**
     * Get the name of the sample.
     * @return the name of the sample
     */
    public String getName() {
        return name;
    }

    /**
     * Get the bcbio data.
     * @return the bcbio data
     */
    public Bcbio getBcbio() {
        return bcbio;
    }
}
