package dev.joopie.jambot.exceptions;

public class JambotCommandReaderException extends JambotException {
    public JambotCommandReaderException() {
    }

    public JambotCommandReaderException(String message) {
        super(message);
    }

    public JambotCommandReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotCommandReaderException(Throwable cause) {
        super(cause);
    }
}
