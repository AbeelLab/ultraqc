package tsv;

import java.util.Map;

/**
 * The Qc item.
 * The qc class is the parsed sample filled with tsv.
 */
public class Qc {

    private Map<String, Tsv> tsvMap;

    /**
     * Create a new Qc.
     * @param tsvMap the TSV data.
     */
    public Qc(Map<String, Tsv> tsvMap) {
        this.tsvMap = tsvMap;
    }

    /**
     * Get the TSV map.
     * @return TsvMap
     */
    public Map<String, Tsv> getTsvMap() {
        return tsvMap;
    }
}
