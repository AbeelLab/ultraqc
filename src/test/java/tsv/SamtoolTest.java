package tsv;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SamtoolTest {

    @Test
    public void getListMap() {
        Samtool samtool = new Samtool();
        List data = new ArrayList();
        List<String> row = new ArrayList<>();
        row.add("RN");
        row.add("1");
        data.add(row);
        samtool.addToList("RN", row);
        row.add("RN");
        row.add("2");
        data.add(row);
        samtool.addToList("RN", row);
        assertThat(samtool.getlistMap().get("RN")).isEqualTo(data);
    }

    @Test
    public void addToList() {
        Samtool samtool = new Samtool();
        List data = new ArrayList();
        List<String> row = new ArrayList<>();
        row.add("RN");
        row.add("1");
        data.add(row);
        samtool.addToList("RN", row);
        assertThat(samtool.getlistMap().get("RN").size()).isEqualTo(1);
    }
}
