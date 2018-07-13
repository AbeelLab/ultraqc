package tsv;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BcbioParserExceptionTest {

    @Test
    void onlyMessage() {
        String msg = "cause message";
        assertThatThrownBy(() -> { throw new BcbioParserException(msg); })
            .isInstanceOf(BcbioParserException.class)
            .hasMessage(msg)
            .hasNoCause();
    }

    @Test
    void messageAndCause() {
        String msg = "cause message";
        Throwable cause = new NullPointerException();
        assertThatThrownBy(() -> { throw new BcbioParserException(msg, cause); })
            .isInstanceOf(BcbioParserException.class)
            .hasMessage(msg)
            .hasCause(cause);
    }
}
