package tsv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The TSV item.
 * It is the class for the parsed tsv file.
 */
public class Tsv {
    private List<String> header;
    private String[][] data;

    /**
     * create a new TSV.
     * @param header the header of the tsv
     * @param data   the data of tsv
     */
    public Tsv(String header, List<String> data) {
        this.header = new ArrayList<>(Arrays.asList(header.split("\t")));
        this.data = new String[data.size()][this.header.size()];
        for (int i = 0; i < data.size(); i++) {
            String[] line = data.get(i).split("\t");
            for (int j = 0; j < line.length; j++) {
                this.data[i][j] = line[j];
            }
        }
    }

    /**
     * Get the header of TSV.
     * @return header
     */
    public List<String> getHeader() {
        return header;
    }

    /**
     * Get the data of TSV.
     * @return data
     */
    public String[][] getData() {
        return data.clone();
    }
}
