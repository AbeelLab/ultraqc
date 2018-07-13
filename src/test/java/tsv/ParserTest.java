package tsv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParserTest {

    @Test
    public void createNewParserFastQc() throws IOException {
        Path path = Paths.get("src/test/resources/parser");
        Parser parser = new Parser(path);
        assertThat(parser.getFastQcs().size()).isEqualTo(5);
    }

    @Test
    public void createNewParserSamtools() throws IOException {
        Path path = Paths.get("src/test/resources/parser");
        Parser parser = new Parser(path);
        assertThat(parser.getSamtools().size()).isEqualTo(1);
    }

    @Test
    public void createNewParserBcbio() throws IOException {
        Path path = Paths.get("src/test/resources/parser");
        Parser parser = new Parser(path);
        assertThat(parser.getBcbios().size()).isEqualTo(1);
    }

}
