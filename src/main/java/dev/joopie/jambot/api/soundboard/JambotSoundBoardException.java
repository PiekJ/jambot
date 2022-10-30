package dev.joopie.jambot.api.soundboard;

import dev.joopie.jambot.JambotException;

public class JambotSoundBoardException extends JambotException {
    public JambotSoundBoardException() {
    }

    public JambotSoundBoardException(String message) {
        super(message);
    }

    public JambotSoundBoardException(String message, Throwable cause) {
        super(message, cause);
    }

    public JambotSoundBoardException(Throwable cause) {
        super(cause);
    }
}
