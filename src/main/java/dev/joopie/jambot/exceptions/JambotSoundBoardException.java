package dev.joopie.jambot.exceptions;

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
