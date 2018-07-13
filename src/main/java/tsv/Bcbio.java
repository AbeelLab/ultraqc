package tsv;

import java.util.HashMap;
import java.util.Map;

/**
 * The Bcbio item.
 * Stores the information of biotools.
 */
public class Bcbio {

    private Map<String, String> map;

    /**
     * Create a new Bcbio.
     */
    public Bcbio() {
        this.map = new HashMap<>();
    }

    /**
     * Add new information to the bcbio.
     * @param key   key of info
     * @param value value of info
     */
    public void addInfo(String key, String value) {
        map.put(key, value);
    }

    /**
     * Get the map.
     * @return the map.
     */
    public Map<String, String> getMap() {
        return map;
    }
}
