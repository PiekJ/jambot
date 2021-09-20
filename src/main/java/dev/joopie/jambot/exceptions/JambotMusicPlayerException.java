package dev.joopie.jambot.exceptions;

public class JambotMusicPlayerException extends JambotException {
    public JambotMusicPlayerException() {
    }

    public JambotMusicPlayerException(final String message) {
        super(message);
    }

    public JambotMusicPlayerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JambotMusicPlayerException(final Throwable cause) {
        super(cause);
    }
}
