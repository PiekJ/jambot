package dev.joopie.jambot.exceptions;

public class JambotCommandReaderException extends JambotException {
    public JambotCommandReaderException() {
    }

    public JambotCommandReaderException(final String message) {
        super(message);
    }

    public JambotCommandReaderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JambotCommandReaderException(final Throwable cause) {
        super(cause);
    }
}
