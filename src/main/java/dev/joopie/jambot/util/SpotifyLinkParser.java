package dev.joopie.jambot.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyLinkParser {
    private static final String REGEX = "open\\.spotify\\.com/track/([a-zA-Z0-9]+)";
    private static final String SI_PARAMETER_REGEX = "\\?si=[a-zA-Z0-9]+";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static Optional<String> extractSpotifyId(String url) {
        // Regex to extract the ID part
        url = removeSiParameter(url);
        Matcher matcher = PATTERN.matcher(url);

        if (matcher.find()) {
            // Extract the ID
            String id = matcher.group(1);
            return Optional.of(id);
        }
        return Optional.empty();
    }

   private static String removeSiParameter(String url) {
        // Remove the si parameter if present
        return url.replaceAll(SI_PARAMETER_REGEX, "");
    }
}
