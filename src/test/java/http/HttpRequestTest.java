package http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class HttpRequestTest {

    @Test
    public void testUrlFormatter() {
        assertThat(HttpRequest.formatUrl("https://path/to/website/")).isEqualTo("http://path/to/website");
        assertThat(HttpRequest.formatUrl("http://path/to/website")).isEqualTo("http://path/to/website");
        assertThat(HttpRequest.formatUrl("exmaple.com")).isEqualTo("http://exmaple.com");
    }
}
