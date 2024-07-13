package dev.joopie.jambot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyLinkParser {
    public static String extractSpotifyId(String url) {
        // Regex to extract the ID part
        url = removeSiParameter(url);
        String regex = "open\\.spotify\\.com/track/([a-zA-Z0-9]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            // Extract the ID
            String id = matcher.group(1);
            return id;
        }
        return null;
    }

   private static String removeSiParameter(String url) {
        // Remove the si parameter if present
        return url.replaceAll("\\?si=[a-zA-Z0-9]+", "");
    }
}
