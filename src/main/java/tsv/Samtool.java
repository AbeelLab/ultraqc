package tsv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The samtool item.
 * The samtool class is the parsed sample filled with tsv.
 */
public class Samtool {

    private Map<String, List> listMap;

    /**
     * Create a new Samtool.
     */
    public Samtool() {
        this.listMap = new HashMap<>();
    }

    /**
     * Adds a row to the list.
     * @param key the key where the row should be added
     * @param row the row that should be added
     */
    public void addToList(String key, List row) {
        List rows;
        if (listMap.containsKey(key)) {
            rows = listMap.get(key);
        } else {
            rows = new ArrayList<>();
        }
        rows.add(row);
        listMap.put(key, rows);
    }

    /**
     * Get the list map.
     * @return listMap
     */
    public Map<String, List> getlistMap() {
        return listMap;
    }
}
