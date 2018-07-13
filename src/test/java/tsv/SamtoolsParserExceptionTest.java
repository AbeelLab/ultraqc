package tsv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class SamtoolsParserExceptionTest {

    @Test
    void onlyMessage() {
        String msg = "cause message";
        assertThatThrownBy(() -> { throw new SamtoolsParserException(msg); })
            .isInstanceOf(SamtoolsParserException.class)
            .hasMessage(msg)
            .hasNoCause();
    }

    @Test
    void messageAndCause() {
        String msg = "cause message";
        Throwable cause = new NullPointerException();
        assertThatThrownBy(() -> { throw new SamtoolsParserException(msg, cause); })
            .isInstanceOf(SamtoolsParserException.class)
            .hasMessage(msg)
            .hasCause(cause);
    }
}
