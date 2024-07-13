package dev.joopie.jambot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeLinkParser {
    public static String extractYouTubeId(String url) {
        // Regex patterns for different YouTube URL formats
        String regex1 = "youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)";
        String regex2 = "youtu\\.be/([a-zA-Z0-9_-]+)";

        Pattern pattern1 = Pattern.compile(regex1);
        Pattern pattern2 = Pattern.compile(regex2);

        Matcher matcher1 = pattern1.matcher(url);
        Matcher matcher2 = pattern2.matcher(url);

        if (matcher1.find()) {
            // Extract the ID from youtube.com/watch?v=...
            return matcher1.group(1);
        } else if (matcher2.find()) {
            // Extract the ID from youtu.be/...
            return matcher2.group(1);
        }
        return null;
    }

    public static String parseIdToYouTubeWatchUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}
