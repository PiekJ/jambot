package dev.joopie.jambot.exceptions;

public class JambotMusicPlayerException extends JambotException {
    public JambotMusicPlayerException() {
    }

    public JambotMusicPlayerException(String message) {
        super(message);
    }

    public JambotMusicPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotMusicPlayerException(Throwable cause) {
        super(cause);
    }
}
