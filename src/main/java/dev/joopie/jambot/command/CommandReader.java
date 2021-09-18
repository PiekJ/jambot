package dev.joopie.jambot.command;

import dev.joopie.jambot.exceptions.JambotCommandReaderException;

public class CommandReader {
    private static final String DELIMITER = " ";
    private static final String BOT_PREFIX = "-";

    private final String message;
    private final String id;

    private int position = BOT_PREFIX.length();

    public CommandReader(final String message) {
        this.message = message;
        this.id = readToken();
    }

    public boolean isBotPrefix() {
        return message.startsWith(BOT_PREFIX);
    }

    public boolean isEol() {
        return message.length() <= position;
    }

    public int readInteger() {
        final String token = readToken();
        return Integer.parseInt(token);
    }

    public double readDouble() {
        final String token = readToken();
        return Double.parseDouble(token);
    }

    public String readToken() {
        assertEol();
        assertBotPrefix();

        final int endPosition = getNextDelimiter();
        final String result = message.substring(position, endPosition);
        position += endPosition + 1;
        return result;
    }

    public String readTokens() {
        assertEol();
        assertBotPrefix();

        final String result = message.substring(position);
        position = message.length();
        return result;
    }

    private int getNextDelimiter() {
        return getNextDelimiter(position);
    }

    private int getNextDelimiter(final int position) {
        final int result = message.indexOf(DELIMITER, position);
        if (result < 0) {
            return message.length();
        }

        return result;
    }

    private void assertBotPrefix() {
        if (!isBotPrefix()) {
            throw new JambotCommandReaderException("Bot prefix missing");
        }
    }

    private void assertEol() {
        if (isEol()) {
            throw new JambotCommandReaderException("EOL");
        }
    }
}
