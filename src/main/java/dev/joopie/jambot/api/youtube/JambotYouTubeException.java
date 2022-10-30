package dev.joopie.jambot.api.youtube;

import dev.joopie.jambot.JambotException;

public class JambotYouTubeException extends JambotException {
    public JambotYouTubeException() {
    }

    public JambotYouTubeException(String message) {
        super(message);
    }

    public JambotYouTubeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotYouTubeException(Throwable cause) {
        super(cause);
    }
}
