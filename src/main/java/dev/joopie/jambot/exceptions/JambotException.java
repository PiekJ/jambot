package dev.joopie.jambot.exceptions;

public class JambotException extends RuntimeException {
    public JambotException() {
    }

    public JambotException(final String message) {
        super(message);
    }

    public JambotException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JambotException(final Throwable cause) {
        super(cause);
    }
}
