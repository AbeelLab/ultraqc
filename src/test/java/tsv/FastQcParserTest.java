package tsv;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FastQcParserTest {
    private Tsv tsv1;
    private Tsv tsv2;
    private Tsv tsv3;

    @BeforeEach
    public void setup() {
        String column = "col1\tcol2";
        List<String> data = new ArrayList<>();
        data.add("420\t1337");

        List<String> data2 = new ArrayList<>();
        data2.add("1\t1337");
        data2.add("2\t1337");

        tsv1 = new Tsv(column, data);
        tsv2 = new Tsv(column, new ArrayList<>());
        tsv3 = new Tsv(column, data2);
    }

    @Test
    public void createNewParser1() {
        FastQcParser parser = new FastQcParser(new File("src/test/resources/qc/test1/fastqc/fastqc_data.txt"));
        String[][] data = parser.getQc().getTsvMap().get("test_content").getData();

        assertThat(data).isEqualTo(tsv1.getData());
    }

    @Test
    public void createNewParser2() {
        FastQcParser parser = new FastQcParser(new File("src/test/resources/qc/test2/fastqc/fastqc_data.txt"));
        String[][] data = parser.getQc().getTsvMap().get("test_content").getData();

        assertThat(data).isEqualTo(tsv2.getData());
    }

    @Test
    public void createNewParser3() {
        FastQcParser parser = new FastQcParser(new File("src/test/resources/qc/test3/fastqc/fastqc_data.txt"));
        String[][] data = parser.getQc().getTsvMap().get("test_content").getData();

        assertThat(data).isEqualTo(tsv3.getData());
    }

    @Test
    public void createNewParser4() {
        FastQcParser parser = new FastQcParser(new File("src/test/resources/qc/test4/fastqc/fastqc_data.txt"));
        String name = parser.getName();

        assertThat(name).isEqualTo("test4");
    }

    @Test
    public void createNewParser5() {
        FastQcParser parser = new FastQcParser(new File("src/test/resources/qc/test5/fastqc/fastqc_data.txt"));
        String[][] data = parser.getQc().getTsvMap().get("test_content").getData();
        String[][] empty = new String[0][0];

        assertThat(data).isEqualTo(empty);
    }

    @Test
    public void createNewParser6() {
        FastQcParser parser = new FastQcParser(new File("src/test/resources/qc/test6/fastqc/fastqc_data.txt"));
        String[][] data = parser.getQc().getTsvMap().get("test_content").getData();

        assertThat(data).isEqualTo(tsv1.getData());
    }

    @Test
    public void invalidParserEmptyHeader() {

        assertThatThrownBy(() -> new FastQcParser(new File("src/test/resources/invalidqc/qc/test1234/fastqc/fastqc_data.txt")))
            .isInstanceOf(FastQcParserException.class)
            .hasMessageContaining("header not found")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void invalidParserNoHeader() {

        assertThatThrownBy(() -> new FastQcParser(new File("src/test/resources/invalidqc3/qc/test1234/fastqc/fastqc_data.txt")))
            .isInstanceOf(FastQcParserException.class)
            .hasMessageContaining("no data found")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void invalidParserEmptyFile() {

        assertThatThrownBy(() -> new FastQcParser(new File("src/test/resources/invalidqc4/qc/test1234/fastqc/fastqc_data.txt")))
            .isInstanceOf(FastQcParserException.class)
            .hasMessageContaining("no test found")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void invalidParserNoEndOfData() {

        assertThatThrownBy(() -> new FastQcParser(new File("src/test/resources/invalidqc5/qc/test1234/fastqc/fastqc_data.txt")))
            .isInstanceOf(FastQcParserException.class)
            .hasMessageContaining("end of data not found")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void FileNotFound() {

        Path path = new File("NonExistingFile.txt").toPath();
        assertThatThrownBy(() -> FastQcParser.getLines(path))
            .isInstanceOf(FastQcParserException.class)
            .hasMessageContaining("reading data")
            .hasCauseInstanceOf(IOException.class);
    }

}
