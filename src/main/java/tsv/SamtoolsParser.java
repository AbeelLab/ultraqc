package tsv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parse the whole folder to a map.
 * The map is filled with the samples.
 */
public class SamtoolsParser {
    private String name;
    private Samtool samtool;

    /**
     * Create a parsed samtool.
     * @param data the file to be parsed
     */
    public SamtoolsParser(File data) {

        this.name = getFileName(data);
        this.samtool = new Samtool();
        createSamtool(samtool, data);
    }

    /**
     * Get the file name without extension.
     * @param file file to be read
     * @return the file name without extension
     */
    private String getFileName(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf(".txt"));
    }

    /**
     * Fill the samtool with data.
     */
    private void createSamtool(Samtool samtool, File data) {

        Queue<String> lines = getLines(data.toPath());

        if (lines.isEmpty()) {
            throw new SamtoolsParserException("Error no test found", new IOException("No test found"));
        }

        String key = null;
        List<List> row = new ArrayList<>();

        while (!lines.isEmpty()) {
            String line = lines.poll();
            if (line.contains("Use `grep")) {

                if (key != null) {
                    samtool.addToList(key, row);
                }

                String newKey = findKey(line);
                if (newKey != null) {
                    key = newKey;
                    row = new ArrayList<>();
                }

            }
            
            if (line.charAt(0) == '#') {
                continue;
            } else {
                row.add(createListFromLine(line));
            }
        }

        if (key == null) {
            throw new SamtoolsParserException("Error no test found", new IOException("No test found"));
        }

        samtool.addToList(key, row);
    }

    /**
     * Use regex to find the key.
     * @param line the line to be read
     * @return the parsed key
     */
    private String findKey(String line) {
        Pattern regex = Pattern.compile("(grep\\s\\^)(.+)(\\s\\|)");
        Matcher m = regex.matcher(line);
        if (m.find()) {
            return m.group(2);
        }

        return null;
    }

    /**
     * Loads a file into a stream of lines.
     */
    static Queue<String> getLines(Path path) {
        try {
            return new LinkedList<>(Files.lines(path).collect(Collectors.toList()));
        } catch (NullPointerException | IOException ex) {
            throw new SamtoolsParserException("Error reading data file", new FileNotFoundException(path.toString()));
        }
    }

    /**
     * Create an arraylist from a tsv line.
     * @param line string to be parsed
     * @return parsed list from string
     */
    private List<String> createListFromLine(String line) {
        return new ArrayList<>(Arrays.asList(line.split("\t")));
    }

    /**
     * Get the name of the sample.
     * @return the name of the sample
     */
    public String getName() {
        return name;
    }

    /**
     * Get the samtool data.
     * @return the samtool data
     */
    public Samtool getSamtool() {
        return samtool;
    }
}
