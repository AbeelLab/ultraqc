import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;

public class MainTest {
    @Test
    void optionsSuccess() {
        String[] args = {"-f", "path/file", "-s", "Cucumber", "-d", "2010-10-24", "-u", "website.com"};
        CommandLine cmd = Main.parseCmd(args);
        assertThat(cmd.getOptionValue('f'))
            .isEqualTo("path/file");
        assertThat(cmd.getOptionValue('s'))
            .isEqualTo("Cucumber");
        assertThat(cmd.getOptionValue('d'))
            .isEqualTo("2010-10-24");
        assertThat(cmd.getOptionValue('u'))
            .isEqualTo("website.com");
    }

    @Test
    void missingOptionalOptions() {
        String[] args = {"-f", "path/file", "-u", "url"};
        CommandLine cmd = Main.parseCmd(args);
        assertThat(cmd).isNotNull();
        assertThat(cmd.hasOption('d')).isFalse();
    }

    @Test
    void missingRequiredOption() {
        String[] args = {"-d", "2010-10-24"};
        CommandLine cmd = Main.parseCmd(args);
        assertThat(cmd).isNull();
    }
}
