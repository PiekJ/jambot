package dev.joopie.jambot.exceptions;

public class JambotHealthcheckServiceException extends JambotException {
    public JambotHealthcheckServiceException() {
        super();
    }

    public JambotHealthcheckServiceException(String message) {
        super(message);
    }

    public JambotHealthcheckServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotHealthcheckServiceException(Throwable cause) {
        super(cause);
    }
}
