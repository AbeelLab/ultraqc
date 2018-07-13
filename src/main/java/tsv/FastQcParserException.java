package tsv;

/**
 * Exception for throwing during TsvParser, will most likely be thrown with an IOException given as cause.
 */
public class FastQcParserException extends RuntimeException {

    /**
     * Create a new exception with a message.
     * @param message the message of exception
     */
    public FastQcParserException(String message) {
        super(message);
    }

    /**
     * Create a new exception with message and cause.
     * @param message the message
     * @param cause the cause
     */
    public FastQcParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
