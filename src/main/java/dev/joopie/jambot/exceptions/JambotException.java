package dev.joopie.jambot.exceptions;

public class JambotException extends RuntimeException {
    public JambotException() {
    }

    public JambotException(String message) {
        super(message);
    }

    public JambotException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotException(Throwable cause) {
        super(cause);
    }
}
