package dev.joopie.jambot.music;

import dev.joopie.jambot.JambotException;

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
