package tsv;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TsvTest {

    private Tsv tsv;

    @BeforeEach
    public void setup() {
        String column = "col1\tcol2";
        List<String> data = new ArrayList<>();
        int size = 2;
        for (int i = 0; i < size; i++) {
            data.add("test: " + 2 * i + "\ttest: " + (2 * i + 1));
        }
        tsv = new Tsv(column, data);
    }

    @Test
    public void getColumn() {
        List<String> col = new ArrayList<>();
        col.add("col1");
        col.add("col2");

        assertThat(tsv.getHeader()).isEqualTo(col);
    }

    @Test
    public void getData() {
        int size = 2;
        String[][] data = new String[size][size];
        for (int i = 0; i < size; i++) {
            for (int k = 0; k < size; k++) {
                data[k][i] = "test: " + (2 * k + i);
            }
        }

        assertThat(tsv.getData()).isEqualTo(data);
    }
}
