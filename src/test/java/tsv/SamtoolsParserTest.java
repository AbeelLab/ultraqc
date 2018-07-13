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

public class SamtoolsParserTest {

    private Samtool samtool;

    @BeforeEach
    public void setup() {
        List<List> data = new ArrayList<>();
        List<String> row = new ArrayList<>();
        row.add("RN");
        row.add("1");
        data.add(row);
        row = new ArrayList<>();
        row.add("RN");
        row.add("6");
        data.add(row);
        samtool = new Samtool();
        samtool.addToList("RN", data);
    }

    @Test
    public void createNewParser() {
        SamtoolsParser parser = new SamtoolsParser(new File("src/test/resources/qc/test1/samtools/test1.txt"));
        List data = parser.getSamtool().getlistMap().get("RN");
        assertThat(data).isEqualTo(samtool.getlistMap().get("RN"));
    }

    @Test
    public void createNewParser2() {
        SamtoolsParser parser = new SamtoolsParser(new File("src/test/resources/qc/test2/samtools/test2.txt"));
        List data = parser.getSamtool().getlistMap().get("RN");
        assertThat(data).isEqualTo(samtool.getlistMap().get("RN"));
    }

    @Test
    public void getName() {
        SamtoolsParser parser = new SamtoolsParser(new File("src/test/resources/qc/test1/samtools/test1.txt"));
        String name = parser.getName();
        assertThat(name).isEqualTo("test1");
    }

    @Test
    public void invalidParserEmptyFile() {

        assertThatThrownBy(() -> new SamtoolsParser(new File("src/test/resources/invalidqc4/qc/test1234/samtools/test1234.txt")))
            .isInstanceOf(SamtoolsParserException.class)
            .hasMessageContaining("no test found")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void invalidParserNoTests() {

        assertThatThrownBy(() -> new SamtoolsParser(new File("src/test/resources/invalidqc/qc/test1234/samtools/test1.txt")))
                .isInstanceOf(SamtoolsParserException.class)
                .hasMessageContaining("no test found")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void FileNotFound() {

        Path path = new File("NonExistingFolder").toPath();
        assertThatThrownBy(() -> SamtoolsParser.getLines(path))
            .isInstanceOf(SamtoolsParserException.class)
            .hasMessageContaining("reading data file")
            .hasCauseInstanceOf(IOException.class);
    }
}
