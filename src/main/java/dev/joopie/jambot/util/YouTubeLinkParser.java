package dev.joopie.jambot.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeLinkParser {
    private static final String REGEX_1 = "youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)";
    private static final String REGEX_2 = "youtu\\.be/([a-zA-Z0-9_-]+)";
    private static final Pattern PATTERN_1 = Pattern.compile(REGEX_1);
    private static final Pattern PATTERN_2 = Pattern.compile(REGEX_2);

    public static Optional<String> extractYouTubeId(String url) {


        Matcher matcher1 = PATTERN_1.matcher(url);
        Matcher matcher2 = PATTERN_2.matcher(url);

        if (matcher1.find()) {
            // Extract the ID from youtube.com/watch?v=...
            return Optional.of(matcher1.group(1));
        } else if (matcher2.find()) {
            // Extract the ID from youtu.be/...
            return Optional.of(matcher2.group(1));
        }
        return Optional.empty();
    }

    public static String parseIdToYouTubeWatchUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}
