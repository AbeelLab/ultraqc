package tsv;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class BcbioParserTest {
    private Bcbio bcbio;

    @BeforeEach
    public void setup() {
        bcbio = new Bcbio();
        bcbio.addInfo("line1", "1");
        bcbio.addInfo("line2", "2");
        bcbio.addInfo("line3", "4");
    }

    @Test
    public void createNewParser() {
        BcbioParser parser = new BcbioParser(new File("src/test/resources/multiqc/report/metrics/test1_bcbio.txt"));

        assertThat(parser.getBcbio().getMap()).isEqualTo(bcbio.getMap());
    }

    @Test
    public void invalidParserEmptyFile() {

        assertThatThrownBy(() -> new BcbioParser(new File("src/test/resources/invalidmultiqc/test1_bcbio.txt")))
            .isInstanceOf(BcbioParserException.class)
            .hasMessageContaining("no information found")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void FileNotFound() {

        Path path = new File("NonExistingFolder").toPath();
        assertThatThrownBy(() -> BcbioParser.getLines(path))
            .isInstanceOf(BcbioParserException.class)
            .hasMessageContaining("reading data file")
            .hasCauseInstanceOf(IOException.class);
    }
}
