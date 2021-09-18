package dev.joopie.jambot.exceptions;

public class JambotMusicServiceException extends JambotException {
    public JambotMusicServiceException() {
    }

    public JambotMusicServiceException(String message) {
        super(message);
    }

    public JambotMusicServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotMusicServiceException(Throwable cause) {
        super(cause);
    }
}
