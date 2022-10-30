package dev.joopie.jambot.music;

import dev.joopie.jambot.JambotException;

public class JambotMusicServiceException extends JambotException {
    public JambotMusicServiceException() {
    }

    public JambotMusicServiceException(final String message) {
        super(message);
    }

    public JambotMusicServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JambotMusicServiceException(final Throwable cause) {
        super(cause);
    }
}
