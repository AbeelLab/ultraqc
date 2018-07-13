package tsv;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The parser for the txt files.
 */
public class Parser {
    private Map<String, Qc> fastQcs;
    private Map<String, Samtool> samtools;
    private Map<String, Bcbio> bcbios;

    /**
     * Check all files and save the files we need.
     * @param folder where to check the files
     * @throws IOException if folder is not found
     */
    public Parser(Path folder) throws IOException {
        this.fastQcs = new HashMap<>();
        this.samtools = new HashMap<>();
        this.bcbios = new HashMap<>();

        Files.walk(folder)
            .filter(p -> p.toString().endsWith(".txt"))
            .distinct()
            .forEach(path -> {
                try {
                    insertData(path);
                } catch (IOException e) {
                    System.out.println("WARNING: empty file");
                }
            });
    }

    /**
     * Check file and save the data.
     * @param path the path of the file
     * @throws IOException if the file is empty
     */
    private void insertData(Path path) throws IOException {
        File file = new File(String.valueOf(path));
        if (path.toString().contains("fastqc_data")) {
            FastQcParser fastQcParser = new FastQcParser(file);
            this.fastQcs.put(fastQcParser.getName(), fastQcParser.getQc());
        } else if (path.toString().contains("bcbio")) {
            BcbioParser bcbioParser = new BcbioParser(file);
            this.bcbios.put(bcbioParser.getName(), bcbioParser.getBcbio());
        } else if (Files.lines(path).findFirst().toString().contains("samtools")) {
            SamtoolsParser samtoolsParser = new SamtoolsParser(file);
            this.samtools.put(samtoolsParser.getName(), samtoolsParser.getSamtool());
        }
    }

    /**
     * Get all the parsed FastQc data.
     * @return all parsed FastQc data.
     */
    public Map<String, Qc> getFastQcs() {
        return fastQcs;
    }

    /**
     * Get all the parsed samtools data.
     * @return all the parsed samtools data
     */
    public Map<String, Samtool> getSamtools() {
        return samtools;
    }

    /**
     * Get all the parsed bcbios data.
     * @return all the parsed bcbios data
     */
    public Map<String, Bcbio> getBcbios() {
        return bcbios;
    }
}
